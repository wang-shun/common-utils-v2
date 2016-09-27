package com.youzan.sz.jutil.jcache;

import com.youzan.sz.jutil.string.StringUtil;

import java.io.Serializable;
import java.util.Arrays;

//import com.qq.jutil.string.StringUtil;

/**
 * 
 * 当hash的key为多参数时，提供更快的所有方法 当参数中包含更多的数字时效果更好，如果参数全部为字符串，不建议使用该类
 * 如果使用2个从0开始的整数，比字符串相加快41%
 * 如果使用2个从0开始的整数，比字符串相加快45%
 * 
 * Serializable,为AdvCache提供便利
 * @author markzhang
 * 
 */
@SuppressWarnings("serial")
public class MapKey implements Serializable
{
	private Object[] paramters;
	private int hashCode;

	public MapKey(Object... c)
	{
		paramters = c;
	}

	public boolean equals(Object obj)
	{
		boolean result = false;
		if (obj != null && obj instanceof MapKey)
		{
			MapKey key = (MapKey) obj;
			result = Arrays.equals(key.paramters, this.paramters);
		}
		return result;
	}

	public int hashCode()
	{
		int h = hashCode;
		if (h == 0)
		{
			h = Arrays.hashCode(paramters);
			hashCode = h;
		}
		return h;
	}

	public static MapKey makeKey(Object... c)
	{
		return new MapKey(c);
	}
	
	//jcacheInfo.jsp和CacheInfo可能用到
	public static MapKey parse(String strKey)
	{
		String[] params = StringUtil.split(strKey, "|");
		Object[] objParams = new Object[params.length];
		for(int i = 0 ; i < params.length ; i++)
		{
			String param = params[i];
			if(param.matches("[\\d]+"))
			{
				objParams[i] = StringUtil.convertInt(param, 0);
			}
		}
		return new MapKey(objParams);
		
	}

	/**
	 * 更强大的语法解析能力,不支持嵌套
	 * <String>hello|<int>123|<long>123|<float>1.2|<double>1.3|<boolean>true
	 * @param strKey
	 * @return
	 */
	public static MapKey advParse(String strKey)
	{
		String[] params = StringUtil.split(strKey, "|");
		Object[] objParams = new Object[params.length];
		for(int i = 0 ; i < params.length ; i++)
		{
			String param = params[i];
			if(param.matches("^<\\s*int\\s*>.*"))
			{
				//int
				objParams[i] = StringUtil.convertInt(param.replaceAll("^<\\s*int\\s*>\\s*(\\d+)\\s*$", "$1"), 0);
			}
			else if(param.matches("^<\\s*long\\s*>.*"))
			{
				//long
				objParams[i] = StringUtil.convertLong(param.replaceAll("^<\\s*long\\s*>\\s*(\\d+)\\s*$", "$1"), 0);				
			}
			else if(param.matches("^<\\s*float\\s*>.*"))
			{
				//float
				objParams[i] = StringUtil.convertFloat(param.replaceAll("^<\\s*float\\s*>\\s*([\\d\\.]+)\\s*$", "$1"), 0);				
			}
			else if(param.matches("^<\\s*double\\s*>.*"))
			{
				//double
				objParams[i] = StringUtil.convertDouble(param.replaceAll("^<\\s*double\\s*>\\s*([\\d\\.]+)\\s*$", "$1"), 0);				
			}
			else if(param.matches("^<\\s*boolean\\s*>.*"))
			{
				//boolean
				System.out.println(param.replaceAll("^<\\s*boolean\\s*>\\s*(.*?)\\s*$", "$1"));
				objParams[i] = StringUtil.convertBoolean(param.replaceAll("^<\\s*boolean\\s*>\\s*(.*?)\\s*$", "$1"), false);				
			}
			else if(param.matches("^<\\s*String\\s*>.*"))
			{
				//String
				objParams[i] = param.replaceAll("^<\\s*String\\s*>\\s*(.*?)\\s*$", "$1");				
			}
			else
			{
				//String
				objParams[i] = param;								
			}
			
		}		
		return new MapKey(objParams);		
	}
	
	
	public Object[] getParamters()
	{
		if (paramters != null)
		{
			//为了避免paramters的值被修改,浅克隆
			return paramters.clone();
		}
		else
		{
			return null;
		}
	}

	public String toString()
	{
		return Arrays.toString(paramters);
	}
	
	/**
	 * 转换成描述的字符串
	 * @return
	 */
	public String toDescString()
	{
		//
		StringBuilder build = new StringBuilder();
		if(paramters != null)
		{
			for(int i = 0 ; i < paramters.length && paramters.length > 0 ; i++)
			{
				Object o = paramters[i];
				if(i != 0)
				{
					build.append("|");
				}
				if(o instanceof Integer)
				{
					build.append("<int>").append(o);					
				}
				else if(o instanceof Long)
				{
					build.append("<long>").append(o);										
				}
				else if(o instanceof Float)
				{
					build.append("<float>").append(o);										
				}
				else if(o instanceof Double)
				{
					build.append("<double>").append(o);										
				}
				else if(o instanceof Boolean)
				{
					build.append("<boolean>").append(o);										
				}
				else 
				{
					build.append("<String>").append(o);															
				}				
			}
		}
		return build.toString();
	}
	
	public static void main(String[] args)
	{
		MapKey m = MapKey.advParse("<String> hello |<int>123|<long>123|<float>1.2|<double>1.3|<boolean>true");
		System.out.println(m.toDescString());
	}
}
