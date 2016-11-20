package org.jglrxavpok.jameboy.audio;

public class NoiseChannel extends SoundChannel {
    public NoiseChannel(int address) {
        super(address);
    }

    @Override
    public void write(int address, byte value) {

    }

    @Override
    public byte read(int address) {
        return 0;
    }
}
