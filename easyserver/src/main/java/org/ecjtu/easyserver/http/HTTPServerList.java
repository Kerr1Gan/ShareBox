/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HTTPServerList.java
 *
 *	Revision;
 *
 *	05/08/03
 *		- first revision.
 *	24/03/06
 *		- Stefano Lenzi:added debug information as request by Stephen More
 *
 ******************************************************************/

package org.ecjtu.easyserver.http;

import java.net.InetAddress;
import java.util.Vector;

import org.ecjtu.easyserver.net.HostInterface;


public class HTTPServerList extends Vector<HTTPServer>
{
	
	
	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	private InetAddress[] binds = null;

	private int port = 4004;

	private String mIpName;

	public HTTPServerList()
	{
	}

	/**
	 * @param list
	 * @param port
	 * */
	public HTTPServerList(InetAddress[] list, int port)
	{
		this.binds = list;
		this.port = port;
	}

	// //////////////////////////////////////////////
	// Methods
	// //////////////////////////////////////////////

	public void addRequestListener(HTTPRequestListener listener)
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.addRequestListener(listener);
		}
	}

	public HTTPServer getHTTPServer(int n)
	{
		return (HTTPServer) get(n);
	}

	// //////////////////////////////////////////////
	// open/close
	// //////////////////////////////////////////////

	public void close()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.close();
		}
	}

	public int open()
	{
		InetAddress[] binds = this.binds;
		String[] bindAddresses;
		int indexAP=-1;

		if (binds != null)
		{
			bindAddresses = new String[binds.length];
			for (int i = 0; i < binds.length; i++)
			{
				bindAddresses[i] = binds[i].getHostAddress(); 
			}
		}
		else
		{
			int nHostAddrs = HostInterface.getNHostAddresses();

			bindAddresses = new String[nHostAddrs];
			StringBuffer name=new StringBuffer();
			for (int n = 0; n < nHostAddrs; n++)
			{
				bindAddresses[n] = HostInterface.getHostAddress(n,name);
				if(mIpName!=null){
					if(name.toString().contains(mIpName)||
							name.toString().contains(mIpName)){
						indexAP=n;
					}
				}
			}
		}
		// System.out.println("=======================================");
		//
		// for(int i=0;i<bindAddresses.length;i++){
		// System.out.println("bindAddresses["+i+"]="+bindAddresses[i]);
		// }

		int j = 0;
//		old version deprecated in 2017.2.3
//		for (int i = 0; i < bindAddresses.length; i++)
//		{
//			HTTPServer httpServer = new HTTPServer();
//			if ((bindAddresses[i] == null)
//					|| (httpServer.open(bindAddresses[i], port) == false))
//			{
//				close();
//				clear();
//			}
//			else
//			{
//				add(httpServer);
//				j++;
//			}
//		}
		for (int i = 0; i < bindAddresses.length; i++) {
			HTTPServer httpServer = new HTTPServer();

			if(indexAP!=-1) i=indexAP;

			if ((bindAddresses[i] == null)
					|| (httpServer.open(bindAddresses[i], port) == false)) {
				close();
				clear();
			} else {
				add(httpServer);
				j++;
			}

			if(indexAP!=-1) break;
		}
		return j;
	}

	public boolean open(int port)
	{
		this.port = port;
		return open() != 0;
	}

	public boolean open(int port,String ipName)
	{
		mIpName=ipName;
		return open(port);
	}

	// //////////////////////////////////////////////
	// start/stop
	// //////////////////////////////////////////////

	public void start()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.start();
		}
	}

	public void stop()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.stop();
		}
	}

}
