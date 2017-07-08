package com.ecjtu.sharebox.server.impl.servlet;

import android.content.res.AssetManager;

import com.ecjtu.sharebox.util.fileutils.AssetsUtil;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by KerriGan on 2016/6/24.
 */
public class AssetsHtml implements BaseServlet {

    @Override
    public void doGet(HTTPRequest req, HTTPResponse httpRes) {
        String uri = req.getURI();


        String filePath = "";
        if (uri.length() <= 1) {
            filePath = "index.html";
        } else {
            filePath = filePath + uri;
        }


        BufferedInputStream contentIn = null;
        try {

            if (isExists(filePath) && isDirectory(filePath)) {
                if (uri.endsWith("index.html"))
                    return;
                if (!uri.endsWith("/"))
                    uri += "/";
                uri += "index.html";
                req.setURI(uri);
                doGet(req, httpRes);

                return;
            }

            if (filePath.startsWith("/"))
                filePath = filePath.substring(1);

            BufferedInputStream input = new BufferedInputStream(AssetsUtil.
                    getAssetsInputStreamByStreaming(AssetsUtil.CONTEXT, filePath));

            long contentLen = (long) input.available();

            contentIn = input;

            if (contentLen <= 0 || contentIn == null) {
//                req.returnBadRequest();
                return;
            }

            httpRes.setContentType("text/html");
            if (filePath.endsWith("css")) {
                httpRes.setContentType("text/css");
            } else if (filePath.endsWith("js")) {
                httpRes.setContentType("application/x-javascript");
            }


            httpRes.setHeader("Accept-Ranges", "bytes");
            httpRes.setStatusCode(HTTPStatus.OK);
            httpRes.setContentLength(contentLen);
            httpRes.setContentInputStream(contentIn);


            req.post(httpRes);

            contentIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {

    }

    protected boolean isExists(String path) throws IOException {

        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);

        String assetsPath = path;
        if (assetsPath.startsWith("/"))
            assetsPath = assetsPath.substring(1);

        int index = assetsPath.indexOf("/");

        if (index >= 0)
            assetsPath = assetsPath.substring(0, index);
        else
            assetsPath = "";

        AssetManager manager = AssetsUtil.CONTEXT.getAssets();
        String[] fileList = manager.list(assetsPath);

        for (int i = 0; i < fileList.length; i++) {
            if (path.endsWith(fileList[i])) {
                return true;
            }
        }
        return false;
    }

    protected boolean isDirectory(String path) {
        String assetsPath = path;
        if (assetsPath.startsWith("/"))
            assetsPath = assetsPath.substring(1);
        if (assetsPath.endsWith("/"))
            assetsPath = assetsPath.substring(0, assetsPath.length() - 1);

        AssetManager manager = AssetsUtil.CONTEXT.getAssets();
        try {
            String[] files = manager.list(assetsPath);

            if (files.length > 0)
                return true;
            else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
