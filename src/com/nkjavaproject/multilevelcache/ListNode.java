package com.nkjavaproject.multilevelcache;

class ListNode {
    private ListNode prev;
    private ListNode next;
    private long key;
    private String value;
    private long frequency;

    ListNode() {
    }

    ListNode(long key, String value) {
        this.key = key;
        this.value = value;
        frequency = 1;
    }

    public ListNode getPrev() {
        return prev;
    }

    public ListNode getNext() {
        return next;
    }

    public void setPrev(ListNode prev) {
        this.prev = prev;
    }

    public void setNext(ListNode next) {
        this.next = next;
    }

    public long getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getFrequency() {
        return frequency;
    }

    public void updateFrequency() {
        frequency++;
    }
}
