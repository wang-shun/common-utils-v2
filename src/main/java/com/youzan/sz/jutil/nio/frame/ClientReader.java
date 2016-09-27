package com.youzan.sz.jutil.nio.frame;

import java.io.IOException;

/**
 * 客户端数据读取器
 * @author meteorchen
 * @version
 */
public class ClientReader implements Stage
{
	ClientNet cn;

    public ClientReader(String serverIp, int port, TaskFactory tf)
    {
        this.cn = new ClientNet(serverIp, port, tf);
    }
    
    public ClientReader(String serverIp, int port, TaskFactory tf, long selectTimeout)
    {
        this.cn = new ClientNet(serverIp, port, tf, selectTimeout);
    }
    
    public void setNext(Stage next)
    {
    	cn.setHandleStage(next);
        //this.next = next;
    }

    public void pushTask(Task task)
    {
        cn.addToReader(task);
    }

    public void startServer() throws IOException
    {
        cn.startServer();
    }
    
    public String toString()
    {
    	return "ClientReader: " + cn.toString();
    }
}
