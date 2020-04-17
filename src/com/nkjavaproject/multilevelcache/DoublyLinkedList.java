package com.nkjavaproject.multilevelcache;

public class DoublyLinkedList {
    private ListNode head;
    private ListNode tail;
    private int listSize;

    public DoublyLinkedList() {
        head = new ListNode();
        tail = new ListNode();
        head.setNext(tail);
        tail.setPrev(head);
        listSize = 0;
    }

    public int getListSize() {
        return listSize;
    }

    public ListNode pushFront(long key, String value) {
        ListNode newNode = new ListNode(key, value);
        newNode.setNext(head.getNext());
        newNode.setPrev(head);
        head.getNext().setPrev(newNode);
        head.setNext(newNode);
        listSize++;
        return newNode;
    }

    public void pushFront(ListNode node) {
        node.setPrev(head);
        node.setNext(head.getNext());
        head.getNext().setPrev(node);
        head.setNext(node);
        listSize++;
    }

    public ListNode pushBack(long key, String value) {
        ListNode newNode = new ListNode(key, value);
        newNode.setNext(tail);
        newNode.setPrev(tail.getPrev());
        tail.getPrev().setNext(newNode);
        tail.setPrev(newNode);
        listSize++;
        return newNode;
    }

    public ListNode popFront() {
        ListNode firstItem = null;
        if (listSize > 0) {
            firstItem = head.getNext();
            removeFromDLL(firstItem);
        }
        return firstItem;
    }

    public ListNode popBack() {
        ListNode lastItem = null;
        if (listSize > 0) {
            lastItem = tail.getPrev();
            removeFromDLL(lastItem);
        }
        return lastItem;
    }

    // переместить существующую ноду из списка в начало
    public void updateNode(ListNode node) {
        removeFromDLL(node);
        pushFront(node);
    }

    // удалить ноду из списка
    public void removeFromDLL(ListNode node) {
        ListNode prev = node.getPrev();
        ListNode next = node.getNext();
        prev.setNext(next);
        next.setPrev(prev);
        listSize--;
    }

    public boolean isEmpty() {
        return listSize == 0;
    }
}
