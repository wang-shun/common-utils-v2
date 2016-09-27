package com.youzan.sz.jutil.config;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 配置文件读取类
 * @author meteor
 *
 */
public class XMLConfigFile
{
	private Document doc;
	
	/**
	 * 解析配置文件
	 * @param is	文件输入流
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void parse(InputStream is) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		doc = docBuilder.parse(is);
	}
	
	/**
	 * 获取根节点
	 * @return	根节点
	 */
	public XMLConfigElement getRootElement()
	{
		Element root = doc.getDocumentElement();
		return new XMLConfigElement(root);
	}
}
