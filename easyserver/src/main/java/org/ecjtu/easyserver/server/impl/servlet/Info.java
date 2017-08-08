package org.ecjtu.easyserver.server.impl.servlet;

import android.text.TextUtils;

import org.ecjtu.easyserver.server.ConversionFactory;
import org.ecjtu.easyserver.server.DeviceInfo;
import org.ecjtu.easyserver.server.ServerManager;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private final String sToken="param=info";

    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse httpRes) {
        doPost(httpReq,httpRes);
    }

    @Override
    public void doPost(HTTPRequest httpReq, HTTPResponse httpRes) {
        DeviceInfo deviceInfo= ServerManager.getInstance().getDeviceInfo();

        if(deviceInfo==null){
            httpReq.returnResponse(HTTPStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        String formParam=null;
        try {
            formParam = new String(httpReq.getContent(), "utf-8");
            if(!TextUtils.isEmpty(formParam)){
                if(formParam.startsWith(sToken)){
                    JSONObject json= ConversionFactory.deviceInfo2Json(deviceInfo);
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

}
