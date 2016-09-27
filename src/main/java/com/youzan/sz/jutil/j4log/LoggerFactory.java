package com.youzan.sz.jutil.j4log;

import com.youzan.sz.jutil.j4log.formatter.PatternFormatter;
import com.youzan.sz.jutil.string.StringUtil;
import com.youzan.sz.jutil.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//import com.qq.jutil.j4log.Logger.Level;
//import com.qq.jutil.j4log.formatter.PatternFormatter;
//import com.qq.jutil.string.StringUtil;
//import com.qq.jutil.util.Pair;

/**
 * 
 * @author isaacdong
 * 
 */
public class LoggerFactory
{
    private static final String LOG_CONF = "j4log.property";

    /**
     * 日志根本目录<br/>
     * 
     * @since 2.0
     */
    private static String defaultLogRoot;

    /**
     * 旧版本默认日志
     */
    static final String DEFAULT_LOG = "_j4log";

    /**
     * 默认日志级别
     */
    static Logger.Level defaultLogLevel = Logger.Level.INFO;
    
    /**
     * 默认pattern为“%message”
     */
    static String defaultPattern = "%message";

    /**
     * @since 2.0
     */
    private static final String VERSION = "j4log.version";

    /**
     * @since 2.0
     */
    private static final String ROOT = "j4log.root";

    /**
     * @since 3.0
     */
    private static final String PATTERN = "j4log.pattern";
    
    private static final String DEFAULT_LEVEL = "j4log.level";

    private static final String REWRITE = "rewrite";

    private static final String FINAL = "final";

    /**
     * 所有的Logger对象
     */
    static final ConcurrentHashMap<String, Logger> loggerMap = new ConcurrentHashMap<String, Logger>(128);

    // public static final boolean debug = false;

    /**
     * 远程日志send buffer
     */
    static byte[] remoteBuffer = new byte[8 * 1024 * 1024];
    
    /**
     * 压缩远程日志的buffer 
     */
    static byte[] remoteDeflateBuffer = new byte[8 * 1024 * 1024];
    
    //空的buffer
    static byte[] emptyBuffer = new byte[0];
    
    
    
    /**
     * 远程日志连接池
     */
    static HashMap<Pair<InetAddress, Integer>, Socket> m = new HashMap<Pair<InetAddress, Integer>, Socket>();

    /**
     * 原始远程配置，用于调试
     */
    private static List<String> rawRemoteConf = new ArrayList<String>();

    /**
     * 初始化J4LOG组件
     */
    static
    {
        init();
    }

    private static void addDefaultLocalLog(Logger log)
    {
        retouchLocalLog(log);
        List<Logger> list = new ArrayList<Logger>(1);
        list.add(log);
        updateLoggerMap(list, true);
    }

    /**
     * 工厂方法，根据名字来获取得一个Logger，同样的名字返回同一个实例。本接口禁止出现空提针异常。
     * 
     * @param name Logger的名字
     * @return Logger
     */
    static Logger getLogger(String name)
    {
        Logger logger = loggerMap.get(name);
        if (logger == null)
        {
            synchronized (LoggerFactory.class)
            {
                logger = loggerMap.get(name);
                if (logger == null)
                {
                    logger = new Logger(name, defaultLogLevel, null, defaultPattern);
                    addDefaultLocalLog(logger);
                    getLogger(name);
                }
            }
        }
        return logger;
    }

    /**
     * @return 返回所有日志对象的只读MAP
     */
    static Map<String, Logger> getLoggerMap()
    {
        return Collections.unmodifiableMap(loggerMap);
    }

    /**
     * 读取原始远程配置，用于调试
     */
    public static List<String> getRawRemoteConfig()
    {
        return Collections.unmodifiableList(rawRemoteConf);
    }

    @SuppressWarnings("unused")
    private static void init()
    {
        try
        {
            List<String> list = new ArrayList<String>();
            LoggerFactory.reloadConfig(list);
            Logger log = loggerMap.get("lib_version_report");
            JarVersionReporter.listDaily();
            new Thread("j4logRemoteConfigInitThread")
            {
                @Override
                public void run()
                {
                    try
                    {
                        Class.forName("com.qq.conf.J4logLoader", true, LoggerFactory.class.getClassLoader());
                    }
                    catch (Exception e)
                    {
                        System.out.println("FATAL J4LOG REMOTE INIT FAILED(remote loader: com.qq.conf.J4logLoader): " + e.getMessage());
                        e.printStackTrace(System.out);
                    }
                    try
                    {
                        System.out.println("INFO load J4logMonitor");
                        Class.forName("com.qq.j4log.companion.J4logMonitor");
                        System.out.println("INFO load J4logMonitor success");
                    }
                    catch (Throwable e)
                    {
                        System.out.println("WARN load J4logMonitor fail, the classs com.qq.j4log.companion.J4logMonitor not found");
                    }
                }
            }.start();
        }
        catch (Exception e)
        {
            System.out.println("FATAL j4log init failed:" + e.getMessage());
            e.printStackTrace(System.out);
        }
        finally
        {
            // start logger thread
            Thread th = new Thread(new LogWorkThread(), "LocalJ4logThread");
            th.start();
            Thread thRemote = new Thread(new LogWorkThread(true), "RemoteJ4logThread");
            thRemote.start();
        }
    }

    /**
     * 读取classpath下的文件<br/>
     * 为了避免类的循环加载，从其它地方拷贝过来的。
     * 
     * @param resource 资源路径
     * @param clazz
     * @return InputStream 文件的input stream,如果找不到则返回null
     */
    static InputStream loadConfigFile(String resource, Class<?> clazz)
    {
        ClassLoader classLoader = null;
        try
        {
            Method method = Thread.class.getMethod("getContextClassLoader");
            classLoader = (ClassLoader) method.invoke(Thread.currentThread());
        }
        catch (Exception e)
        {
            System.out.println("ERROR : j4log reflection error: " + e.getMessage());
            e.printStackTrace(System.out);
        }
        if (classLoader == null)
        {
            classLoader = clazz.getClassLoader();
        }
        try
        {
            if (classLoader != null)
            {
                URL url = classLoader.getResource(resource);
                if (url == null)
                {
                    System.out.println("INFO j4log Can not find resource:" + resource);
                    return null;
                }
                if (url.toString().startsWith("jar:file:"))
                {
                    System.out.println("INFO j4log Get resource \"" + resource + "\" from jar:\t" + url.toString());
                    return clazz.getResourceAsStream(resource.startsWith("/") ? resource : "/" + resource);
                }
                System.out.println("INFO j4log Get resource \"" + resource + "\" from:\t" + url.toString());
                return new FileInputStream(new File(url.toURI()));
            }
        }
        catch (Exception e)
        {
            System.out.println("ERROR : j4log loadConfigFile error: " + e.getMessage());
            e.printStackTrace(System.out);
        }
        return null;
    }

    /**
     * 读取默认日志目录 j4log.root<br/>
     * server.root/log<br/>
     * resin.root/log<br/>
     * tac.root/log<br/>
     * 
     * @param r
     * @return
     */
    private static String loadDefaultLogRoot(String r)
    {
        if (r.isEmpty())
        {
            System.out.println("ERROR j4log.property(j4log.version>1) necessary config key not found: j4log.root");
        }
        File _r = new File(r);
        if (!_r.isDirectory() || !_r.canWrite())
        {
            System.out.println("ERROR j4log.property(j4log.version>1) config error: j4log.root=" + r + " is not a directory or read only.");
        }
        else
        {
            System.out.println("INFO j4log read j4log.property ok, set j4log.root to :" + r);
            return r;
        }

        System.out.println("INFO j4log try to set j4log.root from java system property (server.root)");
        r = System.getProperty("server.root", "");
        r = r.trim() + "/log";
        _r = new File(r);
        if (!_r.isDirectory() || !_r.canWrite())
        {
            System.out.println("ERROR j4log set j4log root from java system property server.root failed.");
        }
        else
        {
            System.out.println("INFO j4log read java system property server.root ok, set j4log.root to :" + r);
            return r;
        }

        System.out.println("INFO j4log try to set j4log.root from java system property (resin.root)");
        r = System.getProperty("resin.home", "");
        r = r.trim() + "/log";
        _r = new File(r);
        if (!_r.isDirectory() || !_r.canWrite())
        {
            System.out.println("ERROR j4log set j4log root from java system property resin.root failed.");
        }
        else
        {
            System.out.println("INFO j4log read java system property resin.root ok, set j4log.root to :" + r);
            return r;
        }

        System.out.println("INFO j4log try to set j4log.root from java system property (tac.root)");
        r = System.getProperty("tac.home", "");
        r = r.trim() + "/log";
        _r = new File(r);
        if (!_r.isDirectory() || !_r.canWrite())
        {
            System.out.println("ERROR j4log set j4log root from java system property tac.root failed.");
        }
        else
        {
            System.out.println("INFO j4log read java system property tac.root ok, set j4log.root to :" + r);
            return r;
        }
        return null;
    }

    private static synchronized int loadLocalConfig(ConcurrentHashMap<String, Logger> m)
    {
        int version = 2;
        try
        {
            Properties prop = new Properties();
            InputStream in = loadConfigFile(LOG_CONF, LoggerFactory.class);
            prop.load(in);
            prop.put("lib_version_report", "info, libversion, 172.27.39.47:10021, /data/log/lib_version_report.lost");

            String x = prop.getProperty(VERSION);
            version = StringUtil.convertInt(x, 2);
            
            if (version == 3)
            {
                loadLocalConfigV3(m, prop);
            }
            else if (version == 2)
            {
                loadLocalConfigV2(m, prop);
            }
            else if (version == 1)
            {
                loadLocalConfigV1(m, prop);
            }
            else
            {
                System.out.println("ERROR j4log.version invalid: " + version + " set version to default 2");
                version = 2;
                loadLocalConfigV2(m, prop);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
        return version;
    }

    private static synchronized void loadLocalConfigV1(ConcurrentHashMap<String, Logger> m, Properties prop)
    {
        loadLocalRawConfig(m, prop);
    }

    private static synchronized void loadLocalConfigV2(ConcurrentHashMap<String, Logger> m, Properties prop)
    {
        loadLocalRootAndLevel(prop);

        loadLocalRawConfig(m, prop);
    }

    /**
     * 在原有基础上增加日至格式pattern可配置，不配置或者不合法将采用默认配置“%message”，也就是和之前一样打印时间、级别、日志内容
     * @param m
     * @param prop
     */
    private static synchronized void loadLocalConfigV3(ConcurrentHashMap<String, Logger> m, Properties prop)
    {
	loadLocalRootAndLevel(prop);
	
	String pattern = prop.getProperty(PATTERN);
	if (StringUtil.isEmpty(pattern))
	{
	    System.out.println("INFO j4log.pattern is empty, set j4log.default pattern to: %message. ");
	}
	//pattern应至少有一个“%”
	else if (pattern.indexOf(PatternFormatter.PATTERN_MARKER) < 0)
	{
	    System.out.println("ERROR j4log.pattern: "+ pattern +" is illegal, set j4log.default pattern to: %message. ");
	}
	else
	{
	    defaultPattern = pattern.trim();
	    System.out.println("INFO j4log.pattern is ok, set j4log.default pattern to: " + pattern);
	}
	
        loadLocalRawConfig(m, prop);
    }
    
    private static void loadLocalRootAndLevel(Properties prop)
    {
	String r = prop.getProperty(ROOT, "");
        r = Util.getFilePath(r);
        r = loadDefaultLogRoot(r);
        if (r != null)
        {
            r = r.replaceAll("[/]+$", "");
            r = r.replaceAll("[\\\\]+$", "");
        }
        LoggerFactory.defaultLogRoot = r + File.separatorChar;

        // 设置默认日志级别
        defaultLogLevel = Logger.Level.valueOf(prop.getProperty(DEFAULT_LEVEL, Logger.Level.INFO.name()).trim().toUpperCase());
        System.out.println("INFO j4log read j4log.property ok, set j4log.default level to :" + defaultLogLevel);
    }  


    /**
     * 加载本地j4log.property的配置项，自动删除不合法的配置项。
     * 
     * @param m
     * @param prop
     */
    private static synchronized void loadLocalRawConfig(ConcurrentHashMap<String, Logger> m, Properties prop)
    {
        Set<Map.Entry<Object, Object>> entrySet = prop.entrySet();
        Iterator<Map.Entry<Object, Object>> it = entrySet.iterator();
        while (it.hasNext())
        {
            try
            {
                Map.Entry<Object, Object> entry = it.next();

                String loggerName = ((String) entry.getKey()).trim();
                if (loggerName.startsWith("j4log."))
                {
                    continue;
                }
                Logger.Level level = null;

                Object o = m.get(loggerName);
                if (o != null)
                {
                    System.out.println("WARN duplicate config found and will be ignored, logger name=" + loggerName);
                    continue;
                }
                String valueStr = ((String) entry.getValue()).trim();
                String[] arr = valueStr.split(",");

                try
                {
                    level = Logger.Level.valueOf(arr[0].trim().toUpperCase());
                }
                catch (Exception e)
                {
                    System.out.println("ERROR Logger(" + loggerName + ") level config error: log level=[" + arr[0] + "]");
                    level = Logger.Level.DEBUG;
                }
                if (arr.length == 1)
                {
                    Logger log = new Logger(loggerName, level, null, defaultPattern);
                    log.isRemoteLog = false;
                    m.put(loggerName, log);
                    continue;
                }
                if (arr.length == 2)
                {
                    /**
                     * 本地日志格式， 可能是：<br/>
                     * name=level,path<br/>
                     * name=level,pattern
                     */
                    Logger log = createLocalLogger(loggerName, level, arr[1].trim());
                    m.put(loggerName, log);
                    continue;
                }
                else if (arr.length >= 3)
                {
                    /**
                     * 如果长度等于3，并且最后一个包含“%”，则为本地日志<br/>
                     * name=level,path,pattern<br/>
                     */
                    if (arr.length == 3 && arr[2].indexOf(PatternFormatter.PATTERN_MARKER) >= 0)
		    {
                        Logger log = createLocalLogger(loggerName, level, arr[1].trim(),arr[2].trim());
                        m.put(loggerName, log);
                        continue;
		    }
                    
                    /**
                     * 远程日志格式<br/>
                     * name=level,type,ip:port,filePath,failFilePath
                     */
                    String type = arr[1].trim();
                    String[] s = arr[2].split(":");
                    if (s.length != 2)
                    {
                        System.out.println("ERROR j4log the configure of " + loggerName + " is wrong! logserver=[" + arr[2] + "]");
                        continue;
                    }

                    InetAddress addr = InetAddress.getByName(s[0].trim());
                    if (addr == null)
                    {
                        System.out.println("ERROR j4log the configure of " + loggerName + " is wrong! remote ip=[" + s[0] + "]");
                        continue;
                    }

                    int port = Integer.parseInt("" + s[1]);
                    if (port < 0)
                    {
                        continue;
                    }

                    String failLog = Util.getFilePath(arr[3].trim());
                    if (failLog != null && !failLog.isEmpty() && !testFile(failLog))
                    {
                        System.out.println("ERROR j4log the configure of " + loggerName + " is wrong! fail log path=[" + failLog + "]");
                        continue;
                    }

                    String localLog = arr.length > 4 ? Util.getFilePath(arr[4].trim()) : null;
                    if (localLog != null && !localLog.isEmpty() && !testFile(localLog))
                    {
                        System.out.println("ERROR j4log the configure of " + loggerName + " is wrong! log path=[" + localLog + "]");
                        continue;
                    }

                    Logger log = new Logger(loggerName, localLog, type, failLog, Pair.makePair(addr, port), defaultPattern);
                    log.isRemoteLog = true;
                    log.setLevel(level);
                    m.put(loggerName, log);
                    continue;
                }
                else
                {
                    System.out.println("ERROR the configure of " + loggerName + " is wrong: " + valueStr);
                }
            }
            catch (Exception e)
            {
                System.out.println("ERROR load j4log.property error:" + e.getMessage());
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * 本地日志格式， 可能是：<br/>
     * name=level,path<br/>
     * name=level,pattern<br/>
     * 根据str是否含有“%”来判断，如果有则为pattern，否则为path
     */
    private static Logger createLocalLogger(String loggerName, Logger.Level level, String str)
    {
	String filePath = null;
	String pattern = null;
	
	if (str.indexOf(PatternFormatter.PATTERN_MARKER) >= 0)
	{
	    System.out.println("INFO j4log Logger(" + loggerName + ") pattern is: " + str);
	    pattern = str;
	}
	else
	{
	    filePath = getLocalLogPath(loggerName, str);
	    pattern = defaultPattern;
	}
	
	return new Logger(loggerName, level, filePath, pattern);	 
    }

    /**
     * 本地日志格式<br/>
     * name=level,path,pattern<br/>
     * 
     */
    private static Logger createLocalLogger(String loggerName, Logger.Level level, String path, String logPattern)
    {
	System.out.println("INFO j4log Logger(" + loggerName + ") pattern is: " + logPattern);
	
	if (StringUtil.isEmpty(logPattern) || logPattern.indexOf(PatternFormatter.PATTERN_MARKER) < 0)
	{
	    System.out.println("ERROR j4log Logger(" + loggerName + ") pattern illegal: " + logPattern);
	    logPattern = defaultPattern;
	}
	
	String filePath = getLocalLogPath(loggerName, path);

	return new Logger(loggerName, level, filePath, logPattern);
    }

    private static String getLocalLogPath(String loggerName, String path)
    {
	String filePath = Util.getFilePath(path);
	if (!testFile(filePath))
	{
	    System.out.println("WARN j4log Logger(" + loggerName + ") path config error: log path=[" + filePath + "]");
	    return null;
	}
	return filePath;
    }
    
    /**
     * 加载配置中心的配置项，自动删除不合法的配置项。
     * 
     * @param m
     * @param data
     */
    private static synchronized void loadRemoteRawConfig(ConcurrentHashMap<String, Logger> m, List<String> data)
    {
        // load data from config center
        for (String x : data)
        {
            try
            {
                if (x == null)
                {
                    continue;
                }
                x = x.trim();
                // 去掉注释和空行
                if (x.isEmpty() || x.startsWith("#"))
                {
                    continue;
                }
                String[] pair = x.split("=");
                if (pair.length != 2 || pair[0] == null || pair[1] == null)
                {
                    System.out.println("ERROR j4log remote config error, line=" + x);
                    continue;
                }

                String name = pair[0].trim();
                if (m.contains(name))
                {
                    System.out.println("ERROR j4log remote config error : duplicate logger found, line=" + x);
                    continue;
                }

                String[] arr = pair[1].trim().split(",");
                Logger.Level l = null;
                boolean canRewrite = true;
                try
                {
                    l = Logger.Level.valueOf("" + arr[0].trim().toUpperCase());
                }
                catch (Exception e)
                {
                    System.out.println("ERROR j4log remote config error: log level invalid line=" + x);
                    continue;
                }
                if (arr.length == 1)
                {
                    Logger log = new Logger(name, l, null, defaultPattern);
                    log.canRewrite = true;
                    log.isRemoteLog = false;
                    m.put(name, log);
                }
                else if (arr.length == 2)
                {
                    /**
                     * local=info,rewrite<br/>
                     */
                    String a2 = arr[1].trim().toLowerCase();
                    if (a2.equals(REWRITE))
                    {
                        canRewrite = true;
                    }
                    else if (a2.equals(FINAL))
                    {
                        canRewrite = false;
                    }
                    else
                    {
                        canRewrite = true;
                        System.out.println("ERROR j4log remote config error: log rewrite flag invalid line=" + x);
                    }

                    Logger log = new Logger(name, l, null, defaultPattern);
                    log.canRewrite = canRewrite;
                    log.isRemoteLog = false;
                    m.put(name, log);
                }
                else if (arr.length >= 6)
                {
                    /**
                     * remot=info,module,ip:port,false,true,final<br/>
                     * remot=info,module,ip:port,false,true,rewrite<br/>
                     */
                    String type = arr[1].trim();
                    if (type == null || !type.matches("[^/\\\\]+"))
                    {
                        System.out.println("ERROR j4log remote config error: the configure of " + name + " is wrong! type=[" + type + "]");
                        continue;
                    }
                    String[] s = arr[2].split(":");
                    InetAddress addr = InetAddress.getByName(s[0].trim());
                    if (addr == null)
                    {
                        System.out.println("ERROR j4log remote config error: the configure of " + name + " is wrong! remote ip=[" + s[0] + "]");
                        continue;
                    }
                    int port = Integer.parseInt("" + s[1].trim());
                    boolean userFailLog = Boolean.valueOf(arr[3].trim());
                    boolean useFullLog = Boolean.valueOf(arr[4].trim());
                    String a5 = arr[5].trim().toLowerCase();
                    if (a5.equals(REWRITE))
                    {
                        canRewrite = true;
                    }
                    else if (a5.equals(FINAL))
                    {
                        canRewrite = false;
                    }
                    else
                    {
                        canRewrite = true;
                        System.out.println("ERROR j4log remote config error: log rewrite flag invalid line=" + x);
                    }

                    Logger log = new Logger(name, null, type, null, Pair.makePair(addr, port), defaultPattern);
                    log.setLevel(l);
                    log.useLogPath = useFullLog;
                    log.useFailLogPath = userFailLog;
                    log.canRewrite = canRewrite;
                    log.isRemoteLog = true;
                    m.put(name, log);
                }
                else
                {
                    System.out.println("ERROR j4log remote config error: format invalid line=" + x);
                }
            }
            catch (Exception e)
            {
                System.out.println("ERROR j4log remote config error: parse data error line=" + x);
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * 测试专用
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        System.setProperty("server.root", "d:/swap");
        List<String> list = new ArrayList<String>();
        // local=rewrite,info
        // remot=final,info,module,ip:port,false,true
        // remot=rewrite,info,module,ip:port,false,true
        // list.add("local=info,rewrite");
        list.add("remote1=INFO,module1,192.168.2.1:10021,true,false,rewrite");
        // list.add("remote2=info,module2,192.168.2.2:10021,false,true,rewrite");
        reloadConfig(list);
        // Logger.getLogger("TST");
        for (Logger log : loggerMap.values())
        {
            System.out.println(log);
        }
        Thread.sleep(5000);
        System.exit(0);
    }

    private static boolean recheck(Logger log)
    {
        if (!log.isRemoteLog)
        {
            if (log.level() == null)
            {
                return false;
            }
            if (!testFile(log.getLogPath()))
            {
                return false;
            }
        }
        else
        {
            if (log.getLogAddr() == null)
            {
                return false;
            }
            if (log.getLogPath() == null && log.getFailLogPath() == null)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 重新读取配置<br/>
     * 注意,该函数仅限配置中心使用，其他模块禁止调用，否则后果自负
     * 
     * @param data 配置中心j4log文件全部内容
     * 
     */
    public static synchronized void reloadConfig(List<String> data)
    {
        System.out.println("INFO j4log start to reload config ...");
        securityCheck();
        ConcurrentHashMap<String, Logger> map = new ConcurrentHashMap<String, Logger>(128);
        ConcurrentHashMap<String, Logger> remote = new ConcurrentHashMap<String, Logger>(128);
        ConcurrentHashMap<String, Logger> local = new ConcurrentHashMap<String, Logger>(128);
        // load data from j4log.property
        int version = loadLocalConfig(local);
        if (version == 1)
        {
            updateLoggerMap(local.values());
        }
        else
        {
            loadRemoteRawConfig(remote, data);
            // 合并远程配置和本地配置
            for (Logger l : local.values())
            {
                Logger log = new Logger(l.getName());
                Logger r = remote.get(l.getName());
                if (r == null)
                {
                    // 没有远程 配置
                    if (l.level() == null)
                    {
                        System.out.println("ERROR  J4LOG logger(" + l.getName() + ") config error: level not found.");
                        continue;
                    }
                    log.setLevel(l.level());
                    log.canRewrite = true;

                    log.setLogPath(l.getLogPath());

                    // 如果l是远程日志的时候，同时logpath是null则不设置默认路径，否则据需要根据路径是否可访问来决定是否设置远程路径
                    if ((!l.isRemoteLog || null != log.getLogPath()) && !testFile(l.getLogPath()))
                    {
                        String path = defaultLogRoot + log.getName();
                        log.setLogPath(path);
                        if (!testFile(path))
                        {
                            System.out.println("ERROR j4log set logger(" + log.getName() + ") path to default path(" + path + ") failed!");
                            log.setLogPath(null);
                        }
                    }

                    // 如果是远程日志，则设置远程日志特用的属性
                    if (l.isRemoteLog)
                    {
                        log.isRemoteLog = true;
                        log.setLogAddr(l.getLogAddr());
                        log.setType(l.getType());
                        log.setFailLogPath(l.getFailLogPath());
                        //3Glog只使用默认pattern
                        log.setPattern(defaultPattern);
                        if (!testFile(log.getFailLogPath()))
                        {
                            String path = defaultLogRoot + log.getName();
                            log.setFailLogPath(path);
                            if (!testFile(path))
                            {
                                System.out.println("ERROR j4log set logger(" + log.getName() + ") fail log path to default path(" + path + ") failed!");
                                log.setFailLogPath(null);
                            }
                        }
                    }
                    else 
                    {
                	//本地日志需要设置pattern，直接把formatter设置给新的logger实例
                	log.formatter = l.formatter;
		    }
                    map.put(log.getName(), log);
                    continue;
                }
                // 有远程配置
                if (r.isRemoteLog)
                {
                    // 远程日志
                    log.isRemoteLog = true;
                    //3Glog只使用默认pattern
                    log.setPattern(defaultPattern);
                    if (r.canRewrite)
                    {
                        log.canRewrite = true;
                        log.setLevel(l.level());
                        log.setType(l.getType());
                        log.setLogAddr(l.getLogAddr());
                        log.setLogPath(l.getLogPath());
                        log.setFailLogPath(l.getFailLogPath());
                    }
                    else
                    {
                        log.canRewrite = false;
                        log.setLevel(r.level());
                        log.setType(r.getType());
                        log.setLogAddr(r.getLogAddr());
                        // log.setLogPath(l.getLogPath());
                        // log.setFailLogPath(l.getFailLogPath());
                        if (r.useFailLogPath)
                        {
                            String path = defaultLogRoot + log.getName() + ".lost";
                            log.setFailLogPath(path);
                            if (!testFile(path))
                            {
                                System.out.println("ERROR j4log set logger(" + log.getName() + ") fail log path to default path(" + path + ") failed!");
                                log.setFailLogPath(null);
                            }
                        }
                        if (r.useLogPath)
                        {
                            String path = defaultLogRoot + log.getName();
                            log.setLogPath(path);
                            if (!testFile(path))
                            {
                                System.out.println("ERROR j4log set logger(" + log.getName() + ")full log path to default path(" + path + ") failed!");
                                log.setLogPath(null);
                            }
                        }
                    }
                    remote.remove(log.getName());                    
                    map.put(log.getName(), log);
                    continue;
                }
                // 本地日志
                log.isRemoteLog = false;
                if (r.canRewrite)
                {
                    log.canRewrite = true;
                    log.setLevel(l.level());
                    log.setLogPath(l.getLogPath());
                }
                else
                {
                    log.canRewrite = false;
                    log.setLevel(r.level());
                }
                
                //本地日志需要设置pattern，直接把formatter设置给新的logger实例
        	log.formatter = l.formatter;
        	
                if (log.getLogPath() == null || !testFile(log.getLogPath()))
                {
                    String path = defaultLogRoot + log.getName();
                    log.setLogPath(path);
                    if (!testFile(path))
                    {
                        log.setLogPath(null);
                        System.out.println("ERROR j4log set logger(" + log.getName() + ")local log path to default path(" + path + ") failed ID=1!");
                    }
                }
                remote.remove(log.getName());
                map.put(log.getName(), log);
                continue;
            }
            for (Logger log : remote.values())
            {
                //这些都是3Glog或者只在远程配置的locallog，使用默认pattern
                log.setPattern(defaultPattern);
                
                if (log.isRemoteLog)
                {
                    if (log.useFailLogPath)
                    {
                        String path = defaultLogRoot + log.getName() + ".lost";
                        log.setFailLogPath(path);
                        if (!testFile(path))
                        {
                            System.out.println("ERROR j4log set logger(" + log.getName() + ") fail log path to default path(" + path + ") failed!");
                            log.setFailLogPath(null);
                        }
                    }
                    if (log.useLogPath)
                    {
                        String path = defaultLogRoot + log.getName();
                        log.setLogPath(path);
                        if (!testFile(path))
                        {
                            System.out.println("ERROR j4log set logger(" + log.getName() + ")full log path to default path(" + path + ") failed!");
                            log.setLogPath(null);
                        }
                    }
                }
                else
                {
                    if (!testFile(log.getLogPath()))
                    {
                        String path = defaultLogRoot + log.getName();
                        log.setLogPath(path);
                        if (!testFile(path))
                        {
                            log.setLogPath(null);
                            System.out.println("ERROR j4log set logger(" + log.getName() + ")local log path to default path(" + path + ") failed! ID=2");
                        }
                    }
                }
            }
            updateLoggerMap(remote.values());
            updateLoggerMap(map.values());
        }
        LoggerFactory.rawRemoteConf = data;
    }

    /**
     * 为本地logger设置level、path属性
     * @param log
     */
    static void retouchLocalLog(Logger log)
    {
        if (log.level() == null)
        {
            log.setLevel(defaultLogLevel);
        }
        if (log.getLogPath() == null || !testFile(log.getLogPath()))
        {
            String path = defaultLogRoot + log.getName();
            log.setLogPath(path);
            if (!testFile(path))
            {
                log.setLogPath(null);
                System.out.println("ERROR j4log set logger(" + log.getName() + ")local log path to default path(" + path + ") failed! ID=0");
            }
        }
    }

    private static void securityCheck()
    {
        @SuppressWarnings("unused")
        RuntimeException e = null;
        try
        {
            throw new RuntimeException();
        }
        catch (RuntimeException ex)
        {
            e = ex;
        }
    }

    /**
     * 试图打开或创建一个文件
     * 
     * @param filePath
     * @return
     */
    public static boolean testFile(String filePath)
    {
        if (filePath == null || filePath.trim().isEmpty() || !filePath.contains("/") && !filePath.contains("\\"))
        {
            return false;
        }
        try
        {
            File f = new File(filePath + "." + Util.getDateSimpleInfo(System.currentTimeMillis()));
            f.createNewFile();
            if (!f.canWrite())
            {
                return false;
            }

            if (f.length() == 0)
            {
                f.delete();
            }
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }

    private static synchronized void updateLoggerMap(Collection<Logger> c)
    {
        updateLoggerMap(c, false);
    }

    private static synchronized void updateLoggerMap(Collection<Logger> c, boolean flag)
    {
        for (Logger log : c)
        {
            Logger o = loggerMap.get(log.getName());
            if (flag || recheck(log))
            {
                if (o == null)
                {
                    loggerMap.put(log.getName(), log);
                    continue;
                }
                o.setLevel(log.level());
                o.setLogPath(log.getLogPath());
                o.setFailLogPath(log.getFailLogPath());
                o.setLogAddr(log.getLogAddr());
                o.setType(log.getType());
                o.formatter = log.formatter;
                o.isRemoteLog = log.isRemoteLog;
                o.canRewrite = log.canRewrite;
                continue;
            }
            System.out.println("WARN j4log failed to update Loggers'Map: invalid logger+" + log);
        }
    }

    public static Logger.Level getDefaultLevel()
    {
	return defaultLogLevel;
    }

    public static String getDefaultPattern()
    {
	return defaultPattern;
    }

    public static String getDefaultLogRoot()
    {
	return defaultLogRoot;
    }
}
