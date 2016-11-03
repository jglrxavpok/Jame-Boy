package org.jglrxavpok.jameboy.memory;

public interface MemoryType {

    public void write(int index, int value);

    public int read(int index);

    public void init(byte[] rom, int[] ram);

}
