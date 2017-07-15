package com.ecjtu.sharebox.server.impl.servlet;

import android.content.Context;

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

/**
 * Created by KerriGan on 2016/4/24.
 */
@Deprecated
public class GetFiles implements BaseServlet {

    public static ArrayList<java.io.File> sShareFileList;

    protected static String sIp;

    public GetFiles() {
//        sIp= MainActivity.sHostIP;
    }

    public static void init(String ip, ArrayList<java.io.File> fileList, Context context){
        sIp=ip;
        sShareFileList=fileList;
        File.sContext=context;
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

            try {
                if (sShareFileList != null) {
                    JSONArray array = new JSONArray();

                    JSONObject shareScreen = new JSONObject();
                    if (sIp == null)
                        sIp = "";
                    shareScreen.put("path","rtsp://"+ sIp +":"+/*MainActivity.sRtspPort*/8001);
                    if(localShareScreen)
                    {
                        shareScreen.put("ShareScreen","true");
                    }
                    else
                    {
                        shareScreen.put("ShareScreen","false");
                    }

                    array.put(shareScreen);

                    for (int i = 0; i < sShareFileList.size(); i++) {
                        java.io.File file = sShareFileList.get(i);
                        JSONObject obj = new JSONObject();

                        obj.put("name", file.getName());

                        int hash= File.getFileHashByPath(file.getPath());
                        obj.put("path", "/File/" + hash + File.getSuffixByPath(file.getName()));

                        String cachePath="/API/Cache"+ CacheUtil.getCachePath(File.sContext,file.getPath());

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
