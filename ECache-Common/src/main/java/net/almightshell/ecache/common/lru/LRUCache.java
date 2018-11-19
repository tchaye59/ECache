/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.common.lru;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import io.grpc.internal.IoUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shell
 */
public class LRUCache {

    private Cache<Long, ByteString> cache = null;
    private static final int MEGABYTES = 10241024;
    private int capacity = Integer.MAX_VALUE;
    private String cacheDataFile = "cache.data";

    public LRUCache() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(capacity)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
        init();
    }

    public LRUCache(int capacity) {
        this();
        this.capacity = capacity;
        init();
    }

    private void init() {
        try {
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(LRUCache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveData() {
        try {
            File file = new File(cacheDataFile);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            try (PrintWriter pw = new PrintWriter(file)) {
                cache.asMap().forEach((k, d) -> {
                    CacheData data = new CacheData(k, d.toByteArray());
                    pw.println(new Gson().toJson(data));
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(LRUCache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadData() throws IOException {
        File file = new File(cacheDataFile);
        String line;
        if (file.exists()) {
            Scanner scan = new Scanner(new File(cacheDataFile));
            while (scan.hasNext()) {
                line = scan.nextLine();
                CacheData data = new Gson().fromJson(line, CacheData.class);
                put(data.getKey(), ByteString.copyFrom(data.getData()));
            }
        }

    }

    public void put(long key, ByteString data) {
        cache.put(key, data);
    }

    public ByteString get(long key) {
        ByteString bs = cache.getIfPresent(key);
        return bs == null ? ByteString.EMPTY : bs;
    }

    public void remove(long key) {
        cache.invalidate(key);
    }

    public void removeAll() {
        cache.invalidateAll();
    }

    public void removeAll(List<Long> key) {
        cache.invalidateAll(key);
    }

    public void cleanUp() {
        cache.cleanUp();
    }

    public void removeEldestEntry() {
        if (Runtime.getRuntime().freeMemory() <= MEGABYTES) {
            cleanUp();
        }
    }

    public Cache<Long, ByteString> getCache() {
        return cache;
    }

    public int getCapacity() {
        return capacity;
    }

    private class CacheData {

        long key;
        byte[] data;

        public CacheData(long key, byte[] data) {
            this.key = key;
            this.data = data;
        }

        public long getKey() {
            return key;
        }

        public void setKey(long key) {
            this.key = key;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "CacheData{" + "key=" + key + '}';
        }

    }
}
