/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.client;

import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import net.almightshell.ecache.slavenode.services.proto3.SlaveNodeServicesGrpc;

/**
 *
 * @author Shell
 */
public class SlaveNodeServicesStubHolder {

    private static HashMap<String, SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub> blockingStubHolder = new HashMap();
    private static HashMap<String, SlaveNodeServicesGrpc.SlaveNodeServicesStub> stubHolder = new HashMap();

    public static SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub getBlockingStub(String target) {
        if (blockingStubHolder.containsKey(target)) {
            return blockingStubHolder.get(target);
        }

        SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesGrpc.newBlockingStub(ManagedChannelBuilder.forTarget(target).usePlaintext().build());
        blockingStubHolder.put(target, stub);
        return stub;
    }

    public static SlaveNodeServicesGrpc.SlaveNodeServicesStub getStub(String target) {
        if (stubHolder.containsKey(target)) {
            return stubHolder.get(target);
        }

        SlaveNodeServicesGrpc.SlaveNodeServicesStub stub = SlaveNodeServicesGrpc.newStub(ManagedChannelBuilder.forTarget(target).usePlaintext().build());
        stubHolder.put(target, stub);
        return stub;
    }
}
