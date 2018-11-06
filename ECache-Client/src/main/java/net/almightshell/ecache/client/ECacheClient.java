/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.almightshell.ecache.common.SlaveNode;
import net.almightshell.ecache.common.lru.LRUCache;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.common.utils.ECacheUtil;
import net.almightshell.ecache.masternode.services.proto3.MasterNodeServicesGrpc;
import net.almightshell.ecache.masternode.services.proto3.MetadataMessage;
import net.almightshell.ecache.slavenode.services.proto3.AddRecordRequest;
import net.almightshell.ecache.slavenode.services.proto3.AddRecordResponse;
import net.almightshell.ecache.slavenode.services.proto3.AddRecordStatus;
import net.almightshell.ecache.slavenode.services.proto3.RecordKeyMessage;
import net.almightshell.ecache.slavenode.services.proto3.SlaveNodeServicesGrpc;

/**
 *
 * @author Shell
 */
public class ECacheClient<K, V extends Serializable> {

    private final LRUCache cache_client = new LRUCache(10000);

    private static MasterNodeServicesGrpc.MasterNodeServicesBlockingStub masterBlockingStub = null;

    private String nameSpace = null;

    private int globalDepth;
    private int splitVersion;
    private List<SlaveNode> directory = new ArrayList<>();
    private boolean clientCacheEnabled = false;

    public ECacheClient(String nameSpace, int masterPort, String masterAdress, boolean clientCacheEnabled) throws Exception {
        if (nameSpace == null || nameSpace.isEmpty()) {
            throw new Exception("The name space cannot be null");
        }
        this.nameSpace = nameSpace;
        this.clientCacheEnabled = clientCacheEnabled;

        if (masterPort <= 0) {
            masterPort = ECacheConstants.DEFAULT_PORT;
        }
        if (masterAdress == null || masterAdress.isEmpty()) {
            masterAdress = "localhost";
        }
        masterBlockingStub = MasterNodeServicesGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(masterAdress, masterPort).usePlaintext().build());
        updateMetaData();
        if (clientCacheEnabled) {
            cache_client.init("");
        }

    }

    private void updateMetaData() {
        MetadataMessage metadata = masterBlockingStub.getMetadata(Empty.newBuilder().build());
        this.directory = metadata.getDirectoryList().stream().map(m -> new SlaveNode(m.getKey(), m.getAddress(), m.getPort())).collect(Collectors.toList());
        this.globalDepth = metadata.getGlobalDepth();
        this.splitVersion = metadata.getSplitVersion();
    }

    public void put(K identifier, V value) {
        long key = getKey(identifier);
        ByteString data = ByteString.copyFrom(ECacheUtil.toObjectStream(value));

        if (clientCacheEnabled) {
            cache_client.put(key, data);
        }

        if (globalDepth < 0) {
            updateMetaData();
        }

        if (globalDepth < 0) {
            return;
        }

        int position = ECacheUtil.checkPositionInDirectory(key, globalDepth);
        System.out.println("position : "+position);
        SlaveNode node = directory.get(position);
        SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(node.getAddress() + ":" + node.getPort());

        AddRecordResponse response = stub.addRecord(AddRecordRequest.newBuilder()
                .setKey(key)
                .setData(data)
                .setSplitVersion(splitVersion)
                .build());

        if (response.getStatus().equals(AddRecordStatus.UPDATE_METADATA)) {
            updateMetaData();

            stub.addRecord(AddRecordRequest.newBuilder()
                    .setKey(key)
                    .setData(data)
                    .setSplitVersion(splitVersion)
                    .build());
        }

    }

    public V get(K identifier) {
        long key = getKey(identifier);
        ByteString bs = null;
        if (clientCacheEnabled) {
            bs = cache_client.get(key);
        }

        if (bs == null && !directory.isEmpty()) {

            int position = ECacheUtil.checkPositionInDirectory(key, globalDepth);
            SlaveNode node = directory.get(position);
            SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(node.getAddress() + ":" + node.getPort());;

            bs = stub.getRecord(RecordKeyMessage.newBuilder().setKey(key).build()).getData();
        }
        if (bs != null && !bs.isEmpty()) {
            return (V) ECacheUtil.toObject(bs.toByteArray());
        }
        return null;
    }

    public void remove(K identifier) {
        long key = getKey(identifier);

        if (clientCacheEnabled) {
            cache_client.remove(key);
        }

        if (!directory.isEmpty()) {
            int position = ECacheUtil.checkPositionInDirectory(key, globalDepth);
            SlaveNode node = directory.get(position);
            SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(node.getAddress() + ":" + node.getPort());

            stub.deleteRecord(RecordKeyMessage.newBuilder().setKey(key).build());
        }
    }

    public void removeAll(List<K> identifiers) {
        List<Long> keys = getKeys(identifiers);
        if (clientCacheEnabled) {
            cache_client.removeAll(keys);
        }

        if (!directory.isEmpty()) {
            keys.parallelStream().forEach(key -> {
                int position = ECacheUtil.checkPositionInDirectory(key, globalDepth);
                SlaveNode node = directory.get(position);
                SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(node.getAddress() + ":" + node.getPort());;
                stub.deleteRecord(RecordKeyMessage.newBuilder().setKey(key).build());
            });

        }

    }

    private Long getKey(K identifier) {
        return Long.valueOf((nameSpace.hashCode() + identifier.hashCode()));
    }

    private List<Long> getKeys(List<K> identifiers) {
        return identifiers.parallelStream().map(m -> getKey(m)).collect(Collectors.toList());
    }

    public void setNameSpace(String nameSpace) throws Exception {
        if (nameSpace == null || nameSpace.isEmpty()) {
            throw new Exception("The name space cannot be null");
        }
        this.nameSpace = nameSpace;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public boolean isClientCacheEnabled() {
        return clientCacheEnabled;
    }

}
