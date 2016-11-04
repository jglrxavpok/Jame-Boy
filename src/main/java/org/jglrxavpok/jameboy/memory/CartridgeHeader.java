package org.jglrxavpok.jameboy.memory;

import org.jglrxavpok.jameboy.utils.BitUtils;
import org.jglrxavpok.jameboy.utils.Destinations;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class CartridgeHeader {

    public static final int HEADER_START = 0x0100;
    public static final int HEADER_END = 0x014F;
    /**
     * ROM sizes in kilobytes
     */
    public static final int[] ROM_SIZES = new int[55];
    static {
        for (int i = 0; i < 8; i++) {
            ROM_SIZES[i] = 32 << i;
        }

        ROM_SIZES[52] = 1100;
        ROM_SIZES[53] = 1200;
        ROM_SIZES[54] = 1500;
    }

    /**
     * RAM sizes in kilobytes
     */
    public static final int[] RAM_SIZES = new int[] { 0, 2, 8, 32 };

    private final ByteBuffer entryPoint;
    private final ByteBuffer logo;
    private final boolean validLogo;
    private final String title;
    private final String licensee;
    private final boolean supportsSGB;
    private final byte cartrigeType;
    private final int romSize;
    private final int ramSize;
    private final Destinations destination;
    private final byte gameVersion;
    private final byte headerChecksum;
    private final short globalChecksum;
    private byte headerSum;
    private boolean onlyCBG;
    private boolean backwardsCompatible;
    private String manufacturerCode = "";
    private boolean preCGB;

    public CartridgeHeader(ByteBuffer contents) {
        entryPoint = contents.slice();
        entryPoint.limit(4);
        contents.position(4);
        logo = contents.slice();
        logo.limit(0x0133-(HEADER_START+4) +1);
        validLogo = BitUtils.checkEqual(logo, createNintendoLogo());
        contents.position(0x134 - HEADER_START);
        ByteBuffer titleBuffer = contents.slice();
        titleBuffer.limit(0x10);
        title = BitUtils.readString(titleBuffer, 15);

        if(title.length() == 16) {
            preCGB = true;
        } else {
            if(title.length() == 11) {
                manufacturerCode = BitUtils.readString(titleBuffer, 4); // read remaining of buffer
            }

            byte cgbFlag = titleBuffer.get(15); // last byte
            backwardsCompatible = cgbFlag == (byte)0x80;
            onlyCBG = cgbFlag == (byte)0xC0;
        }

        byte oldLicensee = contents.get(0x14B - HEADER_START);
        if(oldLicensee == (byte)0x33) {
            contents.position(0x144 - HEADER_START);
            licensee = BitUtils.readString(contents, 2);
        } else {
            licensee = Integer.toHexString(oldLicensee & 0xFF);
        }

        supportsSGB = contents.get(0x146 - HEADER_START) == (byte)0x03;
        cartrigeType = contents.get(0x147 - HEADER_START);

        romSize = ROM_SIZES[contents.get(0x148 - HEADER_START)];
        ramSize = RAM_SIZES[contents.get(0x149 - HEADER_START)];

        destination = Destinations.values()[contents.get(0x14A - HEADER_START)];

        gameVersion = contents.get(0x14C - HEADER_START);

        headerChecksum = contents.get(0x14D - HEADER_START);
        int sum = 0;
        for (int i = 0x134; i <= 0x14C; i++) {
            sum -= contents.get(i - HEADER_START) +1;
        }
        headerSum = (byte) (sum & 0xFF);

        globalChecksum = contents.getShort(0x14E-HEADER_START);
    }

    public short getGlobalChecksum() {
        return globalChecksum;
    }

    public byte getHeaderChecksum() {
        return headerChecksum;
    }

    public byte getHeaderSum() {
        return headerSum;
    }

    public byte getGameVersion() {
        return gameVersion;
    }

    public Destinations getDestination() {
        return destination;
    }

    public int getROMSize() {
        return romSize;
    }

    public int getRAMSize() {
        return ramSize;
    }

    /**
     * Entry point of the cartridge
     * @return
     */
    public ByteBuffer getEntryPoint() {
        return entryPoint;
    }

    /**
     * Returns the cartridge type (contains a memory bank controller and/or additional hardware?)
     * @return
     */
    public byte getCartrigeType() {
        return cartrigeType;
    }

    public static String getCartrigeTypeName(byte cartridgeType) {
        switch (cartridgeType) {
            case (byte)0x00:
                return "ROM ONLY";
            case (byte)0x13:
                return "MBC3+RAM+BATTERY";
            case (byte)0x01:
                return "MBC1";
            case (byte)0x15:
                return "MBC4";
            case (byte)0x02:
                return "MBC1+RAM";
            case (byte)0x16:
                return "MBC4+RAM";
            case (byte)0x03:
                return "MBC1+RAM+BATTERY";
            case (byte)0x17:
                return "MBC4+RAM+BATTERY";
            case (byte)0x05:
                return "MBC2";
            case (byte)0x19:
                return "MBC5";
            case (byte)0x06:
                return "MBC2+BATTERY";
            case (byte)0x1A:
                return "MBC5+RAM";
            case (byte)0x08:
                return "ROM+RAM";
            case (byte)0x1B:
                return "MBC5+RAM+BATTERY";
            case (byte)0x09:
                return "ROM+RAM+BATTERY";
            case (byte)0x1C:
                return "MBC5+RUMBLE";
            case (byte)0x0B:
                return "MMM01";
            case (byte)0x1D:
                return "MBC5+RUMBLE+RAM";
            case (byte)0x0C:
                return "MMM01+RAM";
            case (byte)0x1E:
                return "MBC5+RUMBLE+RAM+BATTERY";
            case (byte)0x0D:
                return "MMM01+RAM+BATTERY";
            case (byte)0xFC:
                return "POCKET CAMERA";
            case (byte)0x0F:
                return "MBC3+TIMER+BATTERY";
            case (byte)0xFD:
                return "BANDAI TAMA5";
            case (byte)0x10:
                return "MBC3+TIMER+RAM+BATTERY";
            case (byte)0xFE:
                return "HuC3";
            case (byte)0x11:
                return "MBC3";
            case (byte)0xFF:
                return "HuC1+RAM+BATTERY";
            case (byte)0x12:
                return "MBC3+RAM";
        }
        return "UNKNOWN";
    }

    /**
     * Returns either a 2 characters licensee code or a hexadecimal string containing the licensee code
     * @return
     */
    public String getLicensee() {
        return licensee;
    }

    /**
     * Supports SGB functions?
     * @return
     */
    public boolean supportsSGB() {
        return supportsSGB;
    }

    /**
     * Manufacturer code, if any (empty string otherwise)
     * @return
     */
    public String getManufacturerCode() {
        return manufacturerCode;
    }

    /**
     * True if the game is only playable on a CGB
     * @return
     */
    public boolean isOnlyCBG() {
        return onlyCBG;
    }

    /**
     * True if the game is a CGB game but also works on a GB
     * @return
     */
    public boolean isBackwardsCompatible() {
        return backwardsCompatible;
    }

    /**
     * Was the game made before the CGB ?
     * @return
     */
    public boolean isPreCGB() {
        return preCGB;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Creates a new byte buffer with the binary form of the Nintendo logo
     * @return
     */
    public static ByteBuffer createNintendoLogo() {
        return ByteBuffer.wrap(new byte[] {
                (byte)0xCE, (byte)0xED, (byte)0x66, (byte)0x66, (byte)0xCC, (byte)0x0D, (byte)0x00, (byte)0x0B,
                (byte)0x03, (byte)0x73, (byte)0x00, (byte)0x83, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x0D,
                (byte)0x00, (byte)0x08, (byte)0x11, (byte)0x1F, (byte)0x88, (byte)0x89, (byte)0x00, (byte)0x0E,
                (byte)0xDC, (byte)0xCC, (byte)0x6E, (byte)0xE6, (byte)0xDD, (byte)0xDD, (byte)0xD9, (byte)0x99,
                (byte)0xBB, (byte)0xBB, (byte)0x67, (byte)0x63, (byte)0x6E, (byte)0x0E, (byte)0xEC, (byte)0xCC,
                (byte)0xDD, (byte)0xDC, (byte)0x99, (byte)0x9F, (byte)0xBB, (byte)0xB9, (byte)0x33, (byte)0x3E
        });
    }

    public boolean isLogoValid() {
        return validLogo;
    }
}
