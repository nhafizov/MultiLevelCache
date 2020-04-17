package com.nkjavaproject.multilevelcache;


public class Main {
    public static void main(String[] args) {
        // директория, куда писать файлы
        String dirPath = "/home/guardian/IdeaProjects/HSE-CS-Java2020-Tasks/" +
                "task-02-multilevel-cache/src/main/java/ru/hse/cs/java2020/task02/";
        // сам кэш
        Cache cache = new Cache(500, 600, dirPath, "LFU" /* LRU или LFU*/);
        // пример
        long curMaxNum = 20;
        int iter = 1;
        long byteSum = 0;
        for (long i = 0; i < 60; i++) {
            byteSum += 8 + ("number" + i).getBytes().length;
        }
        for (long i = 0; i < curMaxNum * iter; ++i) {
            cache.put(i, "number" + i);
        }
        iter += 1;
        for (long i = curMaxNum - 10; i < curMaxNum * iter; ++i) {
            cache.put(i, "number" + i);
        }
        iter += 1;
        for (long i = curMaxNum - 10; i < curMaxNum * iter; ++i) {
            cache.put(i, "number" + i);
        }
        for (long i = 0; i < 70; ++i) {
            System.out.println(cache.get(i));
        }
        for (long i = 30; i < 70; ++i) {
            System.out.println(cache.get(i));
        }
        System.out.println(byteSum);
    }
}
