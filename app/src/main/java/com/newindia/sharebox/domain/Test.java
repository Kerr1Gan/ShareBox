package com.newindia.sharebox.domain;

/**
 * Created by KerriGan on 2017/6/2.
 */

public class Test {


    public Test(int x){

    }

    public Test(int x,int y,int z){

    }

    protected void add(){

    }

    public static class Test2 extends Test{
        public Test2(int x){
            super(x);
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


}
