package org.ecjtu.easyserver.util;

import java.net.Socket;
import java.util.ArrayList;

import org.ecjtu.easyserver.http.HTTPRequest;

/**
 * Created by KerriGan on 2016/4/24.
 */
public class HttpManagement {

    public static String TAG="[HttpManagement]";


    private static HttpManagement _management;

    private ArrayList<HTTPRequest> _httpReqList;

    private HttpManagement()
    {
        _httpReqList=new ArrayList<>();
    }

    public static HttpManagement getInstance()
    {
        if(_management==null)
            _management=new HttpManagement();

        return _management;
    }

    public ArrayList<HTTPRequest> getHttpReqList()
    {
        return _httpReqList;
    }

    public void Debug()
    {
        System.out.println("=============================================HttpManagementDebug Begin");
        for(int i=0;i<_httpReqList.size();i++)
        {
            HTTPRequest req=_httpReqList.get(i);
            System.out.println(req.getFirstLineString());
        }
        System.out.println("=============================================HttpManagementDebug End");
    }



}
