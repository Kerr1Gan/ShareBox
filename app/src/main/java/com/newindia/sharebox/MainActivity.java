package com.newindia.sharebox;

import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar= (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle=new ActionBarDrawerToggle(this,mDrawerLayout,mToolbar,0, 0);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        LinearLayout scroll= (LinearLayout) findViewById(R.id.content);
        for(int i=0;i<100;i++){
            Button b=new Button(this);
            b.setText("1234");
            b.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
            scroll.addView(b);
        }
    }
}
