package com.youzan.sz.jutil.string;

import java.io.Serializable;


/**
 * 字符串分页类
 * 提供方便的分页功能
 * @author stonexie
 *
 */
@SuppressWarnings("serial")
public class StringPage  implements Serializable
{
	/** 默认分页大小:{@value} */
	private static final int DEFAULT_PAGE_SIZE = 300;
	/** 当前页字符串 */
	private String content;
	/** 原始字符串总长度 */
	private int stringLenght;
	/** 分页每页大小 */
	private int pageSize;
	/** 当前分页的页码,页码从1开始 */
	private int pageNo;	
	
	/**
	 * 构造函数
	 * @param _string	要分页的字符串
	 * @param _pageSize	每页的字符数
	 * @param _pageNo	当前页页码
	 */
	public StringPage(String _string, int _pageSize, int _pageNo)
	{
		this.pageSize = ( _pageSize <= 0 ? DEFAULT_PAGE_SIZE : _pageSize);
		this.pageNo = ( _pageNo <= 0 ? 1 : _pageNo);		
		if( _string == null || _string.trim().length() == 0)
		{
			this.content = "";
			this.stringLenght = 0;
			return;
		}
		else
		{
			int begin = (this.pageNo - 1) * this.pageSize;
			int end = 	this.pageNo * this.pageSize;
			this.stringLenght = _string.length();
			if(begin >= _string.length())
			{
				this.content = "";
			}
			if(end >= _string.length())	
			{
				end = _string.length();
			}
			if(end <= begin)
			{
				this.content = "";
			}
			else
			{
				StringBuilder resultStr = new StringBuilder(_string);
				this.content = resultStr.substring(begin,end);
			}
		}			
	}
	
	/**
	 * 从字符串中取得某页字符串
	 * @param string 要分页的字符串
	 * @param pageSize 要分页的字符串
	 * @param pageNo 当前页页码
	 * @return String
	 */
	public static String getPageContent(String string, int pageSize, int pageNo)
	{
		return (new StringPage(string,pageSize,pageNo).getContent());
	}
	
	/**
	 * 取得当前页字符串
	 * @return String 当前页字符串
	 */
	public String getContent() 
	{
		return content;
	}

	/**
	 * 取得当前页页码
	 * @return int 当前页页码 
	 */
	public int getPageNo() 
	{
		return pageNo;
	}
	
	/**
	 * 取得原始字符串总分页数
	 * @return int
	 */
	public int getPageCount()
	{
		return (int) Math.ceil(this.stringLenght * 1.000 / this.pageSize);	
	}
	
	/**
	 * 取得分页大小
	 * @return int
	 */
	public int getPageSize() 
	{
		return pageSize;
	}

	/**
	 * 取得原始字符串总长度
	 * @return int
	 */
	public int getStringLenght() 
	{
		return stringLenght;
	}
	
	/**
	 * 当前面字符串长度
	 * @return int 
	 */
	public int getContentLenght()
	{
		return this.content.length();
	}
	
	/**
	 * 当前页是否有下页
	 * @return boolean
	 */
	public boolean hasNextPage()
	{
		return (this.pageNo < getPageCount());
	}
	
	/**
	 * 当前页是否有上一页
	 * @return boolean
	 */
	public boolean hasPrevPage()
	{
		return (this.pageNo > 1);
	}
}
