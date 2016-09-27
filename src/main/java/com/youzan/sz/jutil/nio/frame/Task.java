package com.youzan.sz.jutil.nio.frame;

import java.nio.channels.SocketChannel;

/**
 * 任务接口
 * @author meteorchen
 *
 */
public interface Task
{
    /**
     * 获取该Task关联的SocketChannel
     * @return  该Task关联的SocketChannel
     */
    public SocketChannel getChannel();

    /**
     * 请求是否已经接收完毕
     */
    public boolean isReadFinish();

    /**
     * 从客户端读取数据
     * @return  返回读取到的字符个数，0表示连接已经关闭，负数表示读取失败（或发生错误）
     */
    public int readFromClient();

    /**
     * 返回数据是否写入完成
     * @return
     */
    public boolean isWriteFinish();

    /**
     * 写入返回数据
     * @return  返回写入的字符个数，负数表示写入失败
     */
    public int writeToClient();

    /**
     * 当一个请求处理完成后，框架会调用该方法清除临时状态，然后再继续处理下一个请求
     *
     */
    public void reset();
}