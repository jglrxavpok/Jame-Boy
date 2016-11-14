package org.jglrxavpok.jameboy.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtils {

    public static byte[] read(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        byte[] buffer = new byte[8*1024];
        while ((i = stream.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, i);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }
}
