package com.ecjtu.sharebox.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Ethan_Xiang on 2017/11/1.
 */

public class ObjectUtil {

    @SuppressWarnings("unchecked")
    public static <T> T deepCopyOrThrow(T src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        return (T) in.readObject();
    }

    public static <T> T deepCopy(T src) {
        try {
            return deepCopyOrThrow(src);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }
}

