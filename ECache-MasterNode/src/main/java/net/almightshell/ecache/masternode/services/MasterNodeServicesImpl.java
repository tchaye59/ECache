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
import net.almightshell.ecache.masternode.services.proto3.GlobalDepthResponse;
import net.almightshell.ecache.masternode.services.proto3.MasterNodeServicesGrpc;
import net.almightshell.ecache.masternode.services.proto3.MasterRequestSplitRequest;
import net.almightshell.ecache.masternode.services.proto3.MasterRequestSplitResponse;
import net.almightshell.ecache.masternode.services.proto3.MetadataMessage;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveRequest;
import net.almightshell.ecache.masternode.services.proto3.RegisterSlaveResponse;
import net.almightshell.ecache.masternode.services.proto3.SplitVersionResponse;
import java.util.ArrayList;
import java.util.List;
import net.almightshell.ecache.masternode.services.proto3.BucketMessage;

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
    public void masterRequestSplit(MasterRequestSplitRequest request, StreamObserver<MasterRequestSplitResponse> responseObserver) {
        boolean b = ECacheMaster.masterRequestSplit(request.getKey(), request.getLocalDepth(), request.getEntryKey());
        responseObserver.onNext(MasterRequestSplitResponse.newBuilder().setSuccess(b).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getSplitVersion(Empty request, StreamObserver<SplitVersionResponse> responseObserver) {
        responseObserver.onNext(SplitVersionResponse.newBuilder().setSplitVersion(ECacheMaster.getMetadata().getSplitVersion()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getMetadata(Empty request, StreamObserver<MetadataMessage> responseObserver) {
        List<DirectoryMessage> dms = new ArrayList<>();
        for (int i = 0; i < ECacheMaster.getMetadata().getDirectory().getDirectory().size(); i++) {
            dms.add(DirectoryMessage.newBuilder().setPosition(i).setValue(ECacheMaster.getMetadata().getDirectory().getDirectory().get(i)).build());
        }
        responseObserver.onNext(
                MetadataMessage.newBuilder()
                        .addAllBuckets(ECacheMaster.getMetadata().getDirectory().getBuckets().stream().map(m -> BucketMessage.newBuilder().setAddress(m.getSlaveNode().getAddress()).setKey(m.getSlaveNode().getKey()).setPort(m.getSlaveNode().getPort()).build()).collect(Collectors.toList()))
                        .addAllDirectory(dms)
                        .setGlobalDepth(ECacheMaster.getMetadata().getDirectory().getGlobalDepth())
                        .setSplitVersion(ECacheMaster.getMetadata().getSplitVersion())
                        .build()
        );
        responseObserver.onCompleted();
    }

}
