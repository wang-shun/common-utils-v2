package com.youzan.sz.jutil.nio.frame;

/**
 * 客户端数据写入器
 * @author meteorchen
 *
 */
public class ClientWriter implements Stage
{
	private ClientNet cn;

    public ClientWriter()
    {
    }

    public void setNext(ClientReader next)
    {
        this.cn = next.cn;
    }
    
    public void pushTask(Task task)
    {
        cn.changeToWriter(task);
    }
    
    public String toString()
    {
    	return "ClientWriter: " + cn.toString();
    }
}
