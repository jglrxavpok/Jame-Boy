package org.jglrxavpok.jameboy.audio;

import org.jglrxavpok.jameboy.utils.BitUtils;

public class WaveOutputChannel extends SoundChannel {

    private boolean running;
    private float soundLength;
    private OutputLevel outputLevel;
    private int lowerChannelFreq;
    private boolean restartSound;
    private boolean consecutiveSelection;
    private int highChannelFreq;

    public WaveOutputChannel(int startAddress) {
        super(startAddress);
    }

    @Override
    public void write(int address, byte value) {
        int offset = address-startAddress;
        if(offset == 0) { // NR30
            running = BitUtils.getBit(value, 7);
        } else if(offset == 1) { // NR31
            int t1 = value & 0xFF;
            soundLength = (256f-t1)*(1f/256f);
        } else if(offset == 2) { // NR32
            outputLevel = OutputLevel.values()[(value & 0x60) >> 5];
        } else if(offset == 3) { // NR33
            lowerChannelFreq = value & 0xFF;
        } else if(offset == 4) { // NR34
            highChannelFreq = value & 0x7;
            consecutiveSelection = BitUtils.getBit(value, 1);
            restartSound = BitUtils.getBit(value, 7);
        }
    }
    private int calculateFrequency() {
        int x = highChannelFreq << 8 | lowerChannelFreq;
        return 65536/(2048-x);
    }


    @Override
    public byte read(int address) {
        return 0;
    }

    public enum OutputLevel {
        MUTE, FULL, HALF, QUARTER
    }
}
