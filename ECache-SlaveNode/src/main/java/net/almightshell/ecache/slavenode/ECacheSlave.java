/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.slavenode;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import net.almightshell.ecache.common.SlaveNode;
import net.almightshell.ecache.common.lru.LRUCache;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.common.utils.ECacheUtil;
import net.almightshell.ecache.masternode.services.proto3.DoubleDirectoryRequest;
import net.almightshell.ecache.masternode.services.proto3.MasterNodeServicesGrpc;
import net.almightshell.ecache.masternode.services.proto3.MasterRequestSplitRequest;
import net.almightshell.ecache.masternode.services.proto3.MasterRequestSplitResponse;
import net.almightshell.ecache.masternode.services.proto3.MetadataMessage;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveRequest;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveResponse;
import net.almightshell.ecache.slavenode.services.SlaveNodeServicesImpl;
import net.almightshell.ecache.slavenode.services.SlaveNodeServicesStubHolder;
import net.almightshell.ecache.slavenode.services.proto3.AddRecordStatus;
import net.almightshell.ecache.slavenode.services.proto3.CacheRecordMessage;
import net.almightshell.ecache.slavenode.services.proto3.SlaveNodeServicesGrpc;

/**
 *
 * @author Shell
 */
public class ECacheSlave {

    private static boolean running = false;
    private static int port = ECacheConstants.DEFAULT_PORT;

    long maxMemory = Runtime.getRuntime().maxMemory();

    private static String key = "";
    private static int localDepth;
    private static int globalDepth;
    private static int splitVersion;

    private static int masterPort = ECacheConstants.DEFAULT_PORT;
    private static String masterAdress = "localhost";
    private static MasterNodeServicesGrpc.MasterNodeServicesBlockingStub masterBlockingStub = null;

    private static Server server = null;

    private static LRUCache cache = new LRUCache();

    public static void start() throws IOException, Exception {
        if (!isRunning()) {
            System.out.println("Starting CacheSlave ...");

            //
            masterBlockingStub = MasterNodeServicesGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(masterAdress, masterPort)
                    .usePlaintext()
                    .build());

            registerSlave();

            server = ServerBuilder.forPort(port)
                    .addService(new SlaveNodeServicesImpl())
                    .build();
            server.start();
            System.out.println("CacheSlave started.");
            setRunning(true);
            updateMetadata();
        }

        cache.init("");
    }

    public static void stop() throws InterruptedException {
        if (isRunning()) {
            System.out.println("Stopping CacheSlave ...");
            server.shutdown();
            server.awaitTermination();
            System.out.println("CacheSlave Stopped");
            setRunning(false);
        }
    }

    private static boolean requireSplit() {
        return cache.getLoadingCache().size() > 10;
    }

    private static void registerSlave() throws Exception {

        RegisterSlaveResponse response = null;
        try {
            response = masterBlockingStub.registerSlave(RegisterSlaveRequest.newBuilder()
                    .setKey(key)
                    .setPort(port)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error : Failed to connect to the master.");
        }
        key = response.getKey();

        if (key == null || key.isEmpty()) {
            throw new Exception("Error : fail to register the Slave to the master.");
        } else {
            System.out.println("Slave successful registered with the key : " + key);
        }
    }

    private static void requestASplit() {
        MasterRequestSplitResponse response = masterBlockingStub.masterRequestSplit(MasterRequestSplitRequest.newBuilder().setKey(key).build());
        if (response.getSuccess()) {
            updateMetadata();
            localDepth = globalDepth;
        }
    }

    public static boolean slaveProcessSplit(String adress, int port) {
        SlaveNodeServicesGrpc.SlaveNodeServicesStub stub = SlaveNodeServicesStubHolder.getStub(adress + ":" + port);
        StreamObserver<CacheRecordMessage> toSlave = stub.slaveSendSplitData(new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {

            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });

        //D
        List<Long> keys = new LinkedList<>();

        MetadataMessage response = masterBlockingStub.getMetadata(Empty.newBuilder().build());

        List<SlaveNode> directory = response.getDirectoryList().stream().map(m -> new SlaveNode(m.getKey(), m.getAddress(), m.getPort())).collect(Collectors.toList());
        globalDepth = response.getGlobalDepth();
        splitVersion = response.getSplitVersion();

        int currentNodePosition = directory.indexOf(directory.stream().filter(p -> p.getKey().equals(key)).findFirst().get());

        //redistribute data to the new node
        cache.getLoadingCache().asMap().forEach((k, d) -> {
            if (ECacheUtil.checkPositionInDirectory(k, globalDepth) != currentNodePosition) {
                toSlave.onNext(CacheRecordMessage.newBuilder()
                        .setKey(k)
                        .setData(d)
                        .build());
                keys.add(k);
            }
        });

        //delete data distributed from the cache
        cache.removeAll(keys);

        toSlave.onCompleted();
        return true;

    }

    public static void setPort(int port) {
        ECacheSlave.port = port;
    }

    public static int getPort() {
        return port;
    }

    public static long getDataSize() {
        return cache.getLoadingCache().size();
    }

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        ECacheSlave.running = running;
    }

    public static AddRecordStatus addRecord(long key, ByteString data, int splitVersion) {
        if (ECacheSlave.splitVersion != splitVersion) {
            return AddRecordStatus.UPDATE_METADATA;
        }

        addRecord(key, data);

//        new Thread(() -> {
        if (localDepth == globalDepth) {
            System.out.println("Double Directory size");
            masterBlockingStub.doubleDirectory(DoubleDirectoryRequest.newBuilder().build());
            updateMetadata();
        }

        if (requireSplit()) {
            System.out.println("Node reqire a split ");
            requestASplit();
        }
//        }).start();

        return AddRecordStatus.SUCESS;
    }

    public static AddRecordStatus addRecord(long key, ByteString data) {
        cache.put(key, data);
        return AddRecordStatus.SUCESS;
    }

    public static ByteString getRecord(long key) {
        return cache.get(key);
    }

    public static void deleteRecord(long key) {
        cache.remove(key);
    }

    public static void deleteAllRecords(List<Long> keys) {
        cache.removeAll(keys);
    }

    private static void updateMetadata() {
        ECacheSlave.globalDepth = masterBlockingStub.getGlobalDepth(Empty.newBuilder().build()).getGlobalDepth();
        ECacheSlave.splitVersion = masterBlockingStub.getSplitVersion(Empty.newBuilder().build()).getSplitVersion();
    }

    public static void setMasterAdress(String masterAdress) {
        ECacheSlave.masterAdress = masterAdress;
    }

    public static void setMasterPort(int masterPort) {
        ECacheSlave.masterPort = masterPort;
    }

}
