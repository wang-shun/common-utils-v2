/**
 * 
 */
package com.youzan.sz.jutil.jcache;

/**
 * @author bingyi
 *
 */

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.jcache.monitor.Visitor;
import com.youzan.sz.jutil.util.Pair;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.jcache.monitor.Visitor;
//import com.qq.jutil.util.Pair;

/**
 * 
 * @author meteor
 * 
 * @param <K>
 *          key的类型
 * @param <V>
 *          value的类型
 */
@SuppressWarnings("unchecked")
public abstract class NewAutoRefreshCache<K, V> extends AutoRefreshCacheBase<K, V> {
    private static final Logger logger = Logger.getLogger("jutil");

    protected final Map<K, V> map = new HashMap<K, V>();

    private volatile int queryCount = 0;

    private volatile int hitCount = 0;

    private String saveFilePath; // 持久化保存路径，如果为null，表示不需要持久化

    private boolean autoRecover = false; // 是否自动从持久化数据中恢复

    public long getReflushTime() {
        return reflushTime;
    }

    // private static final Map<String, Long> reflushTimeMap = new HashMap<String,
    // Long>();

    // protected synchronized static void collectReflushTime(String name, long t)
    // {
    // reflushTimeMap.put(name, t);
    // }

    private volatile long reflushTime = 0;

    private final Map<String, FutureTask<String>> futures = new HashMap<String, FutureTask<String>>();

    @SuppressWarnings("unused")
    private NewAutoRefreshCache() {
    }

    public NewAutoRefreshCache(Properties prop) {
        saveFilePath = prop.getProperty("save_file_path");
        if (saveFilePath != null && saveFilePath.length() == 0) saveFilePath = null;
        autoRecover = getPropertyBoolean(prop, "auto_recover", false);

        if (autoRecover) {
            long st = System.currentTimeMillis();
            recover(saveFilePath);
            logger.info(String.format("%s recovered within %d ms", saveFilePath, System.currentTimeMillis() - st));
        }
    }

    private static boolean getPropertyBoolean(Properties prop, String name, boolean def) {
        String s = prop.getProperty(name);
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)) return true;
        if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)) return false;
        return def;
    }

    protected void doReflush() {

        queryCount = 0;
        hitCount = 0;

        long reflushBegin = System.currentTimeMillis();

        /* Map<K, V> m = */reflush();

        long reflushEnd = System.currentTimeMillis();
        reflushTime = reflushEnd - reflushBegin;

        // if (m != null) map = m;
        save(saveFilePath);
    }

    /**
     * 刷新cache，重新生成cache
     * 
     * @return 返回新cache的数据
     */
    public abstract Map<K, V> reflush();

    public int cleanUp() {
        throw new UnsupportedOperationException("abandoned. do not use this any more, use requestClear() instead");
    }

    public void clear() {
        throw new UnsupportedOperationException("abandoned. do not use this any more, use requestClear() instead");
    }

    public String getReflushingStatus() {
        String r = null;

        synchronized (futures) {
            FutureTask<String> ft = futures.get(getClass().getSimpleName());
            if (null == ft) {
                if (getReflushTime() > 0) {
                    r = String.format("上次重刷花费%d ms.此后没有起动过%s的重刷", getReflushTime(), getClass().getSimpleName());
                } else {
                    r = String.format("没有起动过%s的重刷", getClass().getSimpleName());
                }
            } else {
                if (!ft.isDone()) {
                    logger.info(String.format("%s is reflushing, %d entries finished", getClass().getSimpleName(), size()));
                    r = String.format("%s正在重刷中...多刷无益，稍候吧...", getClass().getSimpleName());
                } else {
                    futures.remove(getClass().getSimpleName());
                    r = String.format("%s重刷完成，共%d项", getClass().getSimpleName(), size());
                }
            }
        }

        return r;
    }

    public V get(K key) {
        ++this.queryCount;
        V v = map.get(key);
        if (v == null) return null;
        ++this.hitCount;
        return v;
    }

    public boolean containsCache(K key) {
        return map.containsKey(key);
    }

    public V put(K key, V value) {
        return map.put(key, value);
    }

    /*
     * 请求清除。 bingyi, 2011/4/26
     */
    public synchronized String requestClear() {
        String r = null;

        boolean flag = true;
        FutureTask<String> ft = futures.get(getClass().getSimpleName());
        if (null == ft) {
            futures.put(getClass().getSimpleName(), ft = newReflushTask());
        } else {
            if (!ft.isDone()) {
                logger.info(String.format("%s is reflushing", getClass().getSimpleName()));
                flag = false;
            } else {
                futures.remove(getClass().getSimpleName());
                futures.put(getClass().getSimpleName(), ft = newReflushTask());
            }
        }
        r = String.format("重刷%s的请求已受理，这需要一点儿时间，稍候取结果吧:-)", getClass().getSimpleName());
        if (flag) CacheFactory2.REFLUSH_EXECUTOR.submit(futures.get(getClass().getSimpleName()));
        else r = String.format("%s正在重刷中；之前已经有人开始刷了，但还需要一点儿时间刷完，稍候取结果吧:-)", getClass().getSimpleName());

        return r;
    }

    private FutureTask<String> newReflushTask() {
        return new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                long st = System.currentTimeMillis();
                reflush();

                String s = String.format("%s reflushed. cost:%d ms", NewAutoRefreshCache.this.getClass().getSimpleName(),
                        System.currentTimeMillis() - st);
                logger.info(s);
                return s;
            }
        });
    }

    public V remove(K key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public int getQueryCount() {
        return this.queryCount;
    }

    public int getHitCount() {
        return this.hitCount;
    }

    public int getMemoryUsage() {
        /*
         * try { return MemoryUsage.getDeepMemoryUsage(this); } catch (Exception e)
         * { logger.error("", e); } return -1;
         */
        return 0;
    }

    public Iterator<Entry<K, V>> iterator() {
        return map.entrySet().iterator();
    }

    private boolean recover(String file) {
        if (file == null) return false;
        FileInputStream in = null;
        ObjectInputStream ois = null;
        File f = null;
        try {
            f = new File(file);
            if (!f.exists()) return false;
            logger.info("recover cache from file: " + file);
            in = new FileInputStream(f);
            ois = new ObjectInputStream(in);
            Pair<K, V> pr = null;
            while (true) {
                try {
                    pr = (Pair<K, V>) ois.readObject();
                } catch (EOFException e) {
                    break;
                }
                if (pr == null) break;
                put(pr.first, pr.second);
            }
        } catch (Throwable e) {
            logger.error(String.format("error recover file (%s), delete it", file), e);
            if (null != f) f.delete();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("close error: " + file, e);
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    logger.error("close error: " + file, e);
                }
            }
        }
        return true;
    }

    private boolean save(String file) {
        if (file == null) return false;
        logger.info("begin save cache to file: " + file);
        FileOutputStream outout = null;
        ObjectOutputStream oos = null;
        try {
            outout = new FileOutputStream(file);
            oos = new ObjectOutputStream(outout);
            Set<Entry<K, V>> entrySet = map.entrySet();
            logger.info("cache size: " + entrySet.size() + "\tfile:" + file);
            Iterator<Entry<K, V>> it = entrySet.iterator();
            while (it.hasNext()) {
                Entry<K, V> n = it.next();
                try {
                    oos.writeObject(Pair.makePair(n.getKey(), n.getValue()));
                } catch (Exception e) {
                    logger.error("writeObject to file error: ", e);
                }
            }
            oos.flush();
        } catch (Exception e) {
            logger.error("save to file error: ", e);
            return false;
        } finally {
            if (outout != null) {
                try {
                    outout.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
            logger.info("finish save cache to file: " + file);
        }

        return true;
    }

    public void accept(Visitor v, String name) {
        v.visitAutoRefreshCache(this, name);
    }
}
