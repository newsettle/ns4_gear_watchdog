package com.creditease.ns4.gear.watchdog.common.monitor;

import java.util.LinkedList;

/**
 * @author outman
 * @description 定义循环队列
 * @date 2019/1/15
 */
public class LimitQueue<E> {

    //队列长度
    private int limit;

    private LinkedList<E> queue = new LinkedList<E>();

    public LimitQueue(int limit) {
        this.limit = limit;
    }

    /**
     * 入列：当队列大小已满时，把队头的元素poll掉
     */
    public void offer(E e) {
        if (queue.size() >= limit) {
            queue.poll();
        }
        queue.offer(e);
    }

    public E get(int position) {
        return queue.get(position);
    }

    public E getLast() {
        return queue.getLast();
    }

    public E getFirst() {
        return queue.getFirst();
    }

    public int getLimit() {
        return limit;
    }

    public int size() {
        return queue.size();
    }

    public E poll() {
        return queue.removeLast();
    }
}
