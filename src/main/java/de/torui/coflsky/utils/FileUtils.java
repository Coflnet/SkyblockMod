package de.torui.coflsky.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;

public class FileUtils {

    public static byte[] createChecksum(File file) throws Exception {
        InputStream fis = Files.newInputStream(file.toPath());
        byte[] buffer = IOUtils.toByteArray(fis);
        fis.close();
        MessageDigest complete = MessageDigest.getInstance("MD5");
        complete.update(buffer,0, buffer.length);
        return complete.digest();
    }


    public static String getMD5Checksum(File file) throws Exception {
        byte[] b = createChecksum(file);
        StringBuilder result = new StringBuilder();

        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }


}
