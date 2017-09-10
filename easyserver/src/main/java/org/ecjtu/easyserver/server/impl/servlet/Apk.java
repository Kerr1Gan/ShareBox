package org.ecjtu.easyserver.server.impl.servlet;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.server.ServerManager;
import org.ecjtu.easyserver.server.util.ApkUtil;
import org.ecjtu.easyserver.server.util.cache.CacheUtil;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.ecjtu.easyserver.util.FileUtil;

import java.io.*;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Created by KerriGan on 2017/9/9.
 */

public class Apk implements BaseServlet {

    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {
        Context context = ServerManager.getInstance().getContext();
        String uri = httpReq.getURI().toLowerCase();
        uri = uri.replace("/api/apk", "");
        Log.e("ttttttt","uri "+uri);
        if (!CacheUtil.hasCache(context, uri)) {
            Bitmap thumb = ApkUtil.getAppThumbnail(context, new File(uri));
            CacheUtil.makeCache(uri, thumb, 150, 150, context);
        }
        try {
            String path = CacheUtil.getCachePath(context, uri);
            java.io.File file = new java.io.File(path);

            long contentLen = file.length();

            String contentType = FileUtil.getFileType(uri);

            FileInputStream contentIn = new FileInputStream(file);

            if (contentLen <= 0 || contentType.length() <= 0
                    || contentIn == null) {
                httpReq.returnBadRequest();
                return;
            }
            httpRes.setContentType(contentType);
            httpRes.setStatusCode(HTTPStatus.OK);
            httpRes.setContentLength(contentLen);
            httpRes.setContentInputStream(contentIn);
            httpReq.post(httpRes);

            contentIn.close();
        } catch (MalformedURLException e) {
            httpReq.returnBadRequest();
            return;
        } catch (IOException e) {
            httpReq.returnBadRequest();
            return;
        } catch (Exception e) {
            httpReq.returnBadRequest();
            return;
        }
    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {

    }
}
