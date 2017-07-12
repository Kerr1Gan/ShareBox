package com.ecjtu.sharebox.server.impl.servlet;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.ecjtu.easyserver.util.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.io.File;
/**
 * Created by KerriGan on 2017/7/12.
 */

public class Icon implements BaseServlet {

    private static String sPath="";

    public static void initPath(String path) {
        sPath=path;
    }

    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {
//        String uri = httpReq.getURI();
//        String filePaths = uri.substring("/API/Icon".length());
//        int indexOf = filePaths.indexOf("&");
//
//        if (indexOf != -1) {
//            filePaths = filePaths.substring(0, indexOf);
//        }

        try {
            File file = new File(sPath);

            long contentLen = file.length();

            String contentType = FileUtil.getFileType(sPath);

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
        doGet(httpReq,httpRes);
    }
}
