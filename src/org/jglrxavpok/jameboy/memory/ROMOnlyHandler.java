package org.jglrxavpok.jameboy.memory;


public class ROMOnlyHandler implements MemoryType
{

    private byte[] rom;
    private int[] ram;

    @Override
    public void write(int index, int value)
    {
        if (index >= 0xA000 && index <= 0xBFFF)
        {
            ram[index-0xA000] = value;
        }
        else 
        {
            // Invalid
            System.err.println("Invalid place to write: "+Integer.toHexString(index));
        }
    }

    @Override
    public int read(int index)
    {
        if(index < 0x8000 && index >= 0)
        {
            return rom[index];
        }
        else if(index >= 0xA000 && index < 0xBFFF)
        {
            return ram[index - 0xA000];
        }
        else
        {
            // Invalid ram place
            return 0;
        }
    }

    @Override
    public void init(byte[] rom, int[] ram)
    {
        this.rom = rom;
        this.ram = ram;
    }

}
