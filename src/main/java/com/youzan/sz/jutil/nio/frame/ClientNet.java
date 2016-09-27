package com.youzan.sz.jutil.nio.frame;

//import com.youzan.sz.jutil.j4log.Logger;
//import com.youzan.sz.jutil.nio.frame.Constant.*;
//import static com.qq.jutil.nio.frame.Constant.nlog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

final class ClientNet {
    private String      serverIp           = "0.0.0.0";

    private int         port               = 33306;

    private Selector    selector           = null;

    private Stage       handleStage;

    private long        SELECT_TIMEOUT     = 5;

    private TaskFactory tf;

    private Queue<Task> addToReaderQueue   = new ConcurrentLinkedQueue<Task>();

    private Queue<Task> changeToWriteQueue = new ConcurrentLinkedQueue<Task>();

    public ClientNet(String serverIp, int port, TaskFactory tf) {
        this.tf = tf;
        this.serverIp = serverIp;
        this.port = port;
    }

    public ClientNet(String serverIp, int port, TaskFactory tf, long selectTimeout) {
        this.tf = tf;
        this.serverIp = serverIp;
        this.port = port;
        this.SELECT_TIMEOUT = selectTimeout;
    }

    public void setHandleStage(Stage next) {
        this.handleStage = next;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClientNet, ").append("size: ").append(selector.keys().size());
        return sb.toString();
    }

    public void addToReader(Task task) {
        /*
        nlog.debug("ClientReader.pushTask");
        SocketChannel sc = task.getChannel();
        try
        {
        	selector.wakeup();
        	sc.register(selector, SelectionKey.OP_READ, task);
        }
        catch (Exception e)
        {
        	nlog.error("ClientReader.pushTask error: ", e);
        }
        /*/
        Constant.nlog.debug("ClientReader.pushTask");
        addToReaderQueue.add(task);
        selector.wakeup();
        //*/
    }

    public void changeToWriter(Task task) {
        /*
        nlog.debug("ClientWriter.pushTask");
        SocketChannel sc = task.getChannel();
        try
        {
            selector.wakeup();
            sc.register(selector, SelectionKey.OP_WRITE|SelectionKey.OP_READ, task);
        }
        catch (Exception e)
        {
            try
            {
                sc.close();
            }
            catch (IOException e1)
            {
            }
            nlog.error("ClientWriter.pushTask error: ", e);
        }
        /*/
        Constant.nlog.debug("ClientWriter.pushTask");
        changeToWriteQueue.add(task);
        selector.wakeup();
        //*/
    }

    private void addToReaderImpl(Task task) {
        SocketChannel sc = task.getChannel();
        try {
            sc.register(selector, SelectionKey.OP_READ, task);
        } catch (Exception e) {
            Constant.nlog.error("ClientReader.pushTask error: ", e);
        }
    }

    private void doAddToReader() {
        while (true) {
            Task task = addToReaderQueue.poll();
            if (task == null)
                break;
            addToReaderImpl(task);
            /*
            SocketChannel sc = task.getChannel();
            try
            {
            	sc.register(selector, SelectionKey.OP_READ, task);
            }
            catch (ClosedChannelException e)
            {
            	Constant.nlog.error("ClientReader.pushTask error: ", e);
            }
            */
        }
    }

    private void doChangeToWriter() {
        while (true) {
            Task task = changeToWriteQueue.poll();
            if (task == null)
                break;
            SocketChannel sc = task.getChannel();
            try {
                sc.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, task);
            } catch (Exception e) {
                try {
                    sc.close();
                } catch (IOException e1) {
                }
                Constant.nlog.error("ClientWriter.pushTask error: ", e);
            }
        }
    }

    public void startServer() throws IOException {
        selector = Selector.open();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(serverIp), port);
        ssc.socket().bind(address);

        // 使设定non-blocking的方式。
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while (true) // 不断的轮询
        {
            // Selector通过select方法通知我们我们感兴趣的事件发生了。
            int nKeys = selector.select(SELECT_TIMEOUT);
            try {
                doAddToReader();
                // 如果有我们注册的事情发生了，它的传回值就会大于0
                if (nKeys > 0) {
                    // Selector传回一组SelectionKeys
                    // 我们从这些key中的channel()方法中取得我们刚刚注册的channel。
                    process(selector.selectedKeys());
                }
            } catch (Exception e) {
                Constant.nlog.error("", e);
            } finally {
                doChangeToWriter();
            }
        }
    }

    private void process(Set<SelectionKey> selectedKeys) throws IOException {
        Constant.nlog.debug("read avaliable, keys in selector: " + selectedKeys.size());
        // Iterator<SelectionKey> i = selectedKeys.iterator();
        for (SelectionKey s : selectedKeys) {
            // SelectionKey s = (SelectionKey) i.next();

            // 一个key被处理完成后，就都被从就绪关键字（ready keys）列表中除去
            // i.remove();
            if (s.isAcceptable()) {
                // 从channel()中取得我们刚刚注册的channel。
                ServerSocketChannel ss = ((ServerSocketChannel) s.channel());
                SocketChannel sc = ss.accept();
                while (sc != null) {
                    Constant.nlog.debug("accept client: " + sc);
                    sc.configureBlocking(false);
                    Task task = tf.createTask(sc);
                    addToReaderImpl(task);
                    sc = ss.accept();
                }
                continue;
            }

            Task task = (Task) s.attachment();
            SocketChannel sc = task.getChannel();
            if (sc != s.channel()) {
                s.cancel();
                s.channel().close();
                sc.close();
                Constant.nlog.error("error! task's channel != select's channel");
                continue;
            }
            Constant.nlog.debug("------interestOps: " + s.interestOps() + "\t" + s.readyOps() + "\t" + sc);
            if (s.isWritable()) {
                try {
                    int n = task.writeToClient();
                    Constant.nlog.debug("write " + n + " bytes to client." + task.getChannel());
                    if (n <= 0) {
                        Constant.nlog.debug("client's socket closed.");
                        s.cancel();
                        sc.close();
                        continue;
                    }
                    if (task.isWriteFinish()) {
                        Constant.nlog.debug("client write finish.");
                        task.reset();
                        // s.interestOps(0);
                        addToReaderImpl(task);
                        // sc.register(selector, 0, task);
                        // next.pushTask(task);
                    }
                } catch (Exception e) {
                    Constant.nlog.error("", e);
                    s.cancel();
                    sc.close();
                }
            } else if (s.isReadable()) {
                try {
                    int n = task.readFromClient();
                    Constant.nlog.debug("read " + n + " bytes from client." + task.getChannel());
                    if (n <= 0) {
                        Constant.nlog.debug("client's socket closed.");
                        s.cancel();
                        sc.close();
                        continue;
                    }
                    if (task.isReadFinish()) {
                        Constant.nlog.debug("client read finish.");
                        s.interestOps(0);
                        Constant.nlog.debug("interestOps: " + s.interestOps() + "\t" + sc);
                        // sc.register(selector, 0, task);
                        handleStage.pushTask(task);
                    }
                } catch (Exception e) {
                    Constant.nlog.error("", e);
                    s.cancel();
                    sc.close();
                }
            }
        }
        selectedKeys.clear();
        /*
        Constant.nlog.debug("read avaliable, keys in selector: "
        		+ selectedKeys.size());
        Iterator<SelectionKey> i = selectedKeys.iterator();
        while (i.hasNext())
        {
        	SelectionKey s = (SelectionKey) i.next();
        
        	// 一个key被处理完成后，就都被从就绪关键字（ready keys）列表中除去
        	i.remove();
        	if (s.isAcceptable())
        	{
        		// 从channel()中取得我们刚刚注册的channel。
        		ServerSocketChannel ss = ((ServerSocketChannel) s.channel());
        		SocketChannel sc = ss.accept();
        		while(sc != null){
        			Constant.nlog.debug("accept client: " + sc);
        			sc.configureBlocking(false);
        			Task task = tf.createTask(sc);
        			addToReaderImpl(task);
        			sc = ss.accept();
        		}
        		continue;
        	}
        	
        	Task task = (Task) s.attachment();
        	SocketChannel sc = task.getChannel();
        	if(sc != s.channel())
        	{
        		s.cancel();
        		s.channel().close();
        		sc.close();
        		Constant.nlog.error("error! task's channel != select's channel");
        		continue;
        	}
        	Constant.nlog.debug("------interestOps: " + s.interestOps() + "\t" + s.readyOps() + "\t" + sc);
        	if (s.isWritable())
        	{
        		try{
        			int n = task.writeToClient();
        			Constant.nlog.debug("write " + n + " bytes to client." + task.getChannel());
        			if(n <= 0)
        			{
        				Constant.nlog.debug("client's socket closed.");
        				s.cancel();
        				sc.close();
        				continue;
        			}
        			if(task.isWriteFinish())
        			{
        				Constant.nlog.debug("client write finish.");
        				task.reset();
        				//s.interestOps(0);
        				addToReaderImpl(task);
        				//sc.register(selector, 0, task);
        				//next.pushTask(task);
        			}
        		}catch(Exception e){
        			Constant.nlog.error("", e);
        			s.cancel();
        			sc.close();
        		}
        	}
        	else if (s.isReadable())
        	{
        		try{
        			int n = task.readFromClient();
        			Constant.nlog.debug("read " + n + " bytes from client." + task.getChannel());
        			if (n <= 0)
        			{
        				Constant.nlog.debug("client's socket closed.");
        				s.cancel();
        				sc.close();
        				continue;
        			}
        			if (task.isReadFinish())
        			{
        				Constant.nlog.debug("client read finish.");
        				s.interestOps(0);
        				Constant.nlog.debug("interestOps: " + s.interestOps() + "\t" + sc);
        				//sc.register(selector, 0, task);
        				handleStage.pushTask(task);
        			}
        		}catch(Exception e){
        			Constant.nlog.error("", e);
        			s.cancel();
        			sc.close();
        		}
        	}
        }
        */
    }
}
