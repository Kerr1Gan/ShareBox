package org.ecjtu.easyserver.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by KerriGan on 2017/8/8.
 */

public class ConversionFactory {

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

    public static JSONObject files2TreeJson(File root, JSONObject json) throws JSONException {
        if (json == null) {
            json = new JSONObject();
            json.put("path", root.getAbsolutePath().replace("\\", "/"));
        }
        if (!root.exists()) return json;

        if (root.isDirectory()) {
            File[] listFile = root.listFiles();
            if (listFile != null) {
                json.put("name", root.getName());
                json.put("type", "dir");
                JSONArray arr = new JSONArray();
                for (File child : listFile) {
                    if (child.exists()) {
                        if (child.isDirectory()) {
                            JSONObject inner=new JSONObject();
                            inner = files2TreeJson(new File(child.getAbsolutePath()), inner);
                            arr.put(inner);
                        } else {
                            JSONObject typeFile = new JSONObject();
                            try {
                                typeFile.put("name", child.getName());
                                typeFile.put("type", "file");
                            } catch (JSONException e) {
                            }
                            arr.put(typeFile);
                        }
                    }
                }
                json.put("child",arr);
            }
        } else {
            json.put("name", root.getName());
            json.put("type", "file");
            return json;
        }
        return json;
    }
}
