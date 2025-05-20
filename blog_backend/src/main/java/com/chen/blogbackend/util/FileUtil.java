package com.chen.blogbackend.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

public class FileUtil {
    String key =  "";

    public static String calculateFileHash(String filePath ) throws NoSuchAlgorithmException, IOException {
        File file = new File(filePath);
        ByteSource byteSource = com.google.common.io.Files.asByteSource(file);
        KeyFactory aes = KeyFactory.getInstance("AES");
        byte[] read = byteSource.read();
        HashCode hashCode = HashCode.fromBytes(read);
        return hashCode.toString();
    }
}
