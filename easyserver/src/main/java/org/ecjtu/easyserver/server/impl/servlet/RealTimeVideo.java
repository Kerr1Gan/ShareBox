package org.ecjtu.easyserver.server.impl.servlet;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.ecjtu.easyserver.util.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by KerriGan on 2016/6/30.
 */
public class RealTimeVideo implements BaseServlet {
    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {

        String uri = httpReq.getURI();

        String requestPath = uri.substring("/API/RealTimeVideo".length());


        try {
            java.io.File file = new java.io.File(requestPath);

            long contentLen = file.length();

            String contentType = FileUtil.getFileType(requestPath);

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

        return;

    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {

    }
}
