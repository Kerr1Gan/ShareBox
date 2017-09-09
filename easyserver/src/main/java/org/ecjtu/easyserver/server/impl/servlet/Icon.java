package org.ecjtu.easyserver.server.impl.servlet;

import org.ecjtu.easyserver.server.ServerManager;

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

    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {
//        String uri = httpReq.getURI();
//        String filePaths = uri.substring("/API/Icon".length());
//        int indexOf = filePaths.indexOf("&");
//
//        if (indexOf != -1) {
//            filePaths = filePaths.substring(0, indexOf);
//        }

        String path= ServerManager.getInstance().getIconPath();
        try {
            File file = new File(path);

            long contentLen = file.length();

            String contentType = FileUtil.getFileType(path);

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
