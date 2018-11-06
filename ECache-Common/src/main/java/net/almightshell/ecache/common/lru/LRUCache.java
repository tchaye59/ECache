/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.almightshell.ecache.common.lru;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;

/**
 *
 * @author Shell
 */
public class LRUCache {

    private LoadingCache<Long, ByteString> loadingCache = null;
    private static final int MEGABYTES = 10241024;
    private String fileName;
    private int capacity = Integer.MAX_VALUE;

    public LRUCache() {

    }
    public LRUCache(int capacity) {
        this.capacity = capacity;
    }

    public void init(String fileName) {
        this.fileName = fileName;

        loadingCache = CacheBuilder.newBuilder()
                .maximumSize(capacity)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build(
                        new CacheLoader<Long, ByteString>() {
                    @Override
                    public ByteString load(Long key) throws Exception {
                        throw new Exception("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }
                );
    }

    public void loadData() throws IOException {
        GenericDatumReader datum = new GenericDatumReader();
        try (DataFileReader reader = new DataFileReader(new File(fileName), datum)) {
            GenericData.Record record = new GenericData.Record(reader.getSchema());
            while (reader.hasNext()) {
                reader.next(record);
                put((long) record.get("key"), (ByteString) record.get("data"));
            }
        }
    }

    public void saveData() throws IOException {

    }

    public void put(long key, ByteString data) {
        loadingCache.put(key, data);
    }

    public ByteString get(long key) {
        try {
            return loadingCache.get(key);
        } catch (ExecutionException ex) {
            Logger.getLogger(LRUCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ByteString.EMPTY;
    }

    public void remove(long key) {
        loadingCache.invalidate(key);
    }

    public void removeAll() {
        loadingCache.invalidateAll();
    }

    public void removeAll(List<Long> key) {
        loadingCache.invalidateAll(key);
    }

    public void cleanUp() {
        loadingCache.cleanUp();
    }

    public void removeEldestEntry() {
        if (Runtime.getRuntime().freeMemory() <= MEGABYTES) {
            cleanUp();
        }
    }

    public LoadingCache<Long, ByteString> getLoadingCache() {
        return loadingCache;
    }

    public int getCapacity() {
        return capacity;
    }
    
    

}
