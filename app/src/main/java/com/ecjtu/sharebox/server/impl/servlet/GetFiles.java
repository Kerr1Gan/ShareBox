package com.ecjtu.sharebox.server.impl.servlet;

import android.content.Context;

import com.ecjtu.sharebox.server.ServerManager;
import com.ecjtu.sharebox.util.cache.CacheUtil;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KerriGan on 2016/4/24.
 */
@Deprecated
public class GetFiles implements BaseServlet {

    public GetFiles() {
//        sIp= MainActivity.sHostIP;
    }

    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {
        return;
    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {

//        sShareFileList= AppInstance.getInstance().getSharingFileList();

        String formParam = "";
        try {
            formParam = new String(httpReq.getContent(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        boolean localShareScreen=AppInstance.getInstance().isShareScreen();
        boolean localShareScreen=false;
        if (httpReq.getHeaderValue("param").equals("getHttpFiles") || formParam.equals("param=getHttpFiles")) {
            List<java.io.File> fileList= ServerManager.getInstance().getSharedFileList();
            String ip=ServerManager.getInstance().getIp();
            try {
                if (fileList != null) {
                    JSONArray array = new JSONArray();

                    JSONObject shareScreen = new JSONObject();
                    if (ip == null)
                        ip = "";
                    shareScreen.put("path","rtsp://"+ ip +":"+/*MainActivity.sRtspPort*/8001);
                    if(localShareScreen)
                    {
                        shareScreen.put("ShareScreen","true");
                    }
                    else
                    {
                        shareScreen.put("ShareScreen","false");
                    }

                    array.put(shareScreen);
                    Context context=ServerManager.getInstance().getApplicationContext();
                    for (int i = 0; i < fileList.size(); i++) {
                        java.io.File file = fileList.get(i);
                        JSONObject obj = new JSONObject();

                        obj.put("name", file.getName());

                        int hash= File.getFileHashByPath(file.getPath());
                        obj.put("path", "/File/" + hash + File.getSuffixByPath(file.getName()));

                        String cachePath="/API/Cache"+ CacheUtil.getCachePath(context,file.getPath());

                        String uri=cachePath.substring(cachePath.lastIndexOf("/")+1);
                        uri= URLEncoder.encode(uri,"utf-8");
                        cachePath=cachePath.substring(0,cachePath.lastIndexOf(("/"))+1);
                        cachePath=cachePath+uri;
                        obj.put("cachePath",cachePath);
                        array.put(obj);
                    }

                    byte[] content = array.toString().getBytes("utf-8");
                    httpRes = new HTTPResponse();
                    httpRes.setContentType("*/*");
                    httpRes.setStatusCode(HTTPStatus.OK);
                    httpRes.setContentLength(content.length);
                    httpRes.setContent(content);
                    httpReq.post(httpRes);

                } else {
                    httpReq.returnOK();
                }


            } catch (IOException e) {
                httpReq.returnBadRequest();
                e.printStackTrace();
            } catch (JSONException e) {
                httpReq.returnBadRequest();
                e.printStackTrace();
            }
        }
    }

}
