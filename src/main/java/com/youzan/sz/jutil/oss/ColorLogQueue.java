package com.youzan.sz.jutil.oss;

import java.util.concurrent.BlockingQueue;

public class ColorLogQueue {
	private static BlockingQueue<ColorLogItem> queue;
	
	public static class ColorLogItem{
		DataForColor data;
		String log;
		public ColorLogItem(DataForColor data,String log){
			this.data = data;
			this.log = log;
		}
		public DataForColor getColorForData(){
			return data;
		}
		public String getLog (){
			return log;
		}
	}
	
	public static boolean offer(ColorLogItem logItem){
		if( queue==null )
			return false;
		return queue.offer(logItem);
	}
	
	public static void setQueue( BlockingQueue<ColorLogItem> itemQueue ){
		queue = itemQueue;
	}
}
