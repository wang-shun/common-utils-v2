package com.youzan.sz.jutil.j4log;

import static com.youzan.sz.jutil.j4log.LoggerFactory.DEFAULT_LOG;
import static com.youzan.sz.jutil.j4log.LoggerFactory.m;
import static com.youzan.sz.jutil.j4log.LoggerFactory.remoteBuffer;
//import static com.qq.jutil.j4log.LoggerFactory.DEFAULT_LOG;
//import static com.qq.jutil.j4log.LoggerFactory.m;
//import static com.qq.jutil.j4log.LoggerFactory.remoteBuffer;

import com.youzan.sz.jutil.j4log.formatter.PatternFormatter;
import com.youzan.sz.jutil.string.StringUtil;
import com.youzan.sz.jutil.util.Pair;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.j4log.formatter.PatternFormatter;
//import com.qq.jutil.string.StringUtil;
//import com.qq.jutil.util.Pair;
//import com.qq.jutil.j4log.LoggingEvent;

/**
 * 日志对象<br/>
 * 
 * @author isaacdong
 */
public class Logger {

    // -------------------------------------------------------------------------------------
    /**
     * 日志级别类型
     */
    public static enum Level {
                              DEBUG(0), INFO(1), WARN(2), ERROR(3), FATAL(4);
        private int value;

        private Level(int value) {
            this.value = value;
        }
    }

    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    // -------------------------------------------------------------------------------------

    /**
     * @return 返回包好所有日志对象的只读MAP
     */
    public static Map<String, Logger> getLoggerMap() {
        return LoggerFactory.getLoggerMap();
    }

    /**
     * Logger类的全名
     */
    static final String                        FQN            = "com.qq.jutil.j4log.Logger";

    private final AtomicInteger                a              = new AtomicInteger();

    /**
     * 日志名称
     */
    private final String                       name;

    /**
     * 
     */
    boolean                                    useLogPath     = false;
    /**
     * 本地日志路径
     */
    private String                             logPath;
    /**
     * 日志级别
     */
    private Level                              level          = null;
    /**
     * 日志缓冲队列
     */
    private final LinkedBlockingQueue<LogItem> logQueue       = new LinkedBlockingQueue<LogItem>(50000);

    /**
     * 远程日志特有的属性,远程日志目录名
     */
    private String                             type;

    boolean                                    isRemoteLog    = false;

    /**
     * 
     */
    boolean                                    useFailLogPath = true;

    /**
     * 用来表明该日志对象是否是启用状态，true表示是启用状态，false表示没人使用
     */
    boolean                                    isEnabled      = false;
    /**
     * 失败路径。<br/>
     * 即写远程日志失败后，写入本地的路径。
     */
    private String                             failLogPath;

    /**
     * 日志服务器地址
     */
    // private Pair<InetAddress, Integer> logAddr;
    List<Pair<InetAddress, Integer>>           logAddrs       = new ArrayList<Pair<InetAddress, Integer>>();

    /**
     * 是否允许使用本地配置覆盖
     */
    boolean                                    canRewrite     = true;

    /**
     * 日志内容格式化器
     */
    PatternFormatter                           formatter;

    /**
     * 使用默认配置初始化本地日志。
     * 
     * @param name
     * @since j4log 2.0
     */
    Logger(String name) {
        this(name, Level.DEBUG, null, null);
    }

    /**
     * 初始化本地日志
     * 
     * @param name
     * @param level
     * @param path
     * @since j4log 2.0
     */
    Logger(String name, Level level, String path, String pattern) {
        this(name, path, null, null, null, pattern);
        this.level = level;
    }

    /**
     * 初始化日志
     * 
     * @param name
     * @param filePath
     * @param type
     * @param failLogPath
     * @param addr
     */
    protected Logger(String name, Level level, String localPath, String failLogPath, String pattern, boolean isRemote) {
        this.name = name;
        this.level = level;
        this.logPath = localPath;
        this.failLogPath = failLogPath;
        this.isRemoteLog = isRemote;

        if (StringUtil.isEmpty(pattern)) {
            formatter = new PatternFormatter();
        } else {
            formatter = new PatternFormatter(pattern);
        }
    }

    /**
     * 初始化日志
     * 
     * @param name
     * @param filePath
     * @param type
     * @param failLogPath
     * @param addr
     */
    Logger(String name, String filePath, String type, String failLogPath, Pair<InetAddress, Integer> addr,
           String pattern) {
        this.name = name;
        this.logPath = filePath;
        this.type = type;
        this.failLogPath = failLogPath;
        logAddrs = new ArrayList<Pair<InetAddress, Integer>>();
        logAddrs.add(addr);

        if (StringUtil.isEmpty(pattern)) {
            formatter = new PatternFormatter();
        } else {
            formatter = new PatternFormatter(pattern);
        }
    }

    /**
     * 打level为debug的log
     * 
     * @param str 要打的log
     */
    public void debug(String str) {
        this.debug(str, null, null);
    }

    /**
     * 打level为debug的log，并将异常的堆栈也打出来
     * 
     * @param str 要打的log
     * @param th 异常的堆栈
     */
    public void debug(String str, Throwable th) {
        this.debug(str, null, th);
    }

    /**
     * 打level为debug的log，支持占位符“{}”替换：</br>
     *   logger.debugMore("Detail info: name={}, age={}", name, age);
     * 
     * @param format  要打的log(包含占位符)
     * @param arguments 替换占位符的对象列表
     */
    public void debugMore(String format, Object... arguments) {
        this.debug(format, arguments, null);
    }

    /**
     * 打level为debug的log
     * 
     * @param str
     *            要打的log
     */
    private void debug(String format, Object[] arguments, Throwable th) {
        if (this.isDebugEnabled()) {
            formatLogMsg(Level.DEBUG, format, arguments, th);
        }
    }

    /**
     * 打level为info的log
     * 
     * @param str 要打的log
     */
    public void info(String str) {
        this.info(str, null, null);
    }

    /**
     * 打level为info的log，并将异常的堆栈也打出来
     * 
     * @param str 要打的log
     * @param th 异常的堆栈
     */
    public void info(String str, Throwable th) {
        this.info(str, null, th);
    }

    /**
     * 打level为info的log，支持占位符“{}”替换：</br>
     *   logger.infoMore("Detail info: name={}, age={}", name, age);
     * 
     * @param format  要打的log(包含占位符)
     * @param arguments 替换占位符的对象列表
     */
    public void infoMore(String format, Object... arguments) {
        //	Throwable throwableCandidate = MessageFormatter.getThrowableCandidate(arguments);
        //	if (throwableCandidate != null)
        //	{
        //	    this.info(format, arguments, throwableCandidate);
        //	}
        //	else
        //	{
        this.info(format, arguments, null);
        //	}    
    }

    /**
     * 打level为info的log
     * 
     * @param str
     *            要打的log
     */
    private void info(String format, Object[] arguments, Throwable th) {
        if (this.isInfoEnabled()) {
            formatLogMsg(Level.INFO, format, arguments, th);
        }
    }

    /**
     * 打level为warn的log
     * 
     * @param str 要打的log
     */
    public void warn(String str) {
        warn(str, null, null);
    }

    /**
     * 打level为warn的log，并将异常的堆栈也打出来
     * 
     * @param str 要打的log
     * @param th 异常的堆栈
     */
    public void warn(String str, Throwable th) {
        warn(str, null, th);
    }

    /**
     * 打level为warn的log，支持占位符“{}”替换：</br>
     *   logger.warnMore("Detail info: name={}, age={}", name, age);
     * 
     * @param format  要打的log(包含占位符)
     * @param arguments 替换占位符的对象列表
     */
    public void warnMore(String format, Object... arguments) {
        this.warn(format, arguments, null);
    }

    /**
     * 打level为warn的log
     * 
     * @param str
     *            要打的log
     */
    private void warn(String format, Object[] arguments, Throwable th) {
        formatLogMsg(Level.WARN, format, arguments, th);
    }

    /**
     * 打level为error的log
     * 
     * @param str 要打的log
     */
    public void error(String str) {
        error(str, null, null);
    }

    /**
     * 打level为error的堆栈
     * 
     * @param str 要打的log
     * @param th 异常的堆栈
     */
    public void error(String str, Throwable th) {
        error(str, null, th);
    }

    /**
     * 打level为error的log，支持占位符“{}”替换：</br>
     *   logger.errorMore("Detail info: name={}, age={}", name, age);
     * 
     * @param format  要打的log(包含占位符)
     * @param arguments 替换占位符的对象列表
     */
    public void errorMore(String format, Object... arguments) {
        this.error(format, arguments, null);
    }

    /**
     * 打level为error的log
     * 
     * @param str
     *            要打的log
     */
    private void error(String format, Object[] arguments, Throwable th) {
        formatLogMsg(Level.ERROR, format, arguments, th);
    }

    /**
     * 打level为fatal的log
     * 
     * @param str 要打的log
     */
    public void fatal(String str) {
        fatal(str, null, null);
    }

    /**
     * 打level为fatal的log，并将异常的堆栈也打出来
     * 
     * @param str 要打的log
     * @param th 异常的堆栈
     */
    public void fatal(String str, Throwable th) {
        fatal(str, null, th);
    }

    /**
     * 打level为fatal的log，支持占位符“{}”替换：</br>
     *   logger.fatalMore("Detail info: name={}, age={}", name, age);
     * 
     * @param format  要打的log(包含占位符)
     * @param arguments 替换占位符的对象列表
     */
    public void fatalMore(String format, Object... arguments) {
        fatal(format, arguments, null);
    }

    /**
     * 打level为fatal的log
     * 
     * @param str
     *            要打的log
     */
    private void fatal(String format, Object[] arguments, Throwable th) {
        formatLogMsg(Level.FATAL, format, arguments, th);
    }

    /**
     * 对日志信息进行格式化
     */
    private void formatLogMsg(Level level, String format, Object[] arguments, Throwable th) {
        LoggingEvent le = new LoggingEvent(this.name, format, arguments);
        //	Throwable throwableCandidate = MessageFormatter.getThrowableCandidate(arguments);
        //	if (throwableCandidate != null)
        //	{
        //
        //	}
        log(level, this.formatter.format(le), th);
    }

    // 日志内容放入队列，如果发现有本地日志没有配置本地文件的，放入DEFAULT_LOG的队列
    protected void log(Level level, String str, Throwable th) {
        // 只打比配置的level要高的log
        if (level.value < this.level.value) {
            return;
        }
        if (str == null) {
            str = "";
        }

        if (!isRemoteLog() && this.logPath == null) {
            // 输出到默认日志            
            //Logger.getLogger(DEFAULT_LOG).log(level, this.name + "\t" + str, th);
            putLogToDefault(level, str, th);
            return;
        }

        boolean result = logQueue.offer(new LogItem(level, str, th));
        if (!result) { // 队列满
                           //if (!DEFAULT_LOG.equals(this.name))
                       //{
                       //Logger.getLogger(DEFAULT_LOG).log(level, this.name + "\t" + str, th);
                       //}
            putLogToDefault(level, str, th);
        }
    }

    protected void putLogToDefault(Level level, String str, Throwable th) {
        if (!DEFAULT_LOG.equals(this.name)) {
            Logger.getLogger(DEFAULT_LOG).log(level, this.name + "\t" + str, th);
        }
    }

    // 打日志线程的回调方法
    public void doWriteLog() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        if (logQueue.isEmpty()) {
            return;
        }
        isEnabled = true;

        if (isRemoteLog()) {
            writeRemoteLog();
        } else {
            writeAllToLocal(logPath);
        }
    }

    /**
     * 打印远程log，这里向3GLogServer打印
     * @throws IOException
     */
    protected void writeRemoteLog() throws IOException {
        Pair<InetAddress, Integer> addr2 = getAddr();
        if (addr2 != null) {
            final int MAX_COUNT = 300;
            boolean empty = logQueue.isEmpty();
            while (!empty) {
                Socket s = m.get(addr2);
                LogPack lp = new LogPack();

                ArrayList<String> logs = new ArrayList<String>();
                int j = getLogs(logs, MAX_COUNT);
                multiEncode(lp, logs);

                if (j < MAX_COUNT) {
                    empty = true;
                }
                boolean succ = false;
                try {
                    int n = lp.client.Encode(remoteBuffer, remoteBuffer.length, 0);
                    if (s == null) {
                        s = new Socket();
                        s.setSoTimeout(2000);
                        s.connect(new InetSocketAddress(addr2.first, addr2.second), 2000);
                        m.put(addr2, s);
                    }
                    OutputStream os = s.getOutputStream();
                    os.write(remoteBuffer, 0, n);
                    os.flush();
                    InputStream is = s.getInputStream();
                    byte[] bs = new byte[4];
                    int r = is.read(bs);
                    if (r == 4) {
                        ByteBuffer b = ByteBuffer.wrap(bs);
                        r = b.getInt();
                        if (r > 0) {
                            succ = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("connection error: " + this.name + "//" + addr2.first + ":" + addr2.second);
                    e.printStackTrace();
                } finally {
                    if (!succ && failLogPath != null) {
                        writeToLocal(logs, failLogPath);
                        writeAllToLocal(failLogPath);
                    }
                    if (s != null) {
                        if (!succ) {
                            m.remove(addr2);
                            s.close();
                        }
                    }
                }
            }
        } else {
            //如果没有可用的logServer，就直接全部打到本地
            writeAllToLocal(logPath);
        }
    }

    /**
     * 将日志转换为字符串列表，每条日志结尾不加换行
     * @param logs
     * @param maxCount
     * @return
     */
    int getLogs(ArrayList<String> logs, int maxCount) {
        int j = 0;
        for (; j < maxCount && !logQueue.isEmpty(); j++) {
            LogItem item = logQueue.poll();
            logs.add(item.toStringNoEndReturn());
        }
        return j;
    }

    /**
     * 将日志转换为字符串列表，每条日志结尾加换行
     * @param logs
     * @param maxCount
     * @return
     */
    protected int getLogsWithEndline(ArrayList<String> logs, int maxCount) {
        int j = 0;
        for (; j < maxCount && !logQueue.isEmpty(); j++) {
            LogItem item = logQueue.poll();
            logs.add(item.toString());
        }
        return j;
    }

    //不压缩格式的编码
    private void multiEncode(LogPack lp, ArrayList<String> logs) throws IOException {
        lp.client.csWriteMulti.name = this.type + "/" + this.name;
        lp.client.Cmd = LogPack.command.WriteMulti;

        if (this.logPath != null) {
            writeToLocal(logs, this.logPath);
        }

        lp.client.csWriteMulti.content = logs;
    }

    /**
     * 每次访问逐个获取可用的服务器地址，以便均衡输出日志
     * 
     * @return
     */
    private Pair<InetAddress, Integer> getAddr() {
        if (logAddrs.size() == 0) {
            return null;
        } else if (logAddrs.size() == 1) {
            return logAddrs.get(0);

        }
        int seed = a.incrementAndGet() % logAddrs.size();
        return logAddrs.get(Math.abs(seed));
    }

    /**
     * 打印本地日志，如果打印失败就丢弃了
     * @param file
     */
    protected void writeAllToLocal(String file) throws UnsupportedEncodingException, FileNotFoundException,
                                                IOException {
        // local log
        boolean succ = false;
        if (file != null) {
            BufferedWriter bw = null;
            try {
                if (!logQueue.isEmpty()) {
                    bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file + "." + Util.getDateSimpleInfo(System.currentTimeMillis()), true),
                        "gbk"));
                    LogItem item = null;
                    while ((item = logQueue.poll()) != null) {
                        bw.write(item.toString());
                    }
                }
                succ = true;
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }
        }
        if (!succ) {
            LogItem item = null;
            while ((item = logQueue.poll()) != null) {
                System.out.print("\t" + item.toString());
            }
        }
    }

    // 打本地日志，如果失败，丢弃了
    protected void writeToLocal(ArrayList<String> content, String file) throws IOException {
        Writer bw = null;
        try {
            OutputStream os = null;
            try {
                os = new FileOutputStream(file + "." + Util.getDateSimpleInfo(System.currentTimeMillis()), true);
                bw = new BufferedWriter(new OutputStreamWriter(os, "gbk"));
                for (String s : content) {
                    bw.write(s);
                    bw.write("\n");
                }
                bw.flush();
                return;
            } catch (Exception e) {
            }
            // 如果输出file有问题，丢弃算了
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    /**
     * 发送心跳消息的接口，这个接口只给j4log伴侣使用，为避免用错，这个方法就不暴露了
     * 
     * @param content
     * @return
     * @throws Exception
     */
    boolean sendHearbeat(String content) throws Exception {
        Pair<InetAddress, Integer> logAddr = getAddr();
        if (null == logAddr) {
            return false;
        }

        Socket s = m.get(logAddr);
        boolean ret = false;
        try {
            if (s == null) {
                s = new Socket();
                s.setSoTimeout(2000);
                s.connect(new InetSocketAddress(logAddr.first, logAddr.second), 2000);
                m.put(logAddr, s);
            }
            OutputStream os = s.getOutputStream();
            LogPack lp = new LogPack();
            lp.client.csWriteMulti.name = this.type + "/" + this.name;
            lp.client.Cmd = LogPack.command.WriteMulti;
            lp.client.csWriteMulti.content.add(new LogItem(level, content, null).toStringNoEndReturn());
            int n = lp.client.Encode(remoteBuffer, remoteBuffer.length, 0);
            os.write(remoteBuffer, 0, n);
            os.flush();
            InputStream is = s.getInputStream();
            byte[] bs = new byte[4];
            int r = is.read(bs);
            if (r != 4) {
                return false;
            }
            ByteBuffer b = ByteBuffer.wrap(bs);
            r = b.getInt();
            if (r > 0) {
                ret = true;
            }
        } catch (Exception e) {
            System.err.println(
                "connection error: " + this.name + "//" + logAddr.first + ":" + logAddr.second + "\t" + content);
            e.printStackTrace();
            return false;
        } finally {
            if (!ret && null != s) {
                m.remove(logAddr);
                s.close();
            }
        }
        return ret;
    }

    // 下面全部是getter/setter方法

    protected String getFailLogPath() {
        return failLogPath;
    }

    /**
     * @deprecated
     * @see #getLogPath()
     */
    @Deprecated
    public String getFilePath() {
        return logPath;
    }

    /**
     * 读取日志级别
     * 
     * @Deprecated
     * @see #level()
     */
    public int getLevel() {
        return level.value;
    }

    Pair<InetAddress, Integer> getLogAddr() {
        return getAddr();
    }

    public String getLogPath() {
        return logPath;
    }

    public String getName() {
        return name;
    }

    protected Queue<LogItem> getQueue() {
        return logQueue;
    }

    public String getRemoteAddrStr() {
        if (logAddrs.size() > 0) {
            return "" + logAddrs;
        }
        return null;
    }

    String getType() {
        return type;
    }

    public boolean isDebugEnabled() {
        return Level.DEBUG.value >= this.level.value;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isInfoEnabled() {
        return Level.INFO.value >= this.level.value;
    }

    // 判断是否远程日志
    public boolean isRemoteLog() {
        return isRemoteLog;
    }

    /**
     * 读取日志级别
     * 
     * @return 日志级别
     */
    public Level level() {
        return level;
    }

    protected void setPattern(String pattern) {
        if (StringUtil.isEmpty(pattern)) {
            formatter = new PatternFormatter();
        } else {
            formatter = new PatternFormatter(pattern);
        }
    }

    protected void setFailLogPath(String path) {
        this.failLogPath = path;
    }

    public void setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException("Logger Level is null.");
        }
        this.level = level;
    }

    void setLogAddr(Pair<InetAddress, Integer> addr) {
        logAddrs = new ArrayList<Pair<InetAddress, Integer>>();
        logAddrs.add(addr);
        // this.logAddr = addr;
    }

    protected void setLogPath(String path) {
        this.logPath = path;
    }

    public void setRemoteAddr(InetAddress addr, int port) {
        logAddrs = new ArrayList<Pair<InetAddress, Integer>>();
        if (addr != null) {
            this.isRemoteLog = true;
            logAddrs.add(Pair.makePair(addr, port));
        }
    }

    /**
     * 设置日志在远程服务器上的模块名。服务器会在日志根目录上建一个目录，再把日志输出到这个目录里。<br/>
     * 
     * @param type
     */
    public void setRemoteType(String type) {
        this.type = type;
    }

    void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Logger [name=" + name + ", logPath=" + logPath + ", level=" + level + ", type=" + type
               + ", isRemoteLog=" + isRemoteLog + ", failLogPath=" + failLogPath + ", logAddrs=" + logAddrs
               + ", formatter=" + formatter + "]";
    }
}
