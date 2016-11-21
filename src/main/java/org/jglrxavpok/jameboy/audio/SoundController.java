package org.jglrxavpok.jameboy.audio;

import java.nio.ByteBuffer;

public class SoundController {

    private final ToneSweepSoundChannel channel1;
    private final ToneSweepSoundChannel channel2;
    private final WaveOutputChannel channel3;
    private final NoiseChannel channel4;
    private final ByteBuffer wavePatternRAM;

    public SoundController() {
        wavePatternRAM = ByteBuffer.allocate(0xFF3F-0xFF30 +1);
        channel1 = new ToneSweepSoundChannel(0xFF10, true);
        channel2 = new ToneSweepSoundChannel(0xFF15, false);
        channel3 = new WaveOutputChannel(0xFF1A);
        channel4 = new NoiseChannel(0xFF1A);
    }

    public void write(int address, byte value) {
        if(channel1.isValid(address))
            channel1.write(address, value);
        else if(channel2.isValid(address))
            channel2.write(address, value);
        else if(channel3.isValid(address))
            channel3.write(address, value);
        else if(channel4.isValid(address))
            channel4.write(address, value);
        else if(address >= 0xFF30 && address <= 0xFF3F) {
            wavePatternRAM.put(address - 0xFF30, value);
        }
    }

    public byte read(int address) {
        if(channel1.isValid(address))
            return channel1.read(address);
        else if(channel2.isValid(address))
            return channel2.read(address);
        else if(channel3.isValid(address))
            return channel3.read(address);
        else if(channel4.isValid(address))
            return channel4.read(address);
        else if(address >= 0xFF30 && address <= 0xFF3F) {
            return wavePatternRAM.get(address - 0xFF30);
        }
        return 0;
    }

    public boolean isValid(int address) {
        return channel1.isValid(address) || channel2.isValid(address) || channel3.isValid(address) || channel4.isValid(address)
                || (address >= 0xFF30 && address <= 0xFF3F);
    }
}
