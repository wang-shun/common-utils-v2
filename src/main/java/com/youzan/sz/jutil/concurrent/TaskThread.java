package com.youzan.sz.jutil.concurrent;

/**
 * A Thread implementation that records the time at which it was created.
 * 
 */
class TaskThread extends Thread {

    private final long creationTime;

    public TaskThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        this.creationTime = System.currentTimeMillis();
    }

    public TaskThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
        this.creationTime = System.currentTimeMillis();
    }

    /**
     * @return the time (in ms) at which this thread was created
     */
    public final long getCreationTime() {
        return creationTime;
    }

}
