package com.nkjavaproject.multilevelcache;

import java.util.HashMap;
import java.io.RandomAccessFile;
import java.io.File;

class StringDisk {
    private int stringSize;
    private long stringPosition;

    StringDisk(int stringSize, long stringPosition) {
        this.stringSize = stringSize;
        this.stringPosition = stringPosition;
    }

    public int getStringSize() {
        return stringSize;
    }

    public long getStringPosition() {
        return stringPosition;
    }
}

public class DiskCache implements Cacheable {
    private String storageFileName = "diskCacheFile";
    private String tmpStorageFileName = "diskCacheFileTMP";
    public static final String DISK_COMPACTION_ERROR = "Can't perform disk compaction";
    public static final long LONG_BYTE_SIZE = 8;
    private String dirPath;
    private HashMap<Long, StringDisk> hashTable;
    private File cacheFile;
    private long diskSize; // provided byte size of disk memory
    private long diskUsed; // number of bytes used
    private long actualDiskUsed; // real number of bytes used
    public static final double COMPACTION_THRESHOLD = 0.6;
    private long memoryUsed;

    public DiskCache(long diskSize, String dirPath) {
        cacheFile = new File(dirPath + storageFileName);
        hashTable = new HashMap<>();
        this.diskSize = diskSize;
        this.dirPath = dirPath;
        diskUsed = 0;
        actualDiskUsed = 0;
        memoryUsed = 0;
    }

    private String get(long key, boolean modify) {
        StringDisk strDisk = hashTable.get(key);
        if (strDisk == null) {
            return null;
        }
        try (RandomAccessFile raf = new RandomAccessFile(cacheFile, "r")) {
            raf.seek(strDisk.getStringPosition());
            byte[] b = new byte[strDisk.getStringSize()];
            raf.readFully(b);
            String str = new String(b);
            if (modify) { // удалять ключи из hashTable или нет при возвращении элементов
                hashTable.remove(key);
                memoryUsed -= LONG_BYTE_SIZE;
                actualDiskUsed -= strDisk.getStringSize();
            }
            return str;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String get(long key) {
        return get(key, true);
    }

    @Override
    public void put(long key, String value) {
        long valueByteSize = value.getBytes().length;
        // если диск переполнен, то писать перестаем
        if (diskUsed + valueByteSize > diskSize) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(cacheFile, "rw")) {
            StringDisk stringDisk = hashTable.get(key);
            if (stringDisk != null) {
                actualDiskUsed -= stringDisk.getStringSize();
                memoryUsed -= LONG_BYTE_SIZE;
            }
            memoryUsed += LONG_BYTE_SIZE;
            int stringByteSize = value.getBytes().length;
            hashTable.put(key, new StringDisk(stringByteSize, diskUsed));
            raf.seek(diskUsed);
            raf.writeBytes(value);
            diskUsed += stringByteSize;
            actualDiskUsed += stringByteSize;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void diskCompaction() {
        if ((double) actualDiskUsed / diskUsed < COMPACTION_THRESHOLD && !hashTable.isEmpty()) {
            File tmpFile = new File(dirPath + tmpStorageFileName);
            try (RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw")) {
                long position = 0L;
                HashMap<Long, StringDisk> newHashTable = new HashMap<>();
                for (long key : hashTable.keySet()) {
                    String curVal = get(key, false);
                    if (curVal == null) {
                        continue;
                    }
                    int curValByteSize = curVal.getBytes().length;
                    newHashTable.put(key, new StringDisk(curValByteSize, position));
                    raf.seek(position);
                    raf.writeBytes(curVal);
                    position += curValByteSize;
                }
                hashTable = newHashTable;
                try {
                    cacheFile.delete();
                    tmpFile.renameTo(cacheFile);
                } catch (Exception e) {
                    System.out.println(DISK_COMPACTION_ERROR);
                }
                cacheFile = new File(dirPath + storageFileName);
                actualDiskUsed = position;
                diskUsed = position;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }
}
