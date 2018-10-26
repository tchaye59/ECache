/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.slavenode.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.almightshell.ecache.common.CacheRecord;
import net.almightshell.ecache.slavenode.ECacheSlave;
import net.almightshell.ecache.slavenode.services.proto3.CacheRecordMessage;
import net.almightshell.ecache.slavenode.services.proto3.SlaveNodeServicesGrpc;

/**
 *
 * @author Shell
 */
public class SlaveNodeServicesImpl  extends SlaveNodeServicesGrpc.SlaveNodeServicesImplBase{

    @Override
    public StreamObserver<CacheRecordMessage> sendSplittedData(StreamObserver<Empty> responseObserver) {
        
        return new StreamObserver<CacheRecordMessage> (){
            @Override
            public void onNext(CacheRecordMessage message) {
               ECacheSlave.addRecord(message.getKey(),message.getAccessCount(),message.getData());
               responseObserver.onNext(Empty.newBuilder().build());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        };
    }

    
}
