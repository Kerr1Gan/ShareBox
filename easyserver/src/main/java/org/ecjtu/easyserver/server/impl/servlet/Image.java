package org.ecjtu.easyserver.server.impl.servlet;

import android.content.Context;
import android.graphics.Bitmap;

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

public class Image implements BaseServlet {
    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {
        String uri = httpReq.getURI().toLowerCase();
        uri = uri.replace("/api/image", "");

        try {
            java.io.File file = new java.io.File(uri);

            long contentLen = file.length();

            String contentType = FileUtil.getFileType(uri);

            FileInputStream contentIn = new FileInputStream(file);

            if (contentLen <= 0 || contentType.length() <= 0) {
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
        } catch (IOException e) {
            httpReq.returnBadRequest();
        } catch (Exception e) {
            httpReq.returnBadRequest();
        }
    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {

    }
}
