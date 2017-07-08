/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HTTPServer.java
 *
 *	Revision;
 *
 *	12/12/02
 *		- first revision.
 *	10/20/03
 *		- Improved the HTTP mobile.easyserver.server using multithreading.
 *	08/27/04
 *		- Changed accept() to set a default timeout, HTTP.DEFAULT_TIMEOUT, to the socket.
 *	
 ******************************************************************/

package org.ecjtu.easyserver.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.ecjtu.easyserver.util.Debug;
import org.ecjtu.easyserver.util.ListenerList;


/**
 * mobile.easyserver.server<br>
 * The mobile.easyserver.server must be initialized iether by the
 * {@link HTTPServer#open(InetAddress, int)} or the
 * {@link HTTPServer#open(String, int)} method.<br>
 * Optionally a set of {@link HTTPRequestListener} may be set<br>
 * The mobile.easyserver.server then can be started or stopped by the method
 * {@link HTTPServer#start()} and {@link HTTPServer#stop()}
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.8
 * 
 */
public class HTTPServer implements Runnable
{
	private final static String tag = "HTTPServer";
	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////

	public final static String NAME = "EasyServerHTTP";
	public final static String VERSION = "1.0";

	public final static int DEFAULT_PORT = 80;

	/**
	 * @since 1.8
	 */
	public final static int DEFAULT_TIMEOUT = 15 * 1000;

	public static String getName()
	{

		String osName = System.getProperty("os.name");

		String osVer = System.getProperty("os.version");
		return osName + "/" + osVer + " " + NAME + "/" + VERSION;
	}

	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	public HTTPServer()
	{
		serverSock = null;

	}

	// //////////////////////////////////////////////
	// ServerSocket
	// //////////////////////////////////////////////

	/** ServerSocket serverSock */
	private ServerSocket serverSock = null;
	/** InetAddress bindAddr ��ַ */
	private InetAddress bindAddr = null;

	private int bindPort = 0;
	/**
	 * variable should be accessed by getter and setter metho
	 */
	protected int timeout = DEFAULT_TIMEOUT;


	public ServerSocket getServerSock()
	{
		return serverSock;
	}


	public String getBindAddress()
	{
		if (bindAddr == null)
		{
			return "";
		}
		return bindAddr.getHostAddress();
	}


	public int getBindPort()
	{
		return bindPort;
	}

	// //////////////////////////////////////////////
	// open/close
	// //////////////////////////////////////////////

	/**
	 * Get the current socket timeout
	 * 
	 * @since 1.8
	 */
	public synchronized int getTimeout()
	{
		return timeout;
	}

	/**
	 * Set the current socket timeout
	 *
	 *            new timeout
	 * @since 1.8
	 */
	public synchronized void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}


	public boolean open(InetAddress addr, int port)
	{
		if (serverSock != null)
			return true;
		try
		{
			serverSock = new ServerSocket(bindPort, 0, bindAddr);
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}


	public boolean open(String addr, int port)
	{
		if (serverSock != null)
		{
			return true;
		}
		try
		{
			bindAddr = InetAddress.getByName(addr);
			bindPort = port;
			serverSock = new ServerSocket(bindPort, 0, bindAddr);
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}


	public boolean close()
	{
		if (serverSock == null)
		{
			return true;
		}
		try
		{
			serverSock.close();
			serverSock = null;
			bindAddr = null;
			bindPort = 0;
		}
		catch (Exception e)
		{
			Debug.warning(e);
			return false;
		}
		return true;
	}

	public Socket accept()
	{
		if (serverSock == null)
			return null;
		try
		{

			Socket sock = serverSock.accept();

			sock.setSoTimeout(getTimeout());
			return sock;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public boolean isOpened()
	{
		return (serverSock != null) ? true : false;
	}

	// //////////////////////////////////////////////
	// httpRequest
	// //////////////////////////////////////////////


	private ListenerList httpRequestListenerList = new ListenerList();


	public void addRequestListener(HTTPRequestListener listener)
	{
		httpRequestListenerList.add(listener);
	}


	public void removeRequestListener(HTTPRequestListener listener)
	{
		httpRequestListenerList.remove(listener);
	}


	public void performRequestListener(HTTPRequest httpReq)
	{
		int listenerSize = httpRequestListenerList.size();
		for (int n = 0; n < listenerSize; n++)
		{
			HTTPRequestListener listener = (HTTPRequestListener) httpRequestListenerList
					.get(n);
		 
			
			listener.httpRequestReceived(httpReq);
		}
	}

	// //////////////////////////////////////////////
	// run
	// //////////////////////////////////////////////

	private Thread httpServerThread = null;

	@Override
	public void run()
	{
		if (isOpened() == false)
		{
			return;
		}

		Thread thisThread = Thread.currentThread();

		while (httpServerThread == thisThread)
		{

			Thread.yield();
			Socket sock;
			try
			{
				Debug.message("accept ...");
				
				sock = accept();

				if (sock != null)
				{
					Debug.message("sock = " + sock.getRemoteSocketAddress());
					/**
					 *  add in 2016/4/24 by KerriGan,fixed in 2017.2.6
					 */
					sock.setSoTimeout(3000);
					HTTPServerThread httpServThread = new HTTPServerThread(this, sock);
					httpServThread.start();
				}
			}
			catch (Exception e)
			{ 
				break;
			}
			Debug.message("httpServThread ...");
		}
	}

	public boolean start()
	{
		StringBuffer name = new StringBuffer("EasyServer.HTTPServer/");
		name.append(serverSock.getLocalSocketAddress());
		httpServerThread = new Thread(this, name.toString());
		httpServerThread.start();
		return true;
	}

	/** httpServerThread */
	public boolean stop()
	{
		httpServerThread = null;
		return true;
	}
}
