package org.jglrxavpok.jameboy.audio;

import org.jglrxavpok.jameboy.utils.BitUtils;

public class ToneSweepSoundChannel extends SoundChannel {

    private final boolean handlesSweep;
    private byte channel1SweepReg;
    private int sweepTime;
    private int sweepIncrease;
    private int sweepShiftCount;
    private int patternDutty;
    private float soundLength;
    private int initialVolumeEnvelope;
    private int envelopeDirection;
    private int lowerChannelFreq;
    private int highChannelFreq;
    private boolean consecutiveSelection;
    private boolean restartSound;

    public ToneSweepSoundChannel(int startAddress, boolean handlesSweep) {
        super(startAddress);
        this.handlesSweep = handlesSweep;
    }

    public void write(int address, byte value) {
        int offset = address-startAddress;
        if(handlesSweep && offset == 0) { // NR10-like
            channel1SweepReg = value;
            sweepTime = (value & 0x70) >> 4;
            sweepIncrease = BitUtils.getBit(value, 3) ? -1 : 1;
            sweepShiftCount = value & 0x07;
        } else if(offset == 1) { // NR11-like
            patternDutty = (value & 0xC) >> 6;
            int t1 = value & 0x3F;
            soundLength = (64f-t1)*(1f/256f);
        } else if(offset == 2) { // NR12-like
            initialVolumeEnvelope = (value & 0xF0) >> 4;
            envelopeDirection = BitUtils.getBit(value, 3) ? 1 : -1;
        } else if(offset == 3) { // NR13-like
            lowerChannelFreq = value & 0xFF;
        } else if(offset == 4) { // NR14-like
            highChannelFreq = value & 0x7;
            consecutiveSelection = BitUtils.getBit(value, 1);
            restartSound = BitUtils.getBit(value, 7);
        }
    }

    private int calculateFrequency() {
        int x = highChannelFreq << 8 | lowerChannelFreq;
        return 131072/(2048-x);
    }

    public byte read(int address) {
        int offset = address-startAddress;
        return 0;
    }
}
