package com.youzan.sz.jutil.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import com.youzan.sz.jutil.common.CongfigResource;
import com.youzan.sz.jutil.string.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import com.qq.jutil.common.CongfigResource;
//import com.qq.jutil.string.StringUtil;

/**
 * 配置文件节点类
 * @author meteor
 *
 */
public class XMLConfigElement
{
	private Element e;
	
	XMLConfigElement(Element e)
	{
		this.e = e;
	}
	
	public boolean isValid()
	{
		return e != null;
	}
	
	/**
	 * 获取字符串属性
	 * @param name	属性名
	 * @return
	 */
	public String getStringAttribute(String name)
	{
		return e != null ? e.getAttribute(name) : null;
	}
	
	/**
	 * 获取字符串属性
	 * @param name	属性名
	 * @param def	默认值
	 * @return
	 */
	public String getStringAttribute(String name, String def)
	{
		String s = getStringAttribute(name);
		return s != null && s.length() > 0 ? s : def;
	}
	
	/**
	 * 判断属性是否存在
	 * @param name	属性名
	 * @return
	 */
	public boolean isExistAttribute(String name)
	{
		return getStringAttribute(name) != null;
	}
	
	/**
	 * 获取布尔型属性
	 * @param name	属性名
	 * @param def	默认值
	 * @return
	 */
	public boolean getBooleanAttribute(String name, boolean def)
	{
		String s = getStringAttribute(name);
		if("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
    		return true;
    	if("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s))
    		return false;
		return StringUtil.convertBoolean(s, def);
	}
	
	/**
	 * 获取整型属性值
	 * @param name	属性名
	 * @param def	默认值
	 * @return
	 */
	public int getIntAttribute(String name, int def)
	{
		String s = getStringAttribute(name);
		return StringUtil.convertInt(s, def);
	}
	
	/**
	 * 获取double类型的属性值
	 * @param name	属性名
	 * @param def	默认值
	 * @return
	 */
	public double getDoubleAttribute(String name, double def)
	{
		String s = getStringAttribute(name);
		return StringUtil.convertDouble(s, def);
	}
	
	/**
	 * 获取long型的属性值
	 * @param name	属性名
	 * @param def	默认值
	 * @return
	 */
	public long getLongAttribute(String name, long def)
	{
		String s = getStringAttribute(name);
		return StringUtil.convertLong(s, def);
	}
	
	/**
	 * 获取short型的属性
	 * @param name	属性名
	 * @param def	默认值
	 * @return
	 */
	public short getShortAttribute(String name, short def)
	{
		String s = getStringAttribute(name);
		return StringUtil.convertShort(s, def);
	}
	
	/**
	 * 获取float型的属性
	 * @param name	属性名
	 * @param def	默认值
	 * @return
	 */
	public float getFloatAttribute(String name, float def)
	{
		String s = getStringAttribute(name);
		return StringUtil.convertFloat(s, def);
	}
	
	/**
	 * 获取节点名
	 * @return
	 */
	public String getName()
	{
		return e != null ? e.getNodeName() : null;
	}
	
	/**
	 * 获取节点的内容
	 * @return
	 */
	public String getContent()
	{
		return e != null ? e.getTextContent() : null;
	}
	
	/**
	 * 按照名字获取第一个子节点
	 * @param name	字节点的名字
	 * @return
	 */
	public XMLConfigElement getChildByName(String name)
	{
		if(e == null)
			return this;
		NodeList nl = e.getElementsByTagName(name);
		for(int i = 0; i < nl.getLength(); ++i){
			Node n = nl.item(i);
			Element ee = (Element) n;
			if(ee != null)
				return new XMLConfigElement(ee);
		}
		return new XMLConfigElement(null);
	}
	
	/**
	 * 按照名字获取字节点列表
	 * @param name	字节点名字
	 * @return
	 */
	public ArrayList<XMLConfigElement> getChildListByName(String name)
	{
		ArrayList<XMLConfigElement> list = new ArrayList<XMLConfigElement>();
		if(e == null)
			return list;
		NodeList nl = e.getElementsByTagName(name);
		for(int i = 0; i < nl.getLength(); ++i){
			Node n = nl.item(i);
			Element ee = (Element) n;
			if(ee != null)
				list.add(new XMLConfigElement(ee));
		}
		return list;
	}
	
	/**
	 * 获取字节点列表
	 * @return
	 */
	public ArrayList<XMLConfigElement> getChildList()
	{
		ArrayList<XMLConfigElement> list = new ArrayList<XMLConfigElement>();
		if(e == null)
			return list;
		NodeList nl = e.getChildNodes();
		for(int i = 0; i < nl.getLength(); ++i){
			Node n = nl.item(i);
			if(n instanceof Element){
				Element ee = (Element) n;
				list.add(new XMLConfigElement(ee));
			}
		}
		return list;
	}
	
	public Map<String, String> getAttributeMap() {
		Map<String, String> map = new HashMap<String, String>();
		if (e == null)
			return map;
		NamedNodeMap attributes = e.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node n = attributes.item(i);
			String name = n.getNodeName();// 获得属性名
			String value = n.getNodeValue();// 获得属性值
			map.put(name, value);
		}
		return map;
	}
	
	/*
	public int getEnumAttribute(String name, String[] ns, int[] vs, int def)
	{
		String value = getStringAttribute(name);
		if(value == null || ns == null || vs == null || ns.length != vs.length)
			return def;
		for(int i = 0; i < ns.length; ++i){
			if(value.equals(ns[i]))
				return vs[i];
		}
		return def;
	}
	*/
	public static void main(String[] argv) throws SAXException, IOException, ParserConfigurationException{
	
		InputStream in = CongfigResource.loadConfigFile("cacheconfig.xml", XMLConfigElement.class);
		XMLConfigFile cfg = new XMLConfigFile();
		cfg.parse(in);
		XMLConfigElement root = cfg.getRootElement();
		ArrayList<XMLConfigElement> cs = root.getChildList();
		
		for(int i = 0; i < cs.size(); ++i){
			System.out.println(cs.get(i).getName());
			/*
			XMLConfigElement cleanup_strategy = cs.get(i).getChildByName("cleanup_strategy");
			String[] ns = {"create_time", "last_access_time"};
			int[] vs = {2, 3};
			int n = cleanup_strategy.getEnumAttribute("value", ns, vs, -1);
			System.out.println(n);
			*/
		}
	}
}
