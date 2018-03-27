package com.tming.common.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p> 低优先级的线程池</p>
 * <ul>
 *     <li>{@link #newLinkQueueCachedThreadPool(java.lang.String)}</li>
 *     <li>{@link #newLinkQueueFixedThreadPool(java.lang.String, int)}</li>
 * </ul>
 * <p> 栈模式队列的线程池 </p>
 * <p>
 *     使用一下两种方法创建
 *     <ul>
 *         <li>{@link #newStackQueueCachedThreadPool(String)}</li>
 *         <li>{@link #newStackQueueFixedThreadPool(String, int)}</li>
 *     </ul>
 * </p>
 * <p>Created by yusongying on 2014/11/5 11:52</p>
 */
public class TMExcutors {


    /**
     * 低优先级缓存线程池,线程闲置时间为60s
     *
     * @param threadPrefixName  线程名称前缀
     * @return
     */
    public static ThreadPoolExecutor newLinkQueueCachedThreadPool(String threadPrefixName) {
        return new ThreadPoolExecutor(3,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new LowerPriorityThreadFactory(threadPrefixName));
    }

    /**
     * 低优先级线程池, 线程闲置不回收
     *
     * @param threadPrefixName 线程名称前缀
     * @param nThreads 线程个数
     * @return
     */
    public static ExecutorService newLinkQueueFixedThreadPool(String threadPrefixName, int nThreads) {
        return new ThreadPoolExecutor(nThreads,
                nThreads,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new LowerPriorityThreadFactory(threadPrefixName));
    }

    /**
     *  创建一个栈模式队列的线程池
     *
     * @param threadPrefixName 线程名称前缀
     * @return 线程池
     */
    public static ThreadPoolExecutor newStackQueueCachedThreadPool(String threadPrefixName) {
        return new ThreadPoolExecutor(3,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new StackBlockingQueue<Runnable>(),
                new LowerPriorityThreadFactory(threadPrefixName));
    }

    /**
     * 创建一个栈模式队列的线程池
     *
     * @param threadPrefixName 线程名称前缀
     * @param  nThreads 线程池中线程的个数
     * @return 线程池
     */
    public static ExecutorService newStackQueueFixedThreadPool(String threadPrefixName,  int nThreads) {
        return new ThreadPoolExecutor(nThreads,
                nThreads,
                0,
                TimeUnit.SECONDS,
                new StackBlockingQueue<Runnable>(),
                new LowerPriorityThreadFactory(threadPrefixName));
    }

    /**
     * 低优先级的线程工厂
     */
    private static class LowerPriorityThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        LowerPriorityThreadFactory(String threadPrefixName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix =  " LowerPriorityThread-" + threadPrefixName + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.MIN_PRIORITY)
                t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }

}
