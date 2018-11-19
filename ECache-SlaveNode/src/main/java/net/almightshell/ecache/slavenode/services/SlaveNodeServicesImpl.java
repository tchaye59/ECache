/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.slavenode.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.stream.Collectors;
import net.almightshell.ecache.slavenode.ECacheSlave;
import net.almightshell.ecache.slavenode.services.proto3.AddRecordRequest;
import net.almightshell.ecache.slavenode.services.proto3.AddRecordResponse;
import net.almightshell.ecache.slavenode.services.proto3.AddRecordStatus;
import net.almightshell.ecache.slavenode.services.proto3.CacheRecordMessage;
import net.almightshell.ecache.slavenode.services.proto3.DeleteAllRecordsRequest;
import net.almightshell.ecache.slavenode.services.proto3.RecordDataMessage;
import net.almightshell.ecache.slavenode.services.proto3.RecordKeyMessage;
import net.almightshell.ecache.slavenode.services.proto3.SlaveNodeServicesGrpc;
import net.almightshell.ecache.slavenode.services.proto3.SlaveProcessSplitRequest;
import net.almightshell.ecache.slavenode.services.proto3.SlaveProcessSplitResponse;
import net.almightshell.ecache.slavenode.services.proto3.SlaveStatInfoMessage;

/**
 *
 * @author Shell
 */
public class SlaveNodeServicesImpl extends SlaveNodeServicesGrpc.SlaveNodeServicesImplBase {

    @Override
    public StreamObserver<CacheRecordMessage> slaveSendSplitData(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<CacheRecordMessage>() {
            @Override
            public void onNext(CacheRecordMessage message) {
                ECacheSlave.addRecord(message.getKey(), message.getData());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(Empty.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }

    public SlaveNodeServicesImpl() {
    }

    @Override
    public void slaveProcessSplit(SlaveProcessSplitRequest request, StreamObserver<SlaveProcessSplitResponse> responseObserver) {
        boolean success = ECacheSlave.slaveProcessSplit(request.getCurrentNodePosition(),request.getGlobalDepth(),request.getAdress(), request.getPort());
        responseObserver.onNext(SlaveProcessSplitResponse.newBuilder().setSuccess(success).build());
        responseObserver.onCompleted();

    }

    @Override
    public void addRecord(AddRecordRequest request, StreamObserver<AddRecordResponse> responseObserver) {

        AddRecordStatus status = ECacheSlave.addRecord(request.getKey(), request.getData(), request.getSplitVersion());

        responseObserver.onNext(AddRecordResponse.newBuilder().setStatus(status).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getRecord(RecordKeyMessage request, StreamObserver<RecordDataMessage> responseObserver) {
        responseObserver.onNext(RecordDataMessage.newBuilder().setData(ECacheSlave.getRecord(request.getKey())).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAllRecords(DeleteAllRecordsRequest request, StreamObserver<Empty> responseObserver) {
        ECacheSlave.deleteAllRecords(request.getKeysList().parallelStream().map(m -> m.getKey()).collect(Collectors.toList()));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteRecord(RecordKeyMessage request, StreamObserver<Empty> responseObserver) {
        ECacheSlave.deleteRecord(request.getKey());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getSlaveStatInfo(Empty request, StreamObserver<SlaveStatInfoMessage> responseObserver) {
        responseObserver.onNext(SlaveStatInfoMessage.newBuilder()
                .setAvailableMemory(Runtime.getRuntime().freeMemory())
                .setMaxMemory(Runtime.getRuntime().maxMemory())
                .setDataSize(ECacheSlave.getDataSize())
                .build());
        responseObserver.onCompleted();
    }

}
