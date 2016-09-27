package com.youzan.sz.jutil.j4log;

import java.util.Collection;


final class LogWorkThread implements Runnable 
{
	private boolean handleRemoteLog = false;
	
	public LogWorkThread()
	{
	}
	
	public LogWorkThread(boolean handleRemoteLog)
	{
		this.handleRemoteLog = handleRemoteLog;
	}
	
    public void run()
    {
        while(true)
        {
        	try {
        		Collection<Logger> coll = Logger.getLoggerMap().values();
                for( Logger logger : coll )
                {
                    if((logger.isRemoteLog() && handleRemoteLog)
                    		||(!logger.isRemoteLog() && !handleRemoteLog))
                    {
    	                try
    	                {
    	                    logger.doWriteLog();
    	                    
//    	                    Thread.sleep(50);       //每写完一个logger，sleep 50毫秒
    	                }
    	                catch(Exception e)
    	                {
    	                    e.printStackTrace();
    	                }
                    }
                }
                
                Thread.sleep(1000);    //每次sleep 1秒
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
}
