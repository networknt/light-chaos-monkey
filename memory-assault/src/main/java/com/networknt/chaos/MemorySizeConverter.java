package com.networknt.chaos;

public class MemorySizeConverter {
    private static final int FACTOR = 1024;

    static long toMegabytes(long bytes) {
        return bytes / FACTOR / FACTOR;
    }

    static long toMegabytes(double bytes) {
        return toMegabytes((long) bytes);
    }

    static int toBytes(int megabytes) {
        return megabytes * FACTOR * FACTOR;
    }

}
