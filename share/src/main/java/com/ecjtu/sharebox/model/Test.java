package com.ecjtu.sharebox.model;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import java.io.IOError;
import java.io.IOException;

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

        String[] strs=new String[2];
        String[] strs2=new String[]{"123","321"};


        try {
            int xx=0/0;
        }catch (NumberFormatException | IOError e2){

        }

        int j=0;
        for(int i=0;i<10;i++){
            j++;
            if(j==2){
                i--;
                j=0;
            }
        }

        Integer iii=0;

        if(iii instanceof Integer){

        }
    }

    public <T extends String> void  addAll(T param){

    }

    public <T> void  addAll2(T param){

    }

    public Test(int x,int y,int z){

    }

    protected void add() throws IOException{

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
            try {
                super.add();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
