package org.ecjtu.easyserver.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by KerriGan in 2016.4.25
 */
public class FileUtil
{
	private static String type = "*/*";
	public static String ip = "127.0.0.1";
	public static String deviceDMRUDN = "0";
	public static String deviceDMSUDN = "0";
	public static int port = 0;
	
	
	public static String getFileType(String uri)
	{
		if (uri == null)
		{
			return type; 
		}
		
		if (uri.endsWith(".mp3"))
		{
			return "audio/mpeg"; 
		}
		
		if (uri.endsWith(".mp4"))
		{
			return "video/mp4"; 
		} 
		
		return type; 
	}
	
	public static String getDeviceDMRUDN()
	{
		return deviceDMRUDN;
	}
	
	public static String getDeviceDMSUDN()
	{
		return deviceDMSUDN;
	}
	

	public static boolean mkdir(String name)
	{
		boolean bool = false;
		
		boolean state = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); 
		if (state)
		{
			File f = Environment.getExternalStorageDirectory();
			String path = f.getPath(); 
			String dir = path+"/"+name+"/";
			File file = new File(dir);
			if (!file.exists())
			{
				  bool = file.mkdir(); 
			}
			else
			{
			}

		}
		else
		{
		}
		
		
		return bool; 
	}
	
	
	

}
