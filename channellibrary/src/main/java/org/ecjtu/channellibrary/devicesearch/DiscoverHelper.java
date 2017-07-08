package org.ecjtu.channellibrary.devicesearch;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.ecjtu.channellibrary.wifiutil.NetworkUtil;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by KerriGan on 2017/6/20 0020.
 */


public class DiscoverHelper{

    public static final int MSG_FIND_DEVICE=0x1001;

    public static final int MSG_BEING_SEARCHED=0x1002;

    public static final int MSG_START_FIND_DEVICE=0x1003;

    public static final int MSG_START_BEING_SEARCHED=0x1004;

    public static class SimpleHandler extends Handler{

        private WeakReference<DiscoverHelper> mHost;

        public SimpleHandler(DiscoverHelper host){
            mHost=new WeakReference<DiscoverHelper>(host);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mHost.get()==null) return;
            IMessageListener listener=mHost.get().getMessageListener();
            if(listener==null) return;

            listener.message(msg.what, (Set<DeviceSearcher.DeviceBean>) msg.obj,this);
        }
    }

    private Context mContext;

    private String mName;

    private DeviceSearcher mSearcher=null;

    private DeviceWaitingSearch mWaitingSearch=null;

    private Handler mHandler=new SimpleHandler(this);

    private IMessageListener mMsgListener;

    private String mPort="";

    private String mIcon="";

    public DiscoverHelper(Context context,String name,String port,String icon){
        mContext=context;
        mName =name;
        mPort=port;
        mIcon=icon;
        prepare(mContext,true,true);
    }

    public void prepare(Context context,boolean restartWaiting,boolean restartSearcher){
        if(restartWaiting){
            if(mWaitingSearch!=null) mWaitingSearch.interrupt();
            mWaitingSearch=new DeviceWaitingSearch(context,mName+","+mPort+","+mIcon,"") {
                @Override
                public void onDeviceSearched(InetSocketAddress socketAddr,String port) {
                    Set<DeviceSearcher.DeviceBean> set=new HashSet<>();
                    DeviceSearcher.DeviceBean bean=new DeviceSearcher.DeviceBean();
                    String ip=NetworkUtil.intToIp(NetworkUtil.byteArrayToInt(socketAddr.getAddress().getAddress()));
                    bean.setIp(ip);
                    bean.setPort(socketAddr.getPort());
                    bean.setName(port);
                    set.add(bean);
                    mHandler.obtainMessage(MSG_BEING_SEARCHED,set).sendToTarget();
                }
            };
        }

        if(restartSearcher){
            if(mSearcher!=null) mSearcher.interrupt();
            mSearcher=new DeviceSearcher(mName+","+mPort+","+mIcon) {
                @Override
                public void onSearchStart() {

                }

                @Override
                public void onSearchFinish(Set<DeviceBean> deviceSet) {
                    mHandler.obtainMessage(MSG_FIND_DEVICE,deviceSet).sendToTarget();
                }
            };
        }
    }

    public void start(boolean waiting,boolean search){
        if(waiting) mWaitingSearch.start();
        if(search) mSearcher.start();
    }

    public void stop(boolean waiting,boolean search){
        if(waiting) mWaitingSearch.interrupt();
        if(search) mSearcher.interrupt();
    }

    public void setMessageListener(IMessageListener listener){
        mMsgListener=listener;
    }

    public IMessageListener getMessageListener(){
        return mMsgListener;
    }

    public void release(){
        if(mSearcher!=null) mSearcher.interrupt();
        if(mWaitingSearch!=null) mWaitingSearch.interrupt();
        mHandler.removeCallbacksAndMessages(null);
        mHandler=null;
        mSearcher=null;
        mWaitingSearch=null;
    }

    public interface IMessageListener{
        /**
         * @see #MSG_FIND_DEVICE
         * @see #MSG_BEING_SEARCHED
         * @see #MSG_START_FIND_DEVICE
         * @see #MSG_START_BEING_SEARCHED
         * @param msg
         * @param deviceSet
         * @param handler
         */
        void message(int msg, Set<DeviceSearcher.DeviceBean> deviceSet,Handler handler);
    }

}
