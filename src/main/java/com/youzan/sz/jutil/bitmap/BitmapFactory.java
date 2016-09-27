package com.youzan.sz.jutil.bitmap;

import com.youzan.sz.jutil.admin.AdminCommand;
import com.youzan.sz.jutil.admin.AdminServer;
import com.youzan.sz.jutil.common.CongfigResource;
import com.youzan.sz.jutil.config.XMLConfigElement;
import com.youzan.sz.jutil.config.XMLConfigFile;
import com.youzan.sz.jutil.crypto.HexUtil;
import com.youzan.sz.jutil.string.StringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

//import com.qq.jutil.admin.AdminCommand;
//import com.qq.jutil.admin.AdminServer;
//import com.qq.jutil.common.CongfigResource;
//import com.qq.jutil.config.XMLConfigElement;
//import com.qq.jutil.config.XMLConfigFile;
//import com.qq.jutil.crypto.HexUtil;
//import com.qq.jutil.string.StringUtil;

/**
 * Bitmap的工厂方法，允许一个Service中可存储多个Bitmap，只要你容量够-_-!
 * @author sunnyin
 * @create 2009-9-14
 */
public class BitmapFactory
{
	private static Map<String, Bitmap> BITMAP_MAP = new HashMap<String, Bitmap>();
	private static String adminIp = "127.0.0.1";
	private static int adminPort = 40015;
	private static AdminServer server;
	static
	{
		boolean fileExist = true;
		XMLConfigFile xf = new XMLConfigFile();
		try
		{
			xf.parse(CongfigResource.loadConfigFile("bitmap.xml", Bitmap.class));
		}
		catch (Exception e)
		{
			fileExist = false;
		}
		if (fileExist)
		{
			XMLConfigElement root = xf.getRootElement();
			XMLConfigElement admin = root.getChildByName("admin");
			if (admin != null)
			{
				adminIp = admin.getStringAttribute("ip", "127.0.0.1");
				adminPort = admin.getIntAttribute("port", 40015);
			}
			ArrayList<XMLConfigElement> list = root.getChildListByName("bitmap");
			for (XMLConfigElement el : list)
			{
				String name = el.getStringAttribute("name");
				Bitmap map = create(el);
				BITMAP_MAP.put(name, map);
			}
			// 启动AdminServer
			server = new AdminServer(adminIp, adminPort, true, "admin>");
			server.addCommand("s", new AdminCommand()
			{
				public void execute(String[] argv, PrintWriter out)
				{
					try
					{
						if (argv.length == 0)
						{
							for (Entry<String, Bitmap> entry : BitmapFactory.BITMAP_MAP.entrySet())
							{

								out.println("------------ " + entry.getKey() + " ----------------");
								out.println(entry.getValue());
							}
						}
						else if (argv.length == 3 && "-get".equalsIgnoreCase(argv[0]))
						{
							// 只提供查询，不提供设置
							Bitmap bitmap = BitmapFactory.getBitmap(argv[1]);
							long uin = StringUtil.convertLong(argv[2], -1);
							//XXX 老版本的MmapBitmap不支持
							out.println(bitmap.getBit(uin));
						}
						else if (argv.length == 2 && "-ext".equalsIgnoreCase(argv[0]))
						{
							Bitmap bitmap = BitmapFactory.getBitmap(argv[1]);
							byte[] bytes = bitmap.getExtInfo();
							out.println(HexUtil.bytes2HexStr(bytes));
						}
						else
						{
							printHelp(out);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						out.println("Exception:" + e.getMessage());
					}
				}

				private void printHelp(PrintWriter out)
				{
					out.print(toString());
				}

				public String toString()
				{
					StringBuilder sb = new StringBuilder();
					sb.append("\nUsage:\n");
					sb.append("\tlist all bitmap\n");
					sb.append("\t[bitmapName]\tshow one bitmap\n");
					sb.append("\t-get\t[bitmapName]\tuin\tget bit value by uin\n");
					sb.append("\t-ext\t[bitmapName]\tget ext value\n");
					sb.append("?\thelp\n");
					return sb.toString();
				}
			});
			server.start();
			System.out.println("start ServerAdmin......");
			System.out.println("start time:" + new Date());
			System.out.println("IP:" + adminIp);
			System.out.println("port:" + adminPort);
		}
	}

	/**
	 * 根据<bitmap>项的配置，创建bitmap对象
	 * @param root
	 * @return
	 */
	private static Bitmap create(XMLConfigElement root)
	{
		// 考虑兼容老版本
		String name = root.getStringAttribute("name");
		String path = root.getStringAttribute("path");
		int capacity = root.getIntAttribute("capacity", Integer.MAX_VALUE);
		int bitLen = root.getIntAttribute("bitLen", 1);
		int extSize = root.getIntAttribute("extSize", 4);
		String clazzName = root.getStringAttribute("class", "");
		// XXX 兼容老版本，如果2012还存在的话，再去除对老版本的兼容-_-!
		if (clazzName == null || clazzName.isEmpty())
		{
			// 默认使用MmapBitmap
			return new MmapBitmap(capacity, bitLen, extSize, name, path);
		}
		// XXX 新版本都通过prop来反射生成class
		Properties props = new Properties();
		ArrayList<XMLConfigElement> ls = root.getChildList();
		for (XMLConfigElement element : ls)
		{
			String pn = element.getStringAttribute("name");
			String pv = element.getStringAttribute("value", "");
			props.setProperty(pn, pv);
		}
		try
		{
			return (Bitmap) Class.forName(clazzName).getConstructor(String.class, String.class, Properties.class)
					.newInstance(name, path, props);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static Bitmap getBitmap(String name)
	{
		return BITMAP_MAP.get(name);
	}

	public static Map<String, Bitmap> getAllBitmap()
	{
		return BITMAP_MAP;
	}

	public static String getAllBitmapInfo()
	{
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (String key : BITMAP_MAP.keySet())
		{
			sb.append(++i).append("]").append(key);
			sb.append(":{").append(BITMAP_MAP.get(key).toString()).append("};");
		}
		return sb.toString();
	}

	public static void main(String[] avg)
	{
		Bitmap bitmap = BitmapFactory.getBitmap("example_bitmap");
		System.out.println("start!");
	}
}
