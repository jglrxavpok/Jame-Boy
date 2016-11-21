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
    private int envelopeSweepCount;

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
            envelopeSweepCount = value & 0x7;
        } else if(offset == 3) { // NR13-like
            lowerChannelFreq = value & 0xFF;
        } else if(offset == 4) { // NR14-like
            highChannelFreq = value & 0x7;
            consecutiveSelection = BitUtils.getBit(value, 6);
            restartSound = BitUtils.getBit(value, 7);
        }
    }

    private int calculateFrequency() {
        int x = highChannelFreq << 8 | lowerChannelFreq;
        return 131072/(2048-x);
    }

    public byte read(int address) {
        int offset = address-startAddress;
        if(handlesSweep && offset == 0) { // NR10-like
            byte register = (byte) (sweepShiftCount & 0x07);
            if(sweepIncrease == -1)
                register |= 1<<3;
            register |= (sweepTime << 4) & 0x70;
            return register;
        } else if(offset == 1) { // NR11-like
            return (byte) ((patternDutty << 6) & 0xC);
        } else if(offset == 2) { // NR12-like
            byte register = (byte) (envelopeSweepCount & 0x7);
            if(envelopeDirection == 1)
                register |= 1 << 3;
            register |= (initialVolumeEnvelope << 4) & 0xF0;
            return register;
        } else if(offset == 3) { // NR13-like
            return (byte) lowerChannelFreq;
        } else if(offset == 4) { // NR14-like
            byte register = (byte) highChannelFreq;
            if(consecutiveSelection)
                register |= 1 << 6;
            if(restartSound)
                register |= 1 << 7;
            return register;
        }
        return 0;
    }
}
