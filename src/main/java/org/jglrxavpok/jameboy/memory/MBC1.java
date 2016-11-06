package org.jglrxavpok.jameboy.memory;

import java.nio.ByteBuffer;

public class MBC1 extends BaseMemoryController {

    private final int ramBankCount;
    private final int romBankCount;
    private final ByteBuffer rom;
    private final ByteBuffer ram;
    private boolean enableRAM;
    private int currentROMBank;
    private byte lowBank;
    private byte highBank;
    private int currentRAMBank;
    private boolean inRamBankingMode;
    private int romOffset;

    public MBC1(int romSize, int ramSize, ByteBuffer rom, ByteBuffer ram) {
        switch (ramSize) {
            case 32:
                ramBankCount = 4;
                break;

            case 0:
                ramBankCount = 0;
                break;

            default:
                ramBankCount = 0;
                break;
        }

        switch (romSize) {
            case 0:
                romBankCount = 0;
                break;

            case 64:
                romBankCount = 4;
                break;

            case 128:
                romBankCount = 8;
                break;

            case 256:
                romBankCount = 16;
                break;

            case 512:
                romBankCount = 32;
                break;

            case 1024:
                romBankCount = 64;
                break;

            case 2048:
                romBankCount = 128;
                break;

            case 4096:
                romBankCount = 256;
                break;

            case 1152:
                romBankCount = 72;
                break;

            case 1280:
                romBankCount = 80;
                break;

            case 1536:
                romBankCount = 96;
                break;

            default:
                romBankCount = 1;
                break;
        }

        this.rom = rom;
        this.ram = ram;
    }

    public int getRamBankCount() {
        return ramBankCount;
    }

    public int getRomBankCount() {
        return romBankCount;
    }

    public boolean isRAMEnabled() {
        return enableRAM;
    }

    public int getCurrentROMBank() {
        return currentROMBank;
    }

    public int getCurrentRAMBank() {
        return currentRAMBank;
    }

    public boolean isInRamBankingMode() {
        return inRamBankingMode;
    }

    @Override
    public void write(int index, byte value) {
        super.write(index, value);
        if(index >= 0x0000 && index <= 0x1FFF) {
            enableRAM = (value & 0b00001111) == (byte)0xA;
        }

        if(index >= 0x2000 && index <= 0x3FFF) {
            if(value == 0x0) {
                value = 0x1; // the MBC translates the value 0 to 1, for some reason
            }
            lowBank = (byte) (value & 0b00011111);
            updateCurrentROMBank();
        }

        if(index >= 0x4000 && index <= 0x5FFF) {
            byte register = (byte) (value & 0b11);
            if(inRamBankingMode) {
                currentRAMBank = register & 0xFF;
            } else {
                highBank = register;
                updateCurrentROMBank();
            }
        }

        if(index >= 0x6000 && index <= 0x7FFF) {
            inRamBankingMode = value == 0x1;
        }

        if(index >= 0xA000 && index <= 0xBFFF) {
            int effectiveRAMBank = 0;
            if(inRamBankingMode)
                effectiveRAMBank = currentRAMBank;
            ram.put(effectiveRAMBank * 0x2000 + (index-0xA000), value);
        }
    }

    private void updateCurrentROMBank() {
        currentROMBank = highBank << 8 | lowBank;
        romOffset = currentROMBank * 0x4000;
    }

    @Override
    public byte read(int index) {
        if(index >= 0x0000 && index <= 0x3FFF) {
            return rom.get(index & 0x3FFF);
        }

        if(index >= 0x4000 && index <= 0x7FFF) {
            return rom.get(index & 0x3FFF + romOffset);
        }

        if(index >= 0xA000 && index <= 0xBFFF) {
            int effectiveRAMBank = 0;
            if(inRamBankingMode)
                effectiveRAMBank = currentRAMBank;
            return ram.get((index - 0xA000) + effectiveRAMBank * 0x2000);
        }
        return super.read(index);
    }

}
