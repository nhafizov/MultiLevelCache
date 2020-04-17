package com.nkjavaproject.multilevelcache;

public interface Cacheable {
    String get(long key);

    void put(long key, String value);
}
