package com.nkjavaproject.multilevelcache;

public abstract class EvictionPolicy implements Cacheable {
    public abstract String get(long key);

    public abstract void put(long key, String value);

    public abstract void setMemorySize(long memorySize);

    public abstract void setDiskCache(DiskCache diskCache);
}
