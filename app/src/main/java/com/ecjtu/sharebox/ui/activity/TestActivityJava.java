package com.ecjtu.sharebox.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.dd.CircularProgressButton;
import com.ecjtu.sharebox.R;

public class TestActivityJava extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_java);


//        Map<String,List<String>> map=new LinkedHashMap<>();
//        List<String> lst=new ArrayList<>();
//        lst.add("/sdcard/test.mp4");
//        lst.add("/sdcard/test2.mp4");
//        map.put("Movie",lst);
//
//        List<String> lst2=new ArrayList<>();
//        lst2.add("/sdcard/test3.mp4");
//        lst2.add("/sdcard/test4.mp4");
//        map.put("Music",lst2);
//        DeviceInfo info=new DeviceInfo("123","192.168.43.1",8080,"/icon",map);
//        JSONObject root=Info.deviceInfo2Json(info);
//        info=Info.json2DeviceInfo(root);

        final CircularProgressButton circularButton1 = (CircularProgressButton) findViewById(R.id.circularButton1);
        circularButton1.setIndeterminateProgressMode(true);
        circularButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circularButton1.getProgress() == 0) {
                    circularButton1.setProgress(50);
                } else if (circularButton1.getProgress() == 100) {
                    circularButton1.setProgress(0);
                } else {
                    circularButton1.setProgress(100);
                }
            }
        });
    }
}
