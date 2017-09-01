package org.ecjtu.easyserver.server.util.cache;

import android.os.Parcel;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ethan_Xiang on 2017/8/21.
 * Parcel 存储在磁盘体积约为Serializable的4倍，而速度比Serializable快了约10至20倍
 */
abstract public class ParcelableFileCacheHelper extends FileCacheHelper {

    private ByteArrayOutputStream mByteArrayOutputStream;

    public ParcelableFileCacheHelper(String path) {
        super(path);
    }

    @Override
    public <T> T readObjectFromStream(FileInputStream is) throws IOException, ClassNotFoundException {
        mByteArrayOutputStream = new ByteArrayOutputStream();
        int len;
        byte[] buf = new byte[1024 * 1024];
        while ((len = is.read(buf)) > 0) {
            mByteArrayOutputStream.write(buf, 0, len);
        }
        Parcel parcel = Parcel.obtain();
        buf = mByteArrayOutputStream.toByteArray();
        parcel.unmarshall(buf, 0, buf.length);
        parcel.setDataPosition(0);

        //read parcel
        T ret = readParcel(parcel);

        parcel.recycle();
        gc();
        mByteArrayOutputStream.close();
        return ret;
    }

    @Override
    public <T> void writeObjectFromStream(FileOutputStream os, T object) throws IOException, ClassNotFoundException {
        Parcel parcel = writeParcel(Parcel.obtain(), object);
        os.write(parcel.marshall());
        parcel.recycle();
        gc();
    }

    @Override
    public <T> T readObject(String key) {
        T ret = super.readObject(key);
        try {
            if (mByteArrayOutputStream != null) {
                mByteArrayOutputStream.close();
                mByteArrayOutputStream = null;
            }
        } catch (IOException e) {
        }
        return ret;
    }

    private void gc() {
        System.gc();
        System.runFinalization();
        System.gc();
    }

    abstract <T> T readParcel(Parcel parcel);

    abstract <T> Parcel writeParcel(Parcel parcel, T object);
}
