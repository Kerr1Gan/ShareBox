package org.ecjtu.easyserver.util;

import java.io.Serializable;

public class FileItem implements Serializable
{
	private String name = "";
	private String path = "/";
	private boolean isFile = false;
	
	
	
	
	public FileItem()
	{
		super();
	}

    


	public FileItem(String name, String path, boolean isFile)
	{
		super();
		this.name = name;
		this.path = path;
		this.isFile = isFile;
	} 

	public String getName()
	{
		return name;
	}




	public void setName(String name)
	{
		this.name = name;
	}




	public String getPath()
	{
		return path;
	}




	public void setPath(String path)
	{
		this.path = path;
	}




	public boolean isFile()
	{
		return isFile;
	}




	public void setFile(boolean isFile)
	{
		this.isFile = isFile;
	}




	@Override
	public String toString()
	{ 
		return name;
	}

}
