package com.ecjtu.sharebox.server.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by KerriGan on 2016/6/16.
 */
public class AssetsUtil {

    public static Context CONTEXT;

    public static InputStream getAssetsInputStream(Context context,String path,int mode) throws IOException {
        AssetManager manager=context.getAssets();

        return manager.open(path,mode);
    }

    public static InputStream getAssetsInputStreamByStreaming(Context context,String path)
            throws IOException
    {
        return getAssetsInputStream(context, path, AssetManager.ACCESS_STREAMING);
    }

    public static InputStream getAssetsInputStreamByBuffer(Context context,String path) throws IOException {
        return getAssetsInputStream(context,path,AssetManager.ACCESS_BUFFER);
    }

    public static InputStream getAssetsInputStreamByRandom(Context context,String path) throws IOException {
        return getAssetsInputStream(context,path,AssetManager.ACCESS_RANDOM);
    }

    public static InputStream getAssetsInputStreamByUnknown(Context context,String path) throws IOException {
        return getAssetsInputStream(context,path,AssetManager.ACCESS_UNKNOWN);
    }

}
