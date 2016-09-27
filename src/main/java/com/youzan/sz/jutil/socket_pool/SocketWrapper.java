package com.youzan.sz.jutil.socket_pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/**
 * Socket包装
 * @author meteorchen
 *
 */
public final class SocketWrapper
{
	long createTime;
	
	int useCount = 0;		// 已经被使用的次数
	
	Socket con;				// socket连接对象
	
	SocketPool sp;			// 所属连接池
	
	public SocketWrapper(Socket con, SocketPool sp)
	{
		this.con = con;
		this.sp = sp;
		// 随机初始化一个值，以避免大量链接同一时间关闭
		createTime = sp.getTimeRandomInit() + System.currentTimeMillis();
		useCount = sp.getUseCountRandomInit();
	}

	public void close()
	{
		if(sp != null){
			++useCount;
			if(sp.closeConnection(this))
				sp = null;
		}
	}
	
	public void closeReal()
	{
		if(sp != null){
			sp.closeReal(this);
			sp = null;
		}
	}

	public boolean equals(Object arg0)
	{
		SocketWrapper o = (SocketWrapper) arg0;
		if(o == null)
			return false;
		return con.equals(o.con);
	}

	public SocketChannel getChannel()
	{
		return con.getChannel();
	}

	public InetAddress getInetAddress()
	{
		return con.getInetAddress();
	}

	public InputStream getInputStream() throws IOException
	{
		return con.getInputStream();
	}

	public boolean getKeepAlive() throws SocketException
	{
		return con.getKeepAlive();
	}

	public InetAddress getLocalAddress()
	{
		return con.getLocalAddress();
	}

	public int getLocalPort()
	{
		return con.getLocalPort();
	}

	public SocketAddress getLocalSocketAddress()
	{
		return con.getLocalSocketAddress();
	}

	public boolean getOOBInline() throws SocketException
	{
		return con.getOOBInline();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return con.getOutputStream();
	}

	public int getPort()
	{
		return con.getPort();
	}

	public int getReceiveBufferSize() throws SocketException
	{
		return con.getReceiveBufferSize();
	}

	public SocketAddress getRemoteSocketAddress()
	{
		return con.getRemoteSocketAddress();
	}

	public boolean getReuseAddress() throws SocketException
	{
		return con.getReuseAddress();
	}

	public int getSendBufferSize() throws SocketException
	{
		return con.getSendBufferSize();
	}

	public int getSoLinger() throws SocketException
	{
		return con.getSoLinger();
	}

	public int getSoTimeout() throws SocketException
	{
		return con.getSoTimeout();
	}

	public boolean getTcpNoDelay() throws SocketException
	{
		return con.getTcpNoDelay();
	}

	public int getTrafficClass() throws SocketException
	{
		return con.getTrafficClass();
	}

	public int hashCode()
	{
		return con.hashCode();
	}

	public boolean isBound()
	{
		return con.isBound();
	}

	public boolean isClosed()
	{
		return con.isClosed();
	}

	public boolean isConnected()
	{
		return con.isConnected();
	}

	public boolean isInputShutdown()
	{
		return con.isInputShutdown();
	}

	public boolean isOutputShutdown()
	{
		return con.isOutputShutdown();
	}

	public void sendUrgentData(int arg0) throws IOException
	{
		con.sendUrgentData(arg0);
	}

	public void setKeepAlive(boolean arg0) throws SocketException
	{
		con.setKeepAlive(arg0);
	}

	public void setOOBInline(boolean arg0) throws SocketException
	{
		con.setOOBInline(arg0);
	}

	public void setPerformancePreferences(int arg0, int arg1, int arg2)
	{
		con.setPerformancePreferences(arg0, arg1, arg2);
	}

	public void setReceiveBufferSize(int arg0) throws SocketException
	{
		con.setReceiveBufferSize(arg0);
	}

	public void setReuseAddress(boolean arg0) throws SocketException
	{
		con.setReuseAddress(arg0);
	}

	public void setSendBufferSize(int arg0) throws SocketException
	{
		con.setSendBufferSize(arg0);
	}

	public void setSoLinger(boolean arg0, int arg1) throws SocketException
	{
		con.setSoLinger(arg0, arg1);
	}

	public void setSoTimeout(int arg0) throws SocketException
	{
		con.setSoTimeout(arg0);
	}

	public void setTcpNoDelay(boolean arg0) throws SocketException
	{
		con.setTcpNoDelay(arg0);
	}

	public void setTrafficClass(int arg0) throws SocketException
	{
		con.setTrafficClass(arg0);
	}

	public void shutdownInput() throws IOException
	{
		con.shutdownInput();
	}

	public void shutdownOutput() throws IOException
	{
		con.shutdownOutput();
	}

	public String toString()
	{
		return con.toString();
	}
	
}
