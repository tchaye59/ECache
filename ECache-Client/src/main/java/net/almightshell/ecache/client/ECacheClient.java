/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import java.util.stream.Collectors;
import net.almightshell.ecache.common.SlaveNode;
import net.almightshell.ecache.common.eh.Bucket;
import net.almightshell.ecache.common.eh.Directory;
import net.almightshell.ecache.common.lru.LRUCache;
import net.almightshell.ecache.common.utils.ECacheConstants;
import net.almightshell.ecache.common.utils.SlaveNodeServicesStubHolder;
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
public class ECacheClient {

    private LRUCache cache_client = null;

    private static MasterNodeServicesGrpc.MasterNodeServicesBlockingStub masterBlockingStub = null;

    private String nameSpace = null;

    private int splitVersion;
    private Directory directory = new Directory();
    private boolean clientCacheEnabled = true;
    private boolean extendibleCacheEnabled = true;

    public ECacheClient(String nameSpace, int masterPort, String masterAdress, boolean clientCacheEnabled, boolean extendibleCacheEnabled) throws Exception {
        if (nameSpace == null || nameSpace.isEmpty()) {
            throw new Exception("The name space cannot be null");
        }
        this.nameSpace = nameSpace;
        this.clientCacheEnabled = clientCacheEnabled;
        this.extendibleCacheEnabled = extendibleCacheEnabled;

        if (extendibleCacheEnabled) {
            if (masterPort <= 0) {
                masterPort = ECacheConstants.DEFAULT_PORT;
            }
            if (masterAdress == null || masterAdress.isEmpty()) {
                masterAdress = "localhost";
            }
            masterBlockingStub = MasterNodeServicesGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(masterAdress, masterPort).usePlaintext().build());
            updateMetaData();
        }

        if (clientCacheEnabled) {
            cache_client = new LRUCache(5000);
        }

    }

    private void updateMetaData() {
        MetadataMessage metadata = masterBlockingStub.getMetadata(Empty.newBuilder().build());

        directory.setBuckets(metadata.getBucketsList().stream().map(m -> {
            Bucket bucket = new Bucket();
            bucket.setSlaveNode(new SlaveNode(m.getKey(), m.getAddress(), m.getPort()));
            return bucket;
        }).collect(Collectors.toList()));

        directory.setDirectory(metadata.getDirectoryList().stream().map(m -> m.getValue()).collect(Collectors.toList()));
        directory.setGlobalDepth(metadata.getGlobalDepth());
        this.splitVersion = metadata.getSplitVersion();
    }

    public void put(long identifier, byte[] value) {
        long key = getKey(identifier);
        ByteString data = ByteString.copyFrom(value);

        if (clientCacheEnabled) {
            cache_client.put(key, data);
        }

        if (!extendibleCacheEnabled) {
            return;
        }

        if (directory.getGlobalDepth() < 0) {
            updateMetaData();
        }

        if (directory.getGlobalDepth() < 0) {
            return;
        }

        Bucket bucket = directory.getBucketByEntryKey(key);
        SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(bucket.getSlaveNode().getAddress() + ":" + bucket.getSlaveNode().getPort());

        AddRecordResponse response = stub.addRecord(AddRecordRequest.newBuilder()
                .setKey(key)
                .setData(data)
                .setSplitVersion(splitVersion)
                .build());

        if (response.getStatus().equals(AddRecordStatus.UPDATE_METADATA)) {
            updateMetaData();

            bucket = directory.getBucketByEntryKey((int) key);
            stub = SlaveNodeServicesStubHolder.getBlockingStub(bucket.getSlaveNode().getAddress() + ":" + bucket.getSlaveNode().getPort());

            stub.addRecord(AddRecordRequest.newBuilder()
                    .setKey(key)
                    .setData(data)
                    .setSplitVersion(splitVersion)
                    .build());
        }

    }

    public byte[] get(long identifier) {
        long key = getKey(identifier);
        ByteString bs = null;
        if (clientCacheEnabled) {
            bs = cache_client.get(key);
        }

        if (bs == null && extendibleCacheEnabled) {

            Bucket bucket = directory.getBucketByEntryKey((int) key);
            SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(bucket.getSlaveNode().getAddress() + ":" + bucket.getSlaveNode().getPort());

            bs = stub.getRecord(RecordKeyMessage.newBuilder().setKey(key).build()).getData();

            if (bs != null && clientCacheEnabled) {
                cache_client.put(key, bs);
            }
        }
        if (bs != null && !bs.isEmpty()) {
            return bs.toByteArray();
        }
        return null;
    }

    public void remove(long identifier) {
        long key = getKey(identifier);

        if (clientCacheEnabled) {
            cache_client.remove(key);
        }

        if (extendibleCacheEnabled) {
            Bucket bucket = directory.getBucketByEntryKey((int) key);

            if (bucket != null) {
                SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(bucket.getSlaveNode().getAddress() + ":" + bucket.getSlaveNode().getPort());
                stub.deleteRecord(RecordKeyMessage.newBuilder().setKey(key).build());
            }
        }

    }

    public void removeAll(List<Long> identifiers) {
        List<Long> keys = getKeys(identifiers);
        if (clientCacheEnabled) {
            cache_client.removeAll(keys);
        }
        if (!extendibleCacheEnabled) {
            return;
        }
        keys.parallelStream().forEach(key -> {
            Bucket bucket = directory.getBucketByEntryKey(key.intValue());
            if (bucket != null) {
                SlaveNodeServicesGrpc.SlaveNodeServicesBlockingStub stub = SlaveNodeServicesStubHolder.getBlockingStub(bucket.getSlaveNode().getAddress() + ":" + bucket.getSlaveNode().getPort());
                stub.deleteRecord(RecordKeyMessage.newBuilder().setKey(key).build());
            }
        });

    }

    private long getKey(long identifier) {
        return nameSpace.hashCode() + identifier;
    }

    private List<Long> getKeys(List<Long> identifiers) {
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
