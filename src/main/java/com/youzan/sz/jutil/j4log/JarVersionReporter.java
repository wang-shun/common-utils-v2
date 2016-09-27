package com.youzan.sz.jutil.j4log;

import com.youzan.sz.jutil.crypto.MD5Coding;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.qq.jutil.crypto.MD5Coding;

/**
 * jar版本自动上报工具
 * 
 * @author philgong
 * 
 */
class JarVersionReporter {
	static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static final Logger log = Logger.getLogger("lib_version_report");

	/**
	 * 启动每小时上报一次
	 */
	public static void listDaily() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				listLib();
			}
		};

		long delay = (long) (20 * Math.random()) + 5;
		Timer timer = new Timer();
		// 设值 10 秒钟后开始执行第一次，以后每隔1小时执行一次
		timer.schedule(task, delay * 1000, 3600 * 1000);
	}

	/**
	 * 把jar版本上报一遍
	 */
	private static void listLib() {
		// 取ip
		String ipPattern = "(172\\..*)|(10\\..*)|(192\\..*)";
		String ip = NetworkInterfaceEx.getLocalAddress(ipPattern);

		// 取classloader
		ClassLoader classLoader = null;
		try {
			Method method = Thread.class.getMethod("getContextClassLoader");
			classLoader = (ClassLoader) method.invoke(Thread.currentThread());
		} catch (Exception e) {
			log.error(ip + "\tget class loader exception\t", e);
		}

		URL url = null;
		try {
			if (classLoader == null) {
				log.error(ip + "\tclass loader is null");
				return;
			}
			// 找到class目录
			url = classLoader.getResource("j4log.property");
			if (url == null) {
				log.error(ip + "\tj4log.property not found");
				return;
			}
			String uri = url.toString();

			int i = uri.lastIndexOf('/');
			int j = uri.lastIndexOf('/', i - 1);
			String classes = uri.substring(j + 1, i);
			// 我们认为class目录应该都是以classes结尾的，否则我们就不上报了
			if (!classes.equals("classes")) {
				log.error(ip + "\tcurrent folder is not classes\t" + url.toString());
				return;
			}
			// 根据class目录找到lib目录
			String dir = uri.substring(0, j) + "/lib";
			File libDir = new File(new URI(dir));
			if (!libDir.exists()) {
				log.error(ip + "\tlib folder not found\t" + libDir.getAbsolutePath());
				return;
			}
			String dirName = libDir.getAbsolutePath();

			// 找lib目录的上一层目录
			i = dirName.lastIndexOf(System.getProperty("file.separator"));
			j = dirName.lastIndexOf(System.getProperty("file.separator"), i - 1);
			String parentDir = dirName.substring(j + 1, i);

			// 找程序根目录
			String baseDir = null;
			// 如果lib的上一层目录是WEB-INF，根目录在WEB-INF上一层
			if (parentDir.equals("WEB-INF")) {
				baseDir = dirName.substring(0, j);
			} else {// 否则根目录就在lib的上一层
				baseDir = dirName.substring(0, i);
			}

			// 上报jar版本
			File[] files = libDir.listFiles();
			for (File file : files) {
				if (file.getName().endsWith("jar")) {
					String fileName = file.getName();
					String filePath = file.getAbsolutePath();
					String md5 = MD5Coding.encodeFile2HexStr(filePath);
					String time = sdf.format(new Date(file.lastModified()));
					String mavenTime = getMavenTime(file.getAbsolutePath());

					// 格式：ip 程序根目录 jar名字 md5
					log.info(ip + "\t" + baseDir + "\t" + fileName + "\t" + md5 + "\t" + time + "\t" + mavenTime);
				}
			}
		} catch (Exception e) {
			log.error(ip + "\turl format error\t" + url.toString(), e);
		}
		return;
	}

	/**
	 * 获得内网ip用的
	 * 
	 */
	private final static class NetworkInterfaceEx {
		public static String getLocalAddress(String pattern) {
			return getLocalAddress(Pattern.compile(pattern));
		}

		public static String getLocalAddress(Pattern pattern) {
			try {
				Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
				while (e.hasMoreElements()) {
					NetworkInterface ni = e.nextElement();
					Enumeration<InetAddress> en = ni.getInetAddresses();
					while (en.hasMoreElements()) {
						InetAddress addr = en.nextElement();
						String ip = addr.getHostAddress();
						Matcher m = pattern.matcher(ip);
						if (m.matches())
							return ip;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private static String getMavenTime(String fileName) {
		String mtime = "";
		BufferedReader br = null;
		JarFile jarFile = null;
		try {
			long start = System.currentTimeMillis();
			jarFile = new JarFile(fileName);
			Enumeration<JarEntry> enume = jarFile.entries();
			InputStream input = null;
			while (enume.hasMoreElements()) {
				JarEntry entry = enume.nextElement();
				String name = entry.getName();
				if (name.endsWith("pom.properties")) {
					input = jarFile.getInputStream(entry);
					break;
				}
			}
			//System.out.println("[JarVersionReporter.getMavenTime] fileName=" + fileName + " input.isNull=" + (input == null));

			if (input != null) {
				br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				String line = null;
				int l = 0;
				while ((line = br.readLine()) != null) {
					l++;
					if (l == 2)
						break;
				}
				//System.out.println("[JarVersionReporter.getMavenTime] maventimeLine=" + line);
				if (line != null && !"".equals(line)) {
					line = line.substring(1);
					SimpleDateFormat mavenSdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
					mtime = sdf.format(mavenSdf.parse(line));
				}
			}

			long end = System.currentTimeMillis();
			//System.out.println("[JarVersionReporter.getMavenTime] mtime=" + mtime + ", utime=" + (end - start) + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(jarFile, br);
		}
		return mtime;
	}
	
	private static void close(JarFile jarfile, Reader reader) {
		try {
			if (jarfile != null)
				jarfile.close();
			if (reader != null)
				reader.close();
		} catch (IOException e) {
			// ignore, do nothing
		}
	}

	public static void main(String[] args) {
		JarVersionReporter.listDaily();
	}
}
