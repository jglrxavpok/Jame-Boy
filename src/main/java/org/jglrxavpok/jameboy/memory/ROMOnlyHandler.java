package org.jglrxavpok.jameboy.memory;

@Deprecated
public class ROMOnlyHandler implements MemoryController {

    private byte[] rom;
    private byte[] ram;

    @Override
    public void write(int index, byte value) {
        if (index >= 0xA000 && index <= 0xBFFF) {
            ram[index - 0xA000] = value;
        } else {
            // Invalid
            System.err.println("Invalid place to write: " + Integer.toHexString(index));
        }
    }

    @Override
    public byte read(int index) {
        if (index < 0x8000 && index >= 0) {
            return rom[index];
        } else if (index >= 0xA000 && index < 0xBFFF) {
            return ram[index - 0xA000];
        } else {
            // Invalid ram place
            return 0;
        }
    }

}
