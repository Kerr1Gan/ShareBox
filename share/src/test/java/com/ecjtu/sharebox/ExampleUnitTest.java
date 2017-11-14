package com.ecjtu.sharebox;

import org.ecjtu.easyserver.server.DeviceInfo;
import org.ecjtu.easyserver.server.impl.servlet.Info;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testInfoServlet() throws Exception{
        //只能测试Java代码
        Map<String,List<String>> map=new LinkedHashMap<>();
        List<String> lst=new ArrayList<>();
        lst.add("/sdcard/test.mp4");
        lst.add("/sdcard/test2.mp4");
        map.put("Movie",lst);

        List<String> lst2=new ArrayList<>();
        lst2.add("/sdcard/test3.mp4");
        lst2.add("/sdcard/test4.mp4");
        map.put("Music",lst2);

        DeviceInfo info=new DeviceInfo("123","192.168.43.1",8080,"/icon",map);

        List list = (List)null;
    }
}