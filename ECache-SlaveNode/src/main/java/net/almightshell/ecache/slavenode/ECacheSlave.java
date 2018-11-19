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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.almightshell.ecache.common.lru.LRUCache;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.common.utils.ECacheUtil;
import net.almightshell.ecache.common.utils.SlaveNodeServicesStubHolder;
import net.almightshell.ecache.masternode.services.proto3.MasterNodeServicesGrpc;
import net.almightshell.ecache.masternode.services.proto3.MasterRequestSplitRequest;
import net.almightshell.ecache.masternode.services.proto3.MasterRequestSplitResponse;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveRequest;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveResponse;
import net.almightshell.ecache.slavenode.services.SlaveNodeServicesImpl;
import net.almightshell.ecache.slavenode.services.proto3.AddRecordStatus;
import net.almightshell.ecache.slavenode.services.proto3.CacheRecordMessage;
import net.almightshell.ecache.slavenode.services.proto3.SlaveNodeServicesGrpc;

/**
 *
 * @author Shell
 */
public class ECacheSlave {

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

    private static int capacity = 5000;
    private static LRUCache cache = null;

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

            cache = new LRUCache(capacity);
            System.out.println("CacheSlave started.");
            updateMetadata();
        }

    }

    public static void stop() throws InterruptedException {
        if (isRunning()) {
            System.out.println("Stopping CacheSlave ...");
            server.shutdown();
            server.awaitTermination();

            cache.saveData();
            System.out.println("CacheSlave Stopped");
        }
    }

    private static boolean requireSplit() {
        return (cache.getCache().size() * 100 / capacity) > 75;
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

    private static void requestASplit(long entryKey) {
        MasterRequestSplitResponse response = masterBlockingStub.masterRequestSplit(MasterRequestSplitRequest.newBuilder().setEntryKey(entryKey).setLocalDepth(localDepth).setKey(key).build());
        if (response.getSuccess()) {
            updateMetadata();
            localDepth = globalDepth;
        }
    }

    public static boolean slaveProcessSplit(int currentNodePosition,int globalDepth,String adress, int port) {
        try {
            SlaveNodeServicesGrpc.SlaveNodeServicesStub stub = SlaveNodeServicesStubHolder.getStub(adress + ":" + port);
            StreamObserver<CacheRecordMessage> requestObserver = stub.slaveSendSplitData(new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {}

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {}
            });

            //redistribute data to the new node
            List<Long> moovedKeys = new LinkedList<>();
            cache.getCache().asMap().forEach((k, d) -> {
                if (ECacheUtil.checkPositionInDirectory(k, globalDepth) != currentNodePosition) {
                    requestObserver.onNext(CacheRecordMessage.newBuilder()
                            .setKey(k)
                            .setData(d)
                            .build());
                    moovedKeys.add(k);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ECacheSlave.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            requestObserver.onCompleted();
            
            //delete mooved data from the cache
            cache.removeAll(moovedKeys);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public static void setPort(int port) {
        ECacheSlave.port = port;
    }

    public static int getPort() {
        return port;
    }

    public static long getDataSize() {
        return cache.getCache().size();
    }

    public static boolean isRunning() {
        return server != null && !server.isShutdown();
    }

    public static AddRecordStatus addRecord(long key, ByteString data, int splitVersion) {
        if (ECacheSlave.splitVersion < splitVersion) {
            updateMetadata();
        }
        if (ECacheSlave.splitVersion != splitVersion) {
            return AddRecordStatus.UPDATE_METADATA;
        }

        addRecord(key, data);

//        new Thread(() -> {
//            if (requireSplit()) {
                requestASplit(key);
//            }
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

    public static void setCapacity(int capacity) {
        ECacheSlave.capacity = capacity;
    }

    public static int getCapacity() {
        return capacity;
    }

}
