/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.stream.Collectors; 
import net.almightshell.ecache.masternode.ECacheMaster;
import net.almightshell.ecache.masternode.services.proto3.DirectoryMessage;
import net.almightshell.ecache.masternode.services.proto3.DirectoryMessageRespponse;
import net.almightshell.ecache.masternode.services.proto3.DoubleDirectoryRequest;
import net.almightshell.ecache.masternode.services.proto3.DoubleDirectoryResponse;
import net.almightshell.ecache.masternode.services.proto3.GlobalDepthResponse;
import net.almightshell.ecache.masternode.services.proto3.MasterNodeServicesGrpc;
import net.almightshell.ecache.masternode.services.proto3.MasterRequestSplitRequest;
import net.almightshell.ecache.masternode.services.proto3.MasterRequestSplitResponse;
import net.almightshell.ecache.masternode.services.proto3.MetadataMessage;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveRequest;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveResponse;
import net.almightshell.ecache.masternode.services.proto3.SplitVersionResponse;
import io.grpc.Context;
import io.grpc.ServerInterceptors;
/**
 *
 * @author Shell
 */
public class MasterNodeServicesImpl extends MasterNodeServicesGrpc.MasterNodeServicesImplBase {
    
    

    @Override
    public void registerSlave(RegisterSlaveRequest request, StreamObserver<RegisterSlaveResponse> responseObserver) {
        
        String[] tab = RegisterSlaveInterceptor.salveAdresse.split(":");
        
        RegisterSlaveResponse response = RegisterSlaveResponse.newBuilder()
                .setKey(ECacheMaster.registerSlave(request.getKey(), tab[0], request.getPort()))
                .build();
        

        
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getGlobalDepth(Empty request, StreamObserver<GlobalDepthResponse> responseObserver) {
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
    public void masterRequestSplit(MasterRequestSplitRequest request, StreamObserver<MasterRequestSplitResponse> responseObserver) {
        boolean b = ECacheMaster.masterRequestSplit(request.getKey());
        responseObserver.onNext(MasterRequestSplitResponse.newBuilder().setSuccess(b).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getDirectory(Empty request, StreamObserver<DirectoryMessageRespponse> responseObserver) {

        responseObserver.onNext(DirectoryMessageRespponse.newBuilder()
                .addAllDirectory(ECacheMaster.getDirectory().stream()
                        .map(d -> DirectoryMessage.newBuilder()
                        .setKey(d.getKey())
                        .setAddress(d.getAddress())
                        .setPort(d.getPort())
                        .setPosition(ECacheMaster.getDirectory().indexOf(d)).build())
                        .collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getSplitVersion(Empty request, StreamObserver<SplitVersionResponse> responseObserver) {
        responseObserver.onNext(SplitVersionResponse.newBuilder().setSplitVersion(ECacheMaster.getSplitVersion()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getMetadata(Empty request, StreamObserver<MetadataMessage> responseObserver) {
        responseObserver.onNext(MetadataMessage.newBuilder()
                .setSplitVersion(ECacheMaster.getSplitVersion())
                .setGlobalDepth(ECacheMaster.getGlobalDepth())
                .addAllDirectory(ECacheMaster.getDirectory().stream()
                        .map(d -> DirectoryMessage.newBuilder()
                        .setKey(d.getKey())
                        .setAddress(d.getAddress())
                        .setPort(d.getPort())
                        .setPosition(ECacheMaster.getDirectory().indexOf(d)).build())
                        .collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

}
