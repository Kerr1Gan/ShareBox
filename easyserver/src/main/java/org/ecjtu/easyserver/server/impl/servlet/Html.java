package org.ecjtu.easyserver.server.impl.servlet;

import org.ecjtu.easyserver.server.impl.server.EasyServer;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Created by KerriGan on 2016/4/24.
 */
public class Html implements BaseServlet {


    public static String CONTENT_EXPORT_URI = "/Html";

    @Override
    public void doGet(HTTPRequest req, HTTPResponse res) {
        String uri = req.getURI();


        String filePath = EasyServer.HOME_PATH;
        if (uri.length() <= 1) {
            filePath = EasyServer.HOME_PAGE;
        } else {
            filePath = filePath + uri;
        }

//        Date date=new Date(System.currentTimeMillis());
//        System.out.println(date+"[Html]"+filePath);


        FileInputStream contentIn = null;
        try {
            File file = new File(filePath);

            if (file.exists() && file.isDirectory()) {
//                System.out.println(date+"[Html]"+"文件不存在，尝试index.html");
                if (uri.endsWith("index.html"))
                    return;
                if (!uri.endsWith("/"))
                    uri += "/";
                uri += "index.html";
                req.setURI(uri);
                doGet(req, res);
            }

            long contentLen = (long) file.length();

            contentIn = new FileInputStream(file);

            if (contentLen <= 0 || contentIn == null) {
//                req.returnBadRequest();
                return;
            }

            res.setContentType("text/html");
            if (file.getName().endsWith("css")) {
                res.setContentType("text/css");
            } else if (file.getName().endsWith("js")) {
                res.setContentType("application/x-javascript");
            }

            ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();

            int size = 0;
            int len = 0;
            byte[] bytes = new byte[1024];
            while (size < contentLen) {
                if (contentLen - size < 1024) {
                    len = contentIn.read(bytes, 0, (int) (contentLen - size));
                    contentBytes.write(bytes, 0, (int) (contentLen - size));
                    size += len;
                    continue;
                }
                len = contentIn.read(bytes, 0, 1024);
                contentBytes.write(bytes, 0, 1024);
                size += len;
            }

            res.setHeader("Accept-Ranges", "bytes");
            res.setStatusCode(HTTPStatus.OK);
            res.setContentLength(contentLen);
            res.setContent(contentBytes.toByteArray());

//            Date date1=new Date(System.currentTimeMillis());
//            System.out.println(date1 +"开始发送"+file.getPath()+"[Html]");
            req.post(res);
//            System.out.println(date1 + "结束" + file.getPath() + "[Html]");

            contentBytes.close();
            contentIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HTTPRequest req, HTTPResponse res) {
        return;
    }
}
