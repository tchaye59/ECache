/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.masternode;

import net.almightshell.ecache.common.eh.Directory;
import net.almightshell.ecache.common.eh.Bucket;
import java.util.ArrayList;
import java.util.List;
import net.almightshell.ecache.common.utils.ECacheConstants;

/**
 *
 * @author Shell Writable
 */
public class EMetadata {

    private int splitVersion = -1;

    private Directory directory = new Directory();
    private List<Bucket> pendingBuckets = new ArrayList<>();

    private int port = ECacheConstants.DEFAULT_PORT;

    public EMetadata() {
    }

    public Bucket getNodeWithKey(String key) {
        try {
            Bucket b = pendingBuckets.stream().filter(p -> p.getSlaveNode().getKey().equals(key)).findAny().get();
            if (b != null) {
                return b;
            }
        } catch (Exception e) {
        }

        try {
            Bucket b = directory.getBuckets().stream().filter(p -> p.getSlaveNode().getKey().equals(key)).findAny().get();
            if (b != null) {
                return b;
            }
        } catch (Exception e) {
        }
        return null;
    }

//    @Override
//    public void write(DataOutput out) throws IOException {
//        directory.write(out);
//    }
//
//    @Override
//    public void readFields(DataInput in) throws IOException {
//        directory.readFields(in);
//    }
    public int getSplitVersion() {
        return splitVersion;
    }

    public void setSplitVersion(int splitVersion) {
        this.splitVersion = splitVersion;
    }

    public Directory getDirectory() {
        return directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public List<Bucket> getPendingBuckets() {
        return pendingBuckets;
    }

    public void setPendingBuckets(List<Bucket> pendingBuckets) {
        this.pendingBuckets = pendingBuckets;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
