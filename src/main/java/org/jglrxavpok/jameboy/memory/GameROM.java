package org.jglrxavpok.jameboy.memory;

import java.nio.ByteBuffer;

public class GameROM {
    private final ByteBuffer data;
    private final CartridgeHeader header;

    public GameROM(ByteBuffer data) {
        this.data = data;
        header = loadHeader();
    }

    private CartridgeHeader loadHeader() {
        data.mark();
        data.position(CartridgeHeader.HEADER_START);
        ByteBuffer slice = data.slice();
        slice.limit(CartridgeHeader.HEADER_END - CartridgeHeader.HEADER_START+1);
        CartridgeHeader header = new CartridgeHeader(slice);
        data.rewind();
        return header;
    }

    public CartridgeHeader getHeader() {
        return header;
    }

    public ByteBuffer getData() {
        return data;
    }
}
