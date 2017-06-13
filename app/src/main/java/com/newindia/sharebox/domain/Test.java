package com.newindia.sharebox.domain;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

/**
 * Created by KerriGan on 2017/6/2.
 */

public class Test {


    public Test(int x){
        setListener2(0, new IListener() {
            @Override
            public void onClick() {

            }
        });

        setListener1(new IListener() {
            @Override
            public void onClick() {

            }
        });
    }

    public Test(int x,int y,int z){

    }

    protected void add(){

    }

    public void setListener2(int x,IListener listener){
        listener.onClick();
    }
    public void setListener1(IListener listener){
        listener.onClick();
    }

    public static class Test2 extends Test{
        public Test2(int x){
            super(x);
            for(int i=0;i<1000;i++){

            }
        }

        public Test2(int x,int y,int z){
            super(x,y,z);
        }

        @Override
        protected void add() {
            super.add();
        }
    }

    public static class Test3 extends Test2{

        public Test3(int x) {
            super(x);
        }

        @Override
        protected void add() {
            super.add();
        }
    }

    private View.OnClickListener mOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private TextWatcher mWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public interface IListener{
        void onClick();
    }
}
