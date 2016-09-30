package com.youzan.sz.dqueue;

import com.youzan.dqueue.client.PopHandler;
import com.youzan.dqueue.client.PopTask;
import com.youzan.dqueue.client.entity.Response;
import com.youzan.dqueue.client.utils.HttpUtils;
import com.youzan.sz.common.util.current.ExceptionThreadFactory;
import com.youzan.sz.dqueue.codec.Decode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by wangpan on 2016/9/30.
 */
public abstract class AbstractDqSubClent<T> extends AbstractDqClient {

    private static final Logger LOGGER     = LoggerFactory.getLogger(AbstractDqSubClent.class);

    /**解码 */
    private Decode              decode;

    private Class<T>            z;

    private ExecutorService     popExecutor;
    private List<PopTask>       taskList;

    /**是否自动删除key,请在处理完成后再删除*/
    private boolean             autoDelete = false;

    public AbstractDqSubClent(String dequeueURL, String chanel, Class<T> z) {
        super(dequeueURL, chanel);
        this.z = z;
    }

    /**用默认线程池always取数据,一定要判取的数据是否有返回*/
    public void popAlways() {
        dq.popAlways(this.getChanel(), getDqPopHandler());
    }

    /**
     * 使用自己的线程池来完成
     * @param coreThreads
     * @param maxThreads
     * @param threadFactory
     */
    public void popAlways(int coreThreads, int maxThreads, ThreadFactory threadFactory) {
        taskList = new ArrayList<PopTask>();

        if (threadFactory == null) {
            threadFactory = new ExceptionThreadFactory(new ExceptionThreadFactory.ExceptionHandler());
        }

        popExecutor = new ThreadPoolExecutor(coreThreads, maxThreads, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), threadFactory);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (PopTask task : taskList) {
                    task.stop();
                }
                popExecutor.shutdown();
                while (!popExecutor.isTerminated()) {

                }
                HttpUtils.close();
            }
        });

        PopTask task = new PopTask(this.dq, this.getChanel(), getDqPopHandler());
        taskList.add(task);
        popExecutor.submit(task);
    }

    private PopHandler getDqPopHandler() {
        PopHandler handler = new PopHandler() {
            @Override
            public void dealJob(Response response) throws Throwable {

                if (response.isSuccess()) {
                    LOGGER.debug("dq pop msg key={},value={}", response.getKey(),response.getValue());
                    if (!StringUtils.isEmpty(response.getKey())) {
                        try {
                           T obj = null;
                            if (decode != null) {

                                if(z !=null && ! (z == String.class) ){
                                    obj = decode.decode(response.getValue(), z);
                                }else {
                                    obj = (T) response.getValue();
                                }
                            }else {
                                obj = (T)response.getValue();
                            }
                            if (linkedDqHandler != null) {
                                linkedDqHandler.handler(response.getKey(),obj);
                            }
                            if (autoDelete) {
                                dq.delete(response.getKey());
                            }
                        } catch (Exception e) {
                            LOGGER.error("handler dq msg key ={},value={},error={}", response.getKey(),
                                response.getValue(), e);
                        }
                    }
                } else if(StringUtils.isNotEmpty(response.getKey())){
                    LOGGER.error("pop dequeue msg error={},", response.getError());
                }
            }
        };
        return handler;
    }

    public Decode getDecode() {
        return decode;
    }

    public void setDecode(Decode decode) {
        this.decode = decode;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }
}
