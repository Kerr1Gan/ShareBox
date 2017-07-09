package com.ecjtu.sharebox.server.impl.server;

import android.os.Environment;

import com.ecjtu.sharebox.server.impl.servlet.AssetsHtml;
import com.ecjtu.sharebox.server.impl.servlet.File;
import com.ecjtu.sharebox.server.impl.servlet.Html;

import org.ecjtu.easyserver.http.HTTPRequest;
import org.ecjtu.easyserver.http.HTTPRequestListener;
import org.ecjtu.easyserver.http.HTTPResponse;
import org.ecjtu.easyserver.http.HTTPServer;
import org.ecjtu.easyserver.http.HTTPServerList;
import org.ecjtu.easyserver.http.HTTPStatus;
import org.ecjtu.easyserver.net.HostInterface;
import org.ecjtu.easyserver.servlet.BaseServlet;
import org.ecjtu.easyserver.util.FileUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


/**
 * Created by KerriGan on 2016/4/22.
 */

//        example:
//        byte[]content=array.toString().getBytes("utf-8");
//        HTTPResponse httpRes=new HTTPResponse();
//        httpRes.setContentType("*/*");
//        httpRes.setStatusCode(HTTPStatus.OK);
//        httpRes.setContentLength(content.length);
//        httpRes.setContent(content);
//        httpReq.post(httpRes);


//        HTTPResponse httpRes = new HTTPResponse();
//        httpRes.setContentType(contentType);
//        httpRes.setStatusCode(HTTPStatus.OK);
//        httpRes.setContentLength(contentLen);
//        httpRes.setContentInputStream(contentIn);
//
//        httpReq.post(httpRes);
//
//        contentIn.close();

public class EasyServer extends Thread implements HTTPRequestListener {

    public static final String TAG = "mobilephone.sharefile.mobile.easyserver.server";

    private HTTPServerList mHttpServerList;

    public static final int DEFAULT_HTTP_PORT = 8000;

    private int HTTPPort = DEFAULT_HTTP_PORT;

    private String mBindIP = null;

    public static final String HOME_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/Share";

    public static final String HOME_PAGE = HOME_PATH + "/index.html";

    public static final String CONTENT_FILE_URI = "/File";

    public static final String CONTENT_API_URI = "/API";

    private HTTPServer mHttpServer;

    public static final int TYPE_AP = 1;

    public static final int TYPE_P2P = 1 << 1;

    private int mType = TYPE_AP;

    private static final String AP = "ap";

    private final int mMaxRetryTimes = 100;

    public String getBindIP() {
        return mBindIP;
    }

    public void setBindIP(String bindIP) {
        this.mBindIP = bindIP;
    }

    public HTTPServerList getHttpServerList() {
        return mHttpServerList;
    }

    @Deprecated
    public void setHttpServerList(HTTPServerList httpServerList) {
        this.mHttpServerList = httpServerList;
    }

    public int getHTTPPort() {
        return HTTPPort;
    }

    public void setHTTPPort(int hTTPPort) {
        HTTPPort = hTTPPort;
    }

    public void setType(int type) {
        mType = type;
    }

    @Override
    public void run() {
        super.run();

        int retryCnt = 0;

        int bindPort = getHTTPPort();

        if (mType == TYPE_AP) {
            HTTPServerList hsl = new HTTPServerList();
            setHttpServerList(hsl);
            while (hsl.open(bindPort, AP) == false) {
                retryCnt++;
                if (mMaxRetryTimes < retryCnt) {
                    return;
                }
                setHTTPPort(bindPort + 1);
                bindPort = getHTTPPort();
            }
            hsl.addRequestListener(this);
            hsl.start();
            FileUtil.ip = hsl.getHTTPServer(0).getBindAddress();
            FileUtil.port = hsl.getHTTPServer(0).getBindPort();

            setBindIP(FileUtil.ip);

            if (sListener != null) {
                if (!hsl.getHTTPServer(0).isOpened() || hsl.getHTTPServer(0).getServerSock().isClosed()
                        || FileUtil.port == 0)
                    sListener.ready(this, "", 0);
                else
                    sListener.ready(this, getBindIP(), getHTTPPort());


                sListener = null;
            }
        } else if (mType == TYPE_P2P) {
            mHttpServer = new HTTPServer();

            while (mHttpServer.open(getBindIP(), bindPort) == false) {
                retryCnt++;
                if (retryCnt > mMaxRetryTimes) {
                    return;
                }
                setHTTPPort(bindPort + 1);
                bindPort = getHTTPPort();
            }
            mHttpServer.addRequestListener(this);
            mHttpServer.start();

            setHTTPPort(bindPort);
            setBindIP(mHttpServer.getBindAddress());

            FileUtil.ip = mHttpServer.getBindAddress();
            FileUtil.port = mHttpServer.getBindPort();

            if (sListener != null) {
                if (!mHttpServer.isOpened() || mHttpServer.getServerSock().isClosed()
                        || getHTTPPort() == 0)
                    sListener.ready(this, "", 0);
                else
                    sListener.ready(this, getBindIP(), getHTTPPort());
                sListener = null;
            }
        }

    }

    @Override
    public void httpRequestReceived(HTTPRequest httpReq) {
        String uri = httpReq.getURI();

        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        httpReq.setURI(uri);
        HTTPResponse res = new HTTPResponse();
        res.setContentType("*/*");
        res.setStatusCode(HTTPStatus.OK);


        String reqMethod = "";

        if (uri.startsWith(CONTENT_API_URI)) {
            try {
                reqMethod = uri.substring(5);

                int last = reqMethod.indexOf("/", 0);

                if (reqMethod.indexOf("/") != -1) {
                    reqMethod = reqMethod.substring(0, last);
                }

            } catch (IndexOutOfBoundsException e) {
                System.out.println("请求错误api");
            }

            try {
                String packageName = Html.class.getPackage().getName();
                Class c = Class.forName(packageName + "." + reqMethod);

                BaseServlet base = (BaseServlet) c.newInstance();

                if (httpReq.isGetRequest())
                    base.doGet(httpReq, res);
                else if (httpReq.isPostRequest())
                    base.doPost(httpReq, res);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                httpReq.returnBadRequest();
            } catch (InstantiationException e) {
                e.printStackTrace();
                httpReq.returnBadRequest();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                httpReq.returnBadRequest();
            }
            return;
        } else if (uri.startsWith(CONTENT_FILE_URI)) {
            BaseServlet file = new File();
            file.doGet(httpReq, res);
            return;
        }

//        BaseServlet html=new Html();
        BaseServlet html = new AssetsHtml();

        html.doGet(httpReq, res);

    }

    @Override
    public void interrupt() {
        if (mHttpServer != null) {
            mHttpServer.stop();
            mHttpServer.close();
            mHttpServer = null;
        }

        if (mHttpServerList != null) {
            mHttpServerList.stop();
            mHttpServerList.close();
            mHttpServerList.clear();
            mHttpServerList = null;
        }
        super.interrupt();
    }

    public static HostInterface.ICallback sListener = null;

    /**
     * only if listener invoked , the listener will be erased.
     */
    public static void setServerListener(HostInterface.ICallback listener) {
        HostInterface.clearCallback();
        sListener = listener;
        HostInterface.addCallback(listener);
    }
}
