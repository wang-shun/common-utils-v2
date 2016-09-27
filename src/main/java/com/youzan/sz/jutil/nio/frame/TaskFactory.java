package com.youzan.sz.jutil.nio.frame;

import java.nio.channels.SocketChannel;

public interface TaskFactory
{
    /**
     * 创建一个Task
     * @param sc    与该Task关联的SocketChannel
     * @return
     */
    public Task createTask(SocketChannel sc);
}