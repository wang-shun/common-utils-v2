package com.youzan.sz.jutil.nio.frame.test;
//

//import com.qq.jutil.nio.frame.Stage;
//import com.qq.jutil.nio.frame.Task;
//import com.qq.jutil.thread.FixThreadExecutor;

import com.youzan.sz.jutil.nio.frame.Stage;
import com.youzan.sz.jutil.nio.frame.Task;
import com.youzan.sz.jutil.thread.FixThreadExecutor;

class EchoHandler implements Stage {
    private Stage             next;

    private FixThreadExecutor tp = new FixThreadExecutor(10, 100000);

    public EchoHandler() {
    }

    public void setNext(Stage next) {
        this.next = next;
    }

    private void process(Task task) {
        next.pushTask(task);
    }

    public void pushTask(final Task task) {
        System.out.println("EchoHandler.pushTask");
        try {
            tp.execute(new Runnable() {
                public void run() {
                    process(task);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
