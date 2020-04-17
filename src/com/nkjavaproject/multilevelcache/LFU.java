package com.nkjavaproject.multilevelcache;

import java.util.HashMap;
import java.util.Map;

public class LFU extends EvictionPolicy {
    private Map<Long, DoublyLinkedList> freqHashTable; // frequencyHashTable
    private Map<Long, ListNode> hashTable; // elemsHashTable
    public static final long LONG_BYTE_SIZE = 8;
    public static final String OVERFLOW_MESSAGE = "Memory cache overflow";
    private long memorySize;
    private long memoryUsed;
    private long minFreq;
    private DiskCache diskCache;

    public LFU() {
        freqHashTable = new HashMap<>();
        hashTable = new HashMap<>();
        memoryUsed = 0;
        minFreq = 0;
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
        updateCache(curNode);
        return curNode.getValue();
    }

    @Override
    public void put(long key, String value) {
        ListNode node = hashTable.get(key);
        long valueByteSize = value.getBytes().length;
        // смотрим на диске, если находим, то должны удалить оттуда значение
        diskCache.get(key);
        diskCache.diskCompaction();
        if (node != null) { // если элемент уже существует
            updateCache(node);
            memoryUsed -= node.getValue().getBytes().length;
            freeCacheMemory(valueByteSize);
            if (valueByteSize + memoryUsed + diskCache.getMemoryUsed() > memorySize) {
                throw new RuntimeException(OVERFLOW_MESSAGE);
            }
            node.setValue(value);
            memoryUsed += node.getValue().getBytes().length;
        } else { // если элемента не существовало
            freeCacheMemory(valueByteSize + LONG_BYTE_SIZE);
            if (valueByteSize + LONG_BYTE_SIZE + memoryUsed + diskCache.getMemoryUsed() > memorySize) {
                throw new RuntimeException(OVERFLOW_MESSAGE);
            }
            memoryUsed += valueByteSize;
            memoryUsed += LONG_BYTE_SIZE;
            ListNode newNode = new ListNode(key, value);
            hashTable.put(key, newNode);
            minFreq = 1;
            DoublyLinkedList minFreqDLL = freqHashTable.get(minFreq);
            if (minFreqDLL == null) {
                minFreqDLL = new DoublyLinkedList();
                freqHashTable.put(minFreq, minFreqDLL);
            }
            minFreqDLL.pushFront(newNode);
        }
    }

    private void updateCache(ListNode node) {
        long curNodeFreq = node.getFrequency();
        freqHashTable.get(curNodeFreq).removeFromDLL(node);
        if (curNodeFreq == minFreq) { // обновим минимум, если DLL с минимальной частотой оказался пустым
            if (freqHashTable.get(curNodeFreq).isEmpty()) { // удаляем пустой DLL
                freqHashTable.remove(curNodeFreq);
                minFreq++;
            }
        }
        node.updateFrequency();
        DoublyLinkedList newFreqDLL = freqHashTable.get(node.getFrequency());
        if (newFreqDLL == null) {
            newFreqDLL = new DoublyLinkedList();
            freqHashTable.put(node.getFrequency(), newFreqDLL);
        }
        newFreqDLL.pushFront(node);
    }

    private void freeCacheMemory(long elemByteSize) {
        while (memoryUsed + elemByteSize + diskCache.getMemoryUsed() > memorySize && !freqHashTable.isEmpty()) {
            DoublyLinkedList curDLL = freqHashTable.get(minFreq);
            if (curDLL == null) {
                minFreq++;
            } else {
                ListNode lPN = curDLL.popBack();
                if (lPN == null) {
                    freqHashTable.remove(minFreq);
                    minFreq++;
                    continue;
                }
                hashTable.remove(lPN.getKey());
                memoryUsed -= lPN.getValue().getBytes().length;
                memoryUsed -= LONG_BYTE_SIZE;
                diskCache.put(lPN.getKey(), lPN.getValue());
            }
        }
    }
}
