package org.jglrxavpok.jameboy.utils;

import java.nio.ByteBuffer;

public final class BitUtils {

    /**
     * Reads an ASCII String from the byte buffer.<br/>
     * <b>Important: </b> if the method finds a null character, it stops reading from the buffer. One should also be aware
     * that this increases the position of the byte buffer by <code>returnedText.length()</code>
     * @param buffer
     *          The buffer to read from
     * @param byteCount
     *          The maximum size, in bytes/characters of the text
     * @return
     *          The text
     */
    public static String readString(ByteBuffer buffer, int byteCount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < byteCount; i++) {
            byte value = buffer.get();
            if(value == 0)
                break;
            builder.append((char)value);
        }
        return builder.toString();
    }

    /**
     * Checks if the contents of two byte buffer are equal.
     * @param a
     *          The first buffer
     * @param b
     *          The second buffer
     * @return
     *          If the contents are equal, ie the limits are the same and two element at any given index are equal
     */
    public static boolean checkEqual(ByteBuffer a, ByteBuffer b) {
        if(a.limit() != b.limit())
            return false;
        for (int i = 0; i < a.limit(); i++) {
            if(a.get(i) != b.get(i))
                return false;
        }
        return true;
    }
}
