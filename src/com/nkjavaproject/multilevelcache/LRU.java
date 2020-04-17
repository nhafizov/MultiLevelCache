package com.nkjavaproject.multilevelcache;

import java.util.HashMap;

public class LRU extends EvictionPolicy {
    public static final String OVERFLOW_MESSAGE = "Memory cache overflow";
    public static final long LONG_BYTE_SIZE = 8;
    private HashMap<Long, ListNode> hashTable;
    private DoublyLinkedList cacheOrder;
    private long memorySize;
    private long memoryUsed;
    private DiskCache diskCache;

    public LRU() {
        hashTable = new HashMap<>();
        cacheOrder = new DoublyLinkedList();
        memoryUsed = 0;
    }

    @Override
    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    @Override
    public void setDiskCache(DiskCache diskCache) {
        this.diskCache = diskCache;
    }

    @Override
    public String get(long key) {
        ListNode curNode = hashTable.get(key);
        if (curNode == null) {
            return null;
        }
        cacheOrder.updateNode(curNode);
        return curNode.getValue();
    }

    public void put(long key, String value) {
        ListNode curNode = hashTable.get(key);
        long valueByteSize = value.getBytes().length;
        // смотрим на диске, если находим, то должны удалить оттуда значение
        diskCache.get(key);
        diskCache.diskCompaction();
        // если ключ уже в кэше, то просто обновить значение
        if (curNode != null) {
            // так как старое значение удалится, заранее удалим его из подсчета памяти
            memoryUsed -= curNode.getValue().getBytes().length;
            // удаляем текущие элементы из кэша, пока не освободим достаточно памяти или кэш не станет пустым
            freeCacheMemory(valueByteSize);
            // если памяти для элемента не хватает, выкидываем исключение
            if (valueByteSize + memoryUsed + diskCache.getMemoryUsed() > memorySize) {
                throw new RuntimeException(OVERFLOW_MESSAGE);
            }
            // обновляем элемент по ключу
            curNode.setValue(value);
            memoryUsed += valueByteSize;
        } else {
            // удаляем текущие элементы из кэша, пока не освободим достаточно памяти или кэш не станет пустым
            freeCacheMemory(valueByteSize + LONG_BYTE_SIZE);
            // если памяти для элемента не хватает, выкидываем исключение
            if (valueByteSize + LONG_BYTE_SIZE + memoryUsed + diskCache.getMemoryUsed() > memorySize) {
                throw new RuntimeException(OVERFLOW_MESSAGE);
            }
            ListNode newNode = new ListNode(key, value);
            cacheOrder.pushFront(newNode);
            hashTable.put(key, newNode);
            memoryUsed += valueByteSize + LONG_BYTE_SIZE;
        }
    }

    private void freeCacheMemory(long elemByteSize) {
        while (memoryUsed + elemByteSize + diskCache.getMemoryUsed() > memorySize && !cacheOrder.isEmpty()) {
            ListNode leastPE = cacheOrder.popBack();
            hashTable.remove(leastPE.getKey());
            diskCache.put(leastPE.getKey(), leastPE.getValue());
            memoryUsed -= leastPE.getValue().getBytes().length;
            memoryUsed -= LONG_BYTE_SIZE;
        }
    }
}
