package com.ecjtu.sharebox.util.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Ethan_Xiang on 2017/7/21.
 * 该类可以跨进程使用
 */
public class FileCacheHelper {

    private ReentrantReadWriteLock mReentrantLock;

    private String mPath;

    private Lock mReadLock;

    private Lock mWriteLock;

    public FileCacheHelper(String path) {
        mPath = path;
        mReentrantLock = new ReentrantReadWriteLock();
        mReadLock = mReentrantLock.readLock();
        mWriteLock = mReentrantLock.writeLock();
    }

    public boolean put(String key, Object object) {
        boolean ret = false;
        try {
            mWriteLock.lockInterruptibly();
            ret = persistObject(key, object);
        } catch (InterruptedException e) {
            e.printStackTrace();
            ret=false;
        } finally {
            mWriteLock.unlock();
        }
        return ret;
    }

    public Object get(String key) {
        Object ret = null;
        try {
            mReadLock.lockInterruptibly();
            ret = readObject(key);
        } catch (InterruptedException e) {
            e.printStackTrace();
            ret=null;
        } finally {
            mReadLock.unlock();
        }
        return ret;
    }

    public boolean persistObject(String key, Object object) {
        File file = new File(mPath, key);
        if (file.exists()) file.delete();
        ObjectOutputStream os = null;
        FileOutputStream fos=null;
        boolean ret = false;
        FileLock fileLock=null;
        try {
            fos=new FileOutputStream(file);
            fileLock=fos.getChannel().lock();
            os = new ObjectOutputStream(fos);
            os.writeObject(object);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if(fileLock!=null){
                try {
                    fileLock.release();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
        return ret;
    }

    public Object readObject(String key) {
        File file = new File(mPath, key);
        if (!file.exists()) return null;

        ObjectInputStream is = null;
        Object ret = null;
        FileLock fileLock=null;
        FileInputStream fis=null;
        try {
            fis=new FileInputStream(file);
            fileLock=fis.getChannel().lock(0L, Long.MAX_VALUE, true);
            is = new ObjectInputStream(fis);
            ret = is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fileLock!=null){
                try {
                    fileLock.release();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return ret;
    }
}
