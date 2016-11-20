package org.jglrxavpok.jameboy.audio;

public class SoundController {

    private final ToneSweepSoundChannel channel1;
    private final ToneSweepSoundChannel channel2;
    private final WaveOutputChannel channel3;
    private final NoiseChannel channel4;

    public SoundController() {
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
        return 0;
    }
}
