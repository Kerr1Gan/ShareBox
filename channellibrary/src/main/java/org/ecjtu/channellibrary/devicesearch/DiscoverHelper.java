package org.ecjtu.channellibrary.devicesearch;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.net.InetSocketAddress;
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
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private Context mContext;

    private String mData;


    private DeviceSearcher mSearcher=null;


    private DeviceWaitingSearch mWaitingSearch=null;

    private Handler mHandler=new SimpleHandler();

    public DiscoverHelper(Context context,String name){
        mContext=context;
        mData=name;
        prepare(mContext,mData,true,true);
    }

    protected void prepare(Context context,String name,boolean restartWaiting,boolean restartSearcher){
        if(restartWaiting){
            if(mWaitingSearch!=null) mWaitingSearch.interrupt();
            mWaitingSearch=new DeviceWaitingSearch(context,name,"") {
                @Override
                public void onDeviceSearched(InetSocketAddress socketAddr) {

                }
            };
        }

        if(restartSearcher){
            if(mSearcher!=null) mSearcher.interrupt();
            mSearcher=new DeviceSearcher() {
                @Override
                public void onSearchStart() {

                }

                @Override
                public void onSearchFinish(Set<DeviceBean> deviceSet) {

                }
            };
        }
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
