/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.common.Node;
import net.almightshell.ecache.common.SlaveNode;
import net.almightshell.ecache.masternode.services.MasterNodeServicesImpl;

/**
 *
 * @author Shell
 */
public class ECacheMaster {

    private static boolean running = false;
    private static long currentTimeMillis = System.currentTimeMillis();

    private static int port = ECacheConstants.DEFAULT_PORT;
    private static int globalDepth;

    private static final List<SlaveNode> directory = new ArrayList<>();
    private static final List<SlaveNode> pendingNodes = new ArrayList<>();

    private static Server server = null;

    public static void start() throws IOException {
        if (!isRunning()) {
            System.out.println("Startting CacheMaster ...");
            server = ServerBuilder.forPort(port)
                    .addService(new MasterNodeServicesImpl())
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
        SlaveNode node = null;
        try {
            if (node == null) {
                node = pendingNodes.stream().filter(n -> n.getKey().equals(key)).findAny().get();
            }
        } catch (Exception e) {
        }
        try {
            node = directory.stream().filter(n -> n.getKey().equals(key)).findAny().get();
        } catch (Exception e) {
        }

        if (node != null) {
            node.setAddress(address);
            node.setPort(port);
            return key;
        } else {
            String newkey = generateAkey();
            node = new SlaveNode(newkey, address, port);
            pendingNodes.add(node);
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
    }

    public static void requestSplit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    

}
