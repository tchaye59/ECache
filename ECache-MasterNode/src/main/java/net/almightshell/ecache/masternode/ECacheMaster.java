/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode;

import net.almightshell.ecache.common.eh.Bucket;
import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import java.io.IOException;
import net.almightshell.ecache.common.SlaveNode;
import net.almightshell.ecache.masternode.services.RegisterSlaveInterceptor;
import net.almightshell.ecache.masternode.services.MasterNodeServicesImpl;
import net.almightshell.ecache.common.utils.SlaveNodeServicesStubHolder;
import net.almightshell.ecache.slavenode.services.proto3.SlaveNodeServicesGrpc;
import net.almightshell.ecache.slavenode.services.proto3.SlaveProcessSplitRequest;
import net.almightshell.ecache.slavenode.services.proto3.SlaveStatInfoMessage;

/**
 *
 * @author Shell
 */
public class ECacheMaster {

    private static boolean running = false;
    private static long currentTimeMillis = System.currentTimeMillis();

    private static Server server = null;
    private static EMetadata metadata = new EMetadata();

    public static void start() throws IOException {
        if (!isRunning()) {
            System.out.println("Startting CacheMaster ...");
            server = ServerBuilder.forPort(metadata.getPort())
                    //                    .addService(new MasterNodeServicesImpl())
                    .addService(ServerInterceptors.intercept(new MasterNodeServicesImpl(), new RegisterSlaveInterceptor()))
                    .build();
            server.start();

            System.out.println("CacheMaster started.");
            setRunning(true);
        }

    }

    public static void stop() throws InterruptedException {
        if (isRunning()) {
            System.out.println("Stopping CacheMaster ...");
            server.shutdown();
            server.awaitTermination();
            System.out.println("CacheMaster Stopped");
            setRunning(false);
        }
    }

    public static String registerSlave(final String key, String address, int port) {
        Bucket bucket = metadata.getNodeWithKey(key);

        if (bucket != null) {
            bucket.getSlaveNode().setAddress(address);
            bucket.getSlaveNode().setPort(port);
            return key;
        } else {
            String newkey = generateAkey();
            bucket = new Bucket();
            bucket.setSlaveNode(new SlaveNode(newkey, address, port));

            metadata.getPendingBuckets().add(bucket);

            if (metadata.getDirectory().getDirectory().isEmpty()) {
                metadata.getDirectory().init(bucket);

                metadata.getPendingBuckets().remove(bucket);
                metadata.setSplitVersion(0);
            }
            return newkey;
        }

    }

    private static String generateAkey() {
        return String.valueOf((currentTimeMillis++));
    }

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        ECacheMaster.running = running;
    }

    public static boolean masterRequestSplit(String slaveKey, int localDepth, long entryKey) {

        Bucket bucket = metadata.getNodeWithKey(slaveKey);
        SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(bucket.getSlaveNode().getAddress() + ":" + bucket.getSlaveNode().getPort());

        //
        if (metadata.getDirectory().getGlobalDepth() == localDepth) {
            metadata.getDirectory().doubleSize();
        }
        int[] poss = metadata.getDirectory().splitPositionsInDirectory(entryKey);
        int pos1 = poss[0];
        int pos2 = poss[1];
        for (Bucket b : metadata.getPendingBuckets()) {

            boolean success = stub.slaveProcessSplit(
                    SlaveProcessSplitRequest.newBuilder()
                            .setAdress(b.getSlaveNode().getAddress())
                            .setPort(b.getSlaveNode().getPort())
                            .setCurrentNodePosition(pos1)
                            .setGlobalDepth(metadata.getDirectory().getGlobalDepth())
                            .build()).getSuccess();
            if (success) {
                metadata.getDirectory().putBucket(bucket, pos1);
                metadata.getDirectory().putBucket(b, pos2);
                metadata.setSplitVersion(metadata.getSplitVersion() + 1);
                metadata.getPendingBuckets().remove(b);
                return success;
            }
        }

        return false;
    }

    static void info() {
        if (running) {
            System.out.println("    GlobalDepth = " + metadata.getDirectory().getGlobalDepth());
            System.out.println("    SplitVersion = " + metadata.getSplitVersion());
            System.out.println("    AllNodes = " + (metadata.getDirectory().getBuckets().size() + metadata.getPendingBuckets().size()));
            System.out.println("    Pending Nodes = " + metadata.getPendingBuckets().size());
            System.out.println("    Using Nodes = " + metadata.getDirectory().getBuckets().size());
            System.out.println("    Using Nodes : ");

            long total = metadata.getDirectory().getBuckets().parallelStream().map(bucket -> {
                SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(bucket.getSlaveNode().getAddress() + ":" + bucket.getSlaveNode().getPort());
                SlaveStatInfoMessage response = stub.getSlaveStatInfo(Empty.newBuilder().build());

                System.out.println("        ->Key " + bucket.getSlaveNode().getKey()
                        + "             Address " + bucket.getSlaveNode().getAddress() + ":" + bucket.getSlaveNode().getPort()
                        + "             DataSize " + response.getDataSize()
                        + "             MaxMemory " + response.getMaxMemory()
                        + "             AvailableMemory " + response.getAvailableMemory());
                return response.getDataSize();
            }).reduce((x, y) -> x + y).get();

            System.out.println("    Total DataSize = " + total);

            System.out.println("Pending Nodes : ");
            metadata.getPendingBuckets().stream().forEach(n -> System.err.println("Address : " + n.getSlaveNode().getAddress()));

        } else {
            System.err.println("The master is not started");
        }
    }

    public static void setPort(int port) {
        metadata.setPort(port);
    }

    public static int getGlobalDepth() {
        return metadata.getDirectory().getGlobalDepth();
    }

    public static EMetadata getMetadata() {
        return metadata;
    }

}
