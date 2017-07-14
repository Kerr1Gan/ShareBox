package com.ecjtu.sharebox.server.impl.servlet;

import android.text.TextUtils;

import com.ecjtu.sharebox.domain.DeviceInfo;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by KerriGan on 2017/7/8.
 */

public class Info implements BaseServlet{

    private static DeviceInfo sDeviceInfo=null;

    private final String sToken="param=info";

    public static void init(DeviceInfo info){
        sDeviceInfo=info;
    }

    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {
        doPost(httpReq,httpRes);
    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {
        if(sDeviceInfo==null){
            httpReq.returnResponse(HTTPStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        String formParam=null;
        try {
            formParam = new String(httpReq.getContent(), "utf-8");
            if(!TextUtils.isEmpty(formParam)){
                if(formParam.startsWith(sToken)){
                    JSONObject json=deviceInfo2Json(sDeviceInfo);
                    String jsonStr=json.toString();
                    httpRes.setContentType("*/*");
                    httpRes.setStatusCode(HTTPStatus.OK);
                    httpRes.setContentLength(jsonStr.length());
                    httpRes.setContent(jsonStr);
                    httpReq.post(httpRes);
                }
            }else{
                httpReq.returnResponse(HTTPStatus.BAD_REQUEST);
                return;
            }
        } catch (UnsupportedEncodingException e) {
        } catch (Exception e){
            //ignore
            httpReq.returnResponse(HTTPStatus.BAD_REQUEST);
        }
    }

    public static JSONObject deviceInfo2Json(DeviceInfo info){
        try {
            JSONObject root=new JSONObject();
            root.put("name",String.valueOf(info.getName().toCharArray()));
            root.put("ip",String.valueOf(info.getIp().toCharArray()));
            root.put("port",String.valueOf(info.getPort()));
            root.put("icon",String.valueOf(info.getIcon().toCharArray()));

            JSONArray arr=new JSONArray();
            Map<String,List<String>> map=info.getFileMap();
            Set set=map.keySet();
            for(Object k:set){
                String key= (String) k;
                List<String> list=map.get(key);
                JSONObject element=new JSONObject();
                JSONArray child=new JSONArray();
                for(int i=0;i<list.size();i++){
                    child.put(i,list.get(i));
                }
                element.put(key,child);
                arr.put(element);
            }
            root.put("arr",arr);
            return root;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DeviceInfo json2DeviceInfo(JSONObject json){
        try {
            String name= json.getString("name");
            String ip= json.getString("ip");
            int port = Integer.valueOf(json.getString("port"));
            String icon=json.getString("icon");

            Map<String,List<String>> map=new LinkedHashMap<>();
            JSONArray array=json.getJSONArray("arr");
            for(int i=0;i<array.length();i++){
                JSONObject child=array.getJSONObject(i);
                String key=child.keys().next();
                JSONArray list=child.getJSONArray(key);
                List<String> fileList=new ArrayList<>();
                for(int j=0;j<list.length();j++){
                    fileList.add(list.getString(j));
                }
                map.put(key,fileList);
            }

            DeviceInfo info=new DeviceInfo(name,ip,port,icon,map);
            return info;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
