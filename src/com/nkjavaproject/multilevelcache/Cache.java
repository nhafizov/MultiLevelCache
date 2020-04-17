package com.nkjavaproject.multilevelcache;

public class Cache implements Cacheable {
    private EvictionPolicy policy;
    private DiskCache diskCache;

    public Cache(long memorySize, long diskSize, String dirPath, String evictionPolicy) {
        diskCache = new DiskCache(diskSize, dirPath);
        if (evictionPolicy.equals("LFU")) {
            this.policy = new LFU();
        } else if (evictionPolicy.equals("LRU")) {
            this.policy = new LRU();
        } else {
            throw new RuntimeException("Only LFU and LRU eviction policies are available");
        }
        policy.setMemorySize(memorySize);
        policy.setDiskCache(diskCache);
    }

    @Override
    public String get(long key) {
        String memVal = policy.get(key);
        if (memVal == null) {
            memVal = diskCache.get(key);
            diskCache.diskCompaction();
            if (memVal != null) {
                policy.put(key, memVal);
            }
        }
        return memVal;
    }

    @Override
    public void put(long key, String value) {
        policy.put(key, value);
    }
}
