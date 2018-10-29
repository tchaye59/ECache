/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.slavenode;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import net.almightshell.ecache.common.lru.LRUCache;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.masternode.services.proto3.MasterNodeServicesGrpc;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveRequest;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveResponse;
import net.almightshell.ecache.masternode.services.proto3.RequestSplitRequest;
import net.almightshell.ecache.masternode.services.proto3.RequestSplitResponse;
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

    private static String masterAdress = "localhost";
    private static ManagedChannel masterShannel = ManagedChannelBuilder.forAddress(masterAdress, port)
            .usePlaintext()
            .build();
    private static MasterNodeServicesGrpc.MasterNodeServicesBlockingStub masterBlockingStub = MasterNodeServicesGrpc.newBlockingStub(masterShannel);

    private static Server server = null;
    
    private static LRUCache cache = new LRUCache();

    public static void start() throws IOException, Exception {
        if (!isRunning()) {
            System.out.println("Starting CacheSlave ...");

            registerSlave();

            server = ServerBuilder.forPort(port)
                    //                .addService(new MasterNodeServicesImpl())
                    .build();
            server.start();
            System.out.println("CacheSlave started.");
            setRunning(true);
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

    private static void registerSlave() throws Exception {

        RegisterSlaveResponse response = null;
        try {
            response = masterBlockingStub.registerSlave(RegisterSlaveRequest.newBuilder()
                    .setKey(key)
                    .setPort(port)
                    .build());
        } catch (Exception e) {
            throw new Exception("Error : Failed to connect to the master.");
        }
        key = response.getKey();

        if (key == null || key.isEmpty()) {
            throw new Exception("Error : fail to register the Slave to the master.");
        } else {
            System.err.println("Slave successful registered with the key : " + key);
        }
    }

    private static boolean processSplit() {

        RequestSplitResponse response = masterBlockingStub.requestSplit(RequestSplitRequest.newBuilder().build());
        String host = response.getAddress();
        if (host == null || host.isEmpty()) {
            return false;
        }

        SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(host, response.getPort()).build());
         
        return false;
    }

    public static void setPort(int port) {
        ECacheSlave.port = port;
    }

    public static int getPort() {
        return port;
    }

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        ECacheSlave.running = running;
    }

    public static void addRecord(long key, ByteString data) {
         cache.put(key, data);
    }
    
    public static void deleteRecord(long key) {
         cache.remove(key);
    }
    

}
