/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.almightshell.ecache.common.Node;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.common.SlaveNode;
import net.almightshell.ecache.common.utils.ECacheUtil;
import net.almightshell.ecache.masternode.services.RegisterSlaveInterceptor;
import net.almightshell.ecache.masternode.services.MasterNodeServicesImpl;
import net.almightshell.ecache.masternode.services.SlaveNodeServicesStubHolder;
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

    private static int port = ECacheConstants.DEFAULT_PORT;
    private static int globalDepth = -1;

    private static final List<SlaveNode> directory = new ArrayList<>();
    private static final List<SlaveNode> pendingNodes = new ArrayList<>();
    private static final Map<String, SlaveNode> allNodes = new HashMap<>();
    private static int splitVersion = -1;

    private static Server server = null;

    public static void start() throws IOException {
        if (!isRunning()) {
            System.out.println("Startting CacheMaster ...");
            server = ServerBuilder.forPort(port)
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
        SlaveNode node = allNodes.get(key);

        if (node != null) {
            node.setAddress(address);
            node.setPort(port);
            return key;
        } else {
            String newkey = generateAkey();
            node = new SlaveNode(newkey, address, port);
            pendingNodes.add(node);
            allNodes.put(newkey, node);

            if (directory.isEmpty()) {
                directory.add(node);
                pendingNodes.remove(node);
                globalDepth = 0;
                splitVersion = 0;
            }
            return newkey;
        }

    }

    private static String generateAkey() {
        return String.valueOf((currentTimeMillis++));
    }

    public static void setPort(int port) {
        ECacheMaster.port = port;
    }

    public static int getPort() {
        return port;
    }

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        ECacheMaster.running = running;
    }

    public static int getGlobalDepth() {
        return globalDepth;
    }

    public static void doubleDirectory() {
        directory.addAll(directory);
        globalDepth++;
    }

    public static boolean masterRequestSplit(String key) {

        Node node = allNodes.get(key);
        SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(node.getAddress()+":"+ node.getPort());

        for (SlaveNode sn : pendingNodes) {

            boolean success = stub.slaveProcessSplit(
                    SlaveProcessSplitRequest.newBuilder()
                            .setAdress(sn.getAddress())
                            .setPort(sn.getPort()).build()).getSuccess();
            if (success) {
                int position  = ECacheUtil.checkNodeFromSplitDirectoryPosition(directory.indexOf(node), globalDepth);
                directory.set(position,sn);
                splitVersion++;
                pendingNodes.remove(sn);
                return success;
            }
        }

        return false;
    }

    public static List<SlaveNode> getDirectory() {
        return directory;
    }

    public static int getSplitVersion() {
        return splitVersion;
    }

    static void info() {
        if (running) {
            System.out.println("    GlobalDepth = " + globalDepth);
            System.out.println("    SplitVersion = " + splitVersion);
            System.out.println("    AllNodes = " + allNodes.size());
            System.out.println("    Pending Nodes = " + pendingNodes.size());
            System.out.println("    Using Nodes = " + (allNodes.size() - pendingNodes.size()));
            System.out.println("    Directory : ");

            long total = directory.parallelStream().map(node -> {
                SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(node.getAddress() + ":" + node.getPort());
                SlaveStatInfoMessage response = stub.getSlaveStatInfo(Empty.newBuilder().build());

                System.out.println("    Key " + node.getKey()
                        + "             Address " + node.getAddress() + ":" + node.getPort()
                        + "             DataSize " + response.getDataSize()
                        + "             MaxMemory " + response.getMaxMemory()
                        + "             AvailableMemory " + response.getAvailableMemory());
                return response.getDataSize();
            }).reduce((x, y) -> x + y).get();

            System.out.println("    Total DataSize = " + total);

            System.out.println("Pending Nodes : ");
            pendingNodes.stream().forEach(n -> System.err.println("Address : " + n.getAddress()));

        } else {
            System.err.println("The master is not started");
        }
    }

}
