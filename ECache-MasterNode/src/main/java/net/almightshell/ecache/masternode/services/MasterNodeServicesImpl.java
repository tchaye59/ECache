/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode.services;

import io.grpc.stub.StreamObserver;
import net.almightshell.ecache.masternode.ECacheMaster;
import net.almightshell.ecache.masternode.services.proto3.DoubleDirectoryRequest;
import net.almightshell.ecache.masternode.services.proto3.DoubleDirectoryResponse;
import net.almightshell.ecache.masternode.services.proto3.GlobalDepthRequest;
import net.almightshell.ecache.masternode.services.proto3.GlobalDepthResponse;
import net.almightshell.ecache.masternode.services.proto3.MasterNodeServicesGrpc;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveRequest;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveResponse;
import net.almightshell.ecache.masternode.services.proto3.RequestSplitRequest;
import net.almightshell.ecache.masternode.services.proto3.RequestSplitResponse;

/**
 *
 * @author Shell
 */
public class MasterNodeServicesImpl extends MasterNodeServicesGrpc.MasterNodeServicesImplBase {

    @Override
    public void registerSlave(RegisterSlaveRequest request, StreamObserver<RegisterSlaveResponse> responseObserver) {
        RegisterSlaveResponse response = RegisterSlaveResponse.newBuilder()
                .setKey(ECacheMaster.registerSlave(request.getKey(), "", request.getPort()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getGlobalDepth(GlobalDepthRequest request, StreamObserver<GlobalDepthResponse> responseObserver) {
        responseObserver.onNext(GlobalDepthResponse.newBuilder()
                .setGlobalDepth(ECacheMaster.getGlobalDepth())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void doubleDirectory(DoubleDirectoryRequest request, StreamObserver<DoubleDirectoryResponse> responseObserver) {

        ECacheMaster.doubleDirectory();
        responseObserver.onNext(DoubleDirectoryResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void requestSplit(RequestSplitRequest request, StreamObserver<RequestSplitResponse> responseObserver) {

        ECacheMaster.requestSplit();
        responseObserver.onNext(RequestSplitResponse.newBuilder()
                .build());
        responseObserver.onCompleted();
    }

}
