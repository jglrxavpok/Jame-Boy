package org.jglrxavpok.jameboy;

import org.jglrxavpok.jameboy.memory.MemoryType;
import org.jglrxavpok.jameboy.memory.ROMOnlyHandler;

public class GBMemory
{

    private byte[] memory;

    private byte handlingOfCGB;

    private boolean compatibleSGB;

    private boolean oldVersion;

    private String title;

    private byte cartridgeType;

    private int romSize;

    private int ramSize;

    private int banks;

    private boolean japanese;

    private byte romVersion;

    private byte headerChecksum;

    private MemoryType memoryHandler;

    private int[] ram;
    private static final byte CGB_WORK_ALSO_OLD_GAMEBOY = (byte)0x80;
    private static final byte CGB_ONLY = (byte)0xC0;
    
    /**
     * In KB
     */
    private static final int[] romSizes = new int[]
            {
        32, 64,128,256,512,1024,2048,4096,8192,1126,1228,1536
            };
    
    private static final int[] ramSizes = new int[]
            {
        0,2,8,32
            };
    
    private static final int[] bankNumbers = new int[]
            {
        0,4,16,32,64,128,256,72,80,96
            };
    
    private static final byte[] NINTENDO_LOGO = new byte[]
            {
        (byte)0xCE,(byte)0xED,(byte)0x66,(byte)0x66,(byte)0xCC,(byte)0x0D,(byte)0x00,(byte)0x0B,(byte)0x03,(byte)0x73,(byte)0x00,(byte)0x83,(byte)0x00,(byte)0x0C,(byte)0x00,(byte)0x0D,
        (byte)0x00,(byte)0x08,(byte)0x11,(byte)0x1F,(byte)0x88,(byte)0x89,(byte)0x00,(byte)0x0E,(byte)0xDC,(byte)0xCC,(byte)0x6E,(byte)0xE6,(byte)0xDD,(byte)0xDD,(byte)0xD9,(byte)0x99,
        (byte)0xBB,(byte)0xBB,(byte)0x67,(byte)0x63,(byte)0x6E,(byte)0x0E,(byte)0xEC,(byte)0xCC,(byte)0xDD,(byte)0xDC,(byte)0x99,(byte)0x9F,(byte)0xBB,(byte)0xB9,(byte)0x33,(byte)0x3E
            };
    
    private static final int HEADER_OFFSET = 0x0100;
    
    public GBMemory()
    {
        memory = new byte[0xFFFF];
    }

    public void loadROM(byte[] rom)
    {
        byte[] header = getCartridgeHeader(rom);
        if(checkLogo(header, 0x104-HEADER_OFFSET))
        {
            System.out.println("Seems like a valid ROM: Nintendo logo is present");
            title = getString(header, 0x134-HEADER_OFFSET, 0x143-0x134);
            System.out.println("ROM title is: "+title);
            handlingOfCGB = rom[0x0143];
            oldVersion = handlingOfCGB != CGB_ONLY && handlingOfCGB != CGB_WORK_ALSO_OLD_GAMEBOY;
//            if(!oldVersion)
//            {
//                System.out.println(getString(header, 0x144-HEADER_OFFSET, 2));//0144-0145
//            }
            //LICENSEE CODE
//            else
//            {
//                System.out.println(getString(header, 0x014B-HEADER_OFFSET, 2));
//            }
            compatibleSGB = rom[0x0146] == 0x03;
            cartridgeType = rom[0x0147];
            romSize = romSizes[rom[0x0148]];
            ramSize = ramSizes[rom[0x0149]];
            banks = bankNumbers[rom[0x0148]];
            japanese = rom[0x014A] == 0x00;
            romVersion = rom[0x014C];
            headerChecksum = rom[0x014D];
            if(!validateHeaderChecksum(header, headerChecksum, 0x0134-HEADER_OFFSET, 0x014C-HEADER_OFFSET))
            {
                System.err.println("Well, not: Invalid header");
                return;
            }
            System.out.println(this.romToString());
            fillMemory(rom);
            switch(cartridgeType)
            {
                case 0x0:
                    memoryHandler = new ROMOnlyHandler();
                    break;

                default:
                    throw new IllegalArgumentException("Cartridge type not supported: "+getCartridgeTypeString(cartridgeType));
            }
            if(memoryHandler != null)
            {
                ram = new int[this.ramSize*1024];
                memoryHandler.init(rom, ram);
            }
        }
        else
        {
            System.out.println("Invalid ROM: Invalid Nintendo logo");
        }
    }
    
    private void fillMemory(byte[] rom)
    {
        int n = Math.min(memory.length, rom.length);
        for(int i = 0;i<n;i++)
        {
            memory[i] = rom[i];
        }
        for(int i = memory.length-n;i<memory.length;i++) // Clean up previous roms
        {
            memory[i] = 0x0;
        }
    }

    private boolean validateHeaderChecksum(byte[] header, byte checksum, int offset, int max)
    {
        int x = 0;
        for(int i = offset;i<=max;i++)
        {
            x -=header[i]+1;
        }
        byte b = (byte)((x)& 0xFF);
        return b == checksum;
    }

    public String romToString()
    {
        if(title == null)
            return "INVALID ROM";
        return "Valid ROM: {"+"\n"+
                "\t"+"Old ROM version: "+oldVersion+"\n"+
                "\t"+"Title: "+title+"\n"+
                "\t"+"Only for Game Boy Color: "+(handlingOfCGB == CGB_ONLY)+"\n"+
                "\t"+"Compatible SuperGameBoy: "+compatibleSGB+"\n"+
                "\t"+"Cartridge type: "+getCartridgeTypeString(cartridgeType)+"\n"+
                "\t"+"ROM size : "+romSize+"KB\n"+
                "\t"+"RAM size : "+ramSize+"KB\n"+
                "\t"+"RAM banks : "+banks+"\n"+
                "\t"+"Japanese only : "+japanese+"\n"+
                "\t"+"ROM version : "+(int)romVersion+"\n"+
                "}";
    }
    
    public String getCartridgeTypeString(byte cartridgeType)
    {
        switch(cartridgeType)
        {
            case 0x0:
            {
                return "ROM only";
            }
            case 0x01:
            {
                return "MBC1";
            }
            case 0x02:
            {
                return "MBC1+RAM";
            }
            case 0x03:
            {
                return "MBC1+RAM+BATTERY";
            }
            case 0x04:
            {
                return "INVALID";
            }
            case 0x05:
            {
                return "MBC2";
            }
            case 0x06:
            {
                return "MBC2+BATTERY";
            }
            case 0x07:
            {
                return "INVALID";
            }
            case 0x08:
            {
                return "ROM+RAM ";
            }
            case 0x09:
            {
                return "ROM+RAM+BATTERY";
            }
            case 0x0B:
            {
                return "MMM01";
            }
            case 0x0C:
            {
                return "MMM01+RAM";
            }
            case 0x0D:
            {
                return "MMM01+RAM+BATTERY";
            }
            case 0x0F:
            {
                return "MBC3+TIMER+BATTERY";
            }
            case 0x10:
            {
                return "MBC3+TIMER+RAM+BATTERY";    
            }
            case 0x11:
            {
                return "MBC3";
            }
            case 0x12:
            {
                return "MBC3+RAM";
            }
            case 0x13:
            {
                return "MBC3+RAM+BATTERY";
            }
            case 0x15:
            {
                return "MBC4";
            }
            case 0x16:
            {
                return "MBC4+RAM";
            }
            case 0x17:
            {
                return "MBC4+RAM+BATTERY";
            }
            case 0x19:
            {
                return "MBC5";
            }
            case 0x1A:
            {
                return "MBC5+RAM";
            }
            case 0x1B:
            {
                return "MBC5+RAM+BATTERY";
            }
            case 0x1C:
            {
                return "MBC5+RUMBLE";
            }
            case 0x1D:
            {
                return "MBC5+RUMBLE+RAM";
            }
            case 0x1E:
            {
                return "MBC5+RUMBLE+RAM+BATTERY";
            }
            case (byte)0xFC:
            {
                return "POCKET CAMERA";
            }
            case (byte)0xFD:
            {
                return "BANDAI TAMA5";
            }
            case (byte)0xFE:
            {
                return "HuC3";
            }
            case (byte)0xFF:
            {
                return "HuC1+RAM+BATTERY";
            }
        }
        return "INVALID";
    }
    
    private String getString(byte[] data, int offset, int length)
    {
        int max = offset+(length);
        StringBuffer buf = new StringBuffer();
        for(int i = offset;i<max;i++)
        {
            byte current = data[i];
            if(current == 0)
                break;
            buf.append((char)current);
        }
        return buf.toString();
    }
    
    private boolean checkLogo(byte[] rom, int offset)
    {
        for(int i = 0;i<NINTENDO_LOGO.length;i++)
        {
            try
            {
                if(rom[i+offset] != NINTENDO_LOGO[i])
                    return false;
            }
            catch(Exception e)
            {
                return false;
            }
        }
        return true;
    }

    public byte[] getCartridgeHeader()
    {
        return getCartridgeHeader(memory);
    }
    
    public byte[] getCartridgeHeader(byte[] romData)
    {
        byte[] header = new byte[0x014F-0x0100];
        for(int i = 0x100;i<0x14F;i++)
        {
            header[i-0x100] = romData[i];
        }
        return header;
    }
    
    public byte[] getRawMemory()
    {
        return memory;
    }

    public String getROMTitle()
    {
        return title;
    }

    public void write(int index, int value)
    {
        memoryHandler.write(index, value);
        System.out.println("[Jame Boy] Write value to RAM: "+Integer.toHexString(value)+" at index "+Integer.toHexString(index));
    }
    
    public int read(int index)
    {
        int value = memoryHandler.read(index);
        System.out.println("[Jame Boy] Read value from RAM: "+Integer.toHexString(value)+" at index "+Integer.toHexString(index));
        return value;
    }
}
