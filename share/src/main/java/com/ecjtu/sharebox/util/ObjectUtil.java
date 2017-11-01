package com.ecjtu.sharebox.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by Ethan_Xiang on 2017/11/1.
 */

public class ObjectUtil {

    @SuppressWarnings("unchecked")
    public static <T> List<T> deepCopyOrThrow(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        return (List<T>) in.readObject();
    }

    public static <T> List<T> deepCopy(List<T> src) {
        try {
            return deepCopyOrThrow(src);
        } catch (Exception ignore) {
            return null;
        }
    }
}
