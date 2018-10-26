/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.common;

import com.google.protobuf.ByteString;

/**
 *
 * @author Shell
 */
public class CacheRecord {

    int accessCount = 0;
    ByteString data;

    public CacheRecord() {
    }

    public CacheRecord(ByteString data) {
        this.data = data;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    public ByteString getData() {
        return data;
    }

    public void setData(ByteString data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CacheRecord{" + "accessCount=" + accessCount + '}';
    }

}
