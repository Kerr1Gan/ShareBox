package com.ecjtu.sharebox.server.impl.servlet;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Ke on 2016/4/24.
 */
public class GetFiles implements BaseServlet {

    public static ArrayList<java.io.File> _shareFileList;

    protected String _ip;

    public GetFiles() {
//        _ip= MainActivity.sHostIP;
    }

    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {

        return;
    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {

//        _shareFileList= AppInstance.getInstance().getSharingFileList();

        String formParam = "";
        try {
            formParam = new String(httpReq.getContent(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        boolean localShareScreen=AppInstance.getInstance().isShareScreen();

        if (httpReq.getHeaderValue("param").equals("getHttpFiles") || formParam.equals("param=getHttpFiles")) {

            try {
                if (_shareFileList != null) {
                    JSONArray array = new JSONArray();

                    JSONObject shareScreen = new JSONObject();
                    if (_ip == null)
                        _ip = "";
//                    shareScreen.put("path","rtsp://"+_ip+":"+MainActivity.sRtspPort);
//                    if(localShareScreen)
//                    {
//                        shareScreen.put("ShareScreen","true");
//                    }
//                    else
//                    {
//                        shareScreen.put("ShareScreen","false");
//                    }

                    array.put(shareScreen);

                    for (int i = 0; i < _shareFileList.size(); i++) {
                        java.io.File file = _shareFileList.get(i);
                        JSONObject obj = new JSONObject();

                        obj.put("name", file.getName());

//                        int hash= mobile.easyserver.servlet.File.getFileHashByPath(file.getPath());
//                        obj.put("path", "/File/" + hash + mobile.easyserver.servlet.File.getSuffixByPath(file.getName()));
//
//                        ImageElement e = new ImageElement();
//                        e.setKey(file.getPath());
//                        String cachePath="/API/Cache"+e.getCachePath(AppInstance.getInstance().getAppContext());
//
//                        String uri=cachePath.substring(cachePath.lastIndexOf("/")+1);
//
//                        uri= URLEncoder.encode(uri,"utf-8");
//
//                        cachePath=cachePath.substring(0,cachePath.lastIndexOf(("/"))+1);
//
//                        cachePath=cachePath+uri;
//
//                        obj.put("cachePath",cachePath);
//
//                        array.put(obj);
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
