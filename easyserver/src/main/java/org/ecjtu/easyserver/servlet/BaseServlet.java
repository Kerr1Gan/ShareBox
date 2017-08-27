package org.ecjtu.easyserver.servlet;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPResponse;

/**
 * Created by KerriGan on 2016/4/22.
 */
public interface BaseServlet {

    void doGet(HTTPRequest httpReq,HTTPResponse httpRes);

    void doPost(HTTPRequest httpReq,HTTPResponse httpRes);

}
