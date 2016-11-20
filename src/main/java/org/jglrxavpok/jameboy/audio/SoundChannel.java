package org.jglrxavpok.jameboy.audio;

public abstract class SoundChannel {

    protected final int startAddress;

    public SoundChannel(int startAddress) {
        this.startAddress = startAddress;
    }

    public boolean isValid(int address) {
        return address >= startAddress && address < startAddress+5;
    }

    public abstract void write(int address, byte value);

    public abstract byte read(int address);
}
