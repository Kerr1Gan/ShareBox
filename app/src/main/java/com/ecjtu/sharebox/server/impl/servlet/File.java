package com.ecjtu.sharebox.server.impl.servlet;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.ecjtu.easyserver.util.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * Created by KerriGan on 2016/4/24.
 */
public class File implements BaseServlet {

    public static String TAG = "File";

    public static ArrayList<java.io.File> fileList = new ArrayList<>();

    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse res) {

        String uri = httpReq.getURI();
//        try
//        {
//            uri = URLDecoder.decode(uri, "UTF-8");
//        }
//        catch (UnsupportedEncodingException e1)
//        {
//            e1.printStackTrace();
//        }

        String filePaths = uri.substring(5);


        int indexOf = filePaths.indexOf("&");

        if (indexOf != -1) {
            filePaths = filePaths.substring(0, indexOf);
        }

        if (filePaths.startsWith("/"))
            filePaths = filePaths.substring(1);

        try {
            int index = filePaths.indexOf("/");
            if (index >= 0) {
                filePaths = filePaths.substring(index + 1);
            }

            filePaths = filePaths.substring(0, filePaths.indexOf("."));
        } catch (IndexOutOfBoundsException e) {
            httpReq.returnBadRequest();
            return;
        }


        try {
            filePaths = getFilePathByHash(Integer.valueOf(filePaths));
        } catch (Exception e) {
            e.printStackTrace();
            httpReq.returnBadRequest();
            return;
        }

        try {
            java.io.File file = new java.io.File(filePaths);

            long contentLen = file.length();

            String contentType = FileUtil.getFileType(filePaths);

            FileInputStream contentIn = new FileInputStream(file);

            if (contentLen <= 0 || contentType.length() <= 0
                    || contentIn == null) {
                httpReq.returnBadRequest();
                return;
            }


            HTTPResponse httpRes = new HTTPResponse();
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
    public void doPost(HTTPRequest req, HTTPResponse res) {

    }

    public static String getFilePathByHash(int hash) {
        if (fileList == null)
            return null;

        for (int i = 0; i < fileList.size(); i++) {
            java.io.File f = fileList.get(i);
            if (f.hashCode() == hash) {
                return f.getPath();
            }
        }
        return null;
    }

    public static int getFileHashByPath(String path) {
        if (fileList == null)
            return 0;

        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).getPath().compareTo(path) == 0)
                return fileList.get(i).hashCode();
        }

        return 0;
    }

    public static String getSuffixByPath(String path) {
        return path.substring(path.lastIndexOf("."));
    }

    public static void addFiles(ArrayList<java.io.File> files) {
        boolean hasSrc = false;

        if (fileList == null || files == null)
            return;

        for (int i = 0; i < files.size(); i++) {
            java.io.File fNew = files.get(i);
            for (int j = 0; j < fileList.size(); j++) {
                java.io.File fOld = fileList.get(j);

                if (fOld.getPath().compareTo(fNew.getPath()) == 0) {
                    hasSrc = true;
                    break;
                }
            }

            if (!hasSrc) {
                fileList.add(fNew);
            }

            hasSrc = false;
        }
    }
}
