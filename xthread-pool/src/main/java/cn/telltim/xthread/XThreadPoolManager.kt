//
// Copyright (C), 2019-2022, Tim.WJ
// FileName   : XThreadPoolManager
// Author     : Tim.WJ
// Create At  : 2022/3/22 23:44
// History:
// <author> <time> <version> <desc>
// 作者姓名 修改时间 版本号 描述
//

/**
 * 线程的几个状态
 * 1 初始状态
 *      线程被实例化后，会进入初始状态
 * 2 可运行状态
 *     线程处于运行就绪状态，等待操作系统的调度程序选中进入运行态。以下几种情况会使线程进入可运行状态
 *     a: 调用线程的start
 *     b: 当前线程sleep结束;其他线程join结束;等待用户输入完毕; 锁池中的线程拿到对象锁
 *     c: 当前线程时间片用完，
 *     d: 调用线程yield方法,放弃cpu的时间片,进入等待线程调度重新分配时间片
 * 3 运行状态
 *     线程调度程序从可运行池中选择一个线程作为当前线程时线程所处的状态。这也是线程进入运行状态的唯一一种方式
 * 4 死亡状态
 *     在一个死去的线程上调用start()方法，会抛出java.lang.IllegalThreadStateException异常，以下方式，
 *   线程会进入死亡状态
 *     a: 当线程的run()方法完成时
 *     b: 主线程的main()方法完成时
 * 5 阻塞状态
 *   以下情况线程会进入阻塞状态
 *   a: 当前线程调用Thread.sleep()方法
 *   b: 运行在当前线程里的其它线程t2调用join()方法，当前线程进入阻塞状态
 *   c: 等待用户输入的时候，当前线程进入阻塞状态
 *   d: 等待队列
 *     当前线程拿到object的锁后,在synchronized锁中调用object的wait方法，当前线程会释放object所持有的锁进入等待队列，
 *   e: 锁池状态(同步锁)
 *     锁池里的其他线程争抢到object的锁，进入synchronized中,
 *         调用object的notify，等待队列中的一个线程会被唤醒进入锁池队列
 *         调用object的nofityAll,唤醒等待队列中的所有线程，所有线程将进入锁池
 *     synchronized完成后，所持中的所有线程将会去争抢object的锁。
 *     当前线程想调用对象A的同步方法时，发现对象A的锁被别的线程占有，此时当前线程进入锁池状态。简言之，锁池里面放的都是想争夺对象锁的线程。
 *     当一个线程1被另外一个线程2唤醒时，1线程进入锁池状态，去争夺对象锁。
 *     锁池是在同步的环境下才有的概念，一个对象对应一个锁池
 */

package cn.telltim.xthread

import cn.telltim.xthread.factory.XThreadFactory
import com.orhanobut.logger.Logger
import java.util.concurrent.*


/**
 * @Author     : Tim.WJ
 * @Description: 线程池管理
 * 功能介绍
 * 获取io型，cpu型，单核的线程池，打印线程池的状态，线程支持schedule，支持future。支持关闭
 */
class XThreadPoolManager {

    companion object {
        val instance: XThreadPoolManager
            get() = SingletonHolder.holder
        val TAG = "XThreadPoolManager"
    }

    private object SingletonHolder {
        val holder = XThreadPoolManager()
    }

    /**
     * 线程池map
     */
    private var threadPoolMap = hashMapOf<String, ThreadPoolExecutor>()

    /**
     * cpu数量
     * */
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    /**
     * 核心线程数为手机CPU数量+1
     * */
    private val CORE_POOL_SIZE = CPU_COUNT + 1

    /**
     * 允许线程空闲时间（单位：默认为秒）
     */
    private val KEEP_ALIVE_TIME = 60L

    /**
     * 最大线程数为手机CPU数量×2+1
     * */
    private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1

    /**
     * 缓冲队列数
     */
    private val QUEUE_CAPACITY = 200

    /**
     *   @param tag 针对每个TAG 获取对应的线程池
     *   @param corePoolSize  线程池中核心线程的数量
     *   @param maximumPoolSize  线程池中最大线程数量，等待队列的任务塞满了之后，才会触发开启非核心线程，
     *          直到总线程数达到 maximumPoolSize
     *   @param keepAliveTime 非核心线程的超时时长，
     *          当系统中非核心线程闲置时间超过keepAliveTime之后，则会被回收
     *          如果ThreadPoolExecutor的allowCoreThreadTimeOut属性设置为true，则该参数也作用于核心线程的超时时长
     *   @param unit 第三个参数的单位，有纳秒、微秒、毫秒、秒、分、时、天等
     *   @param queueSize 等待队列的长度 一般128 (参考 AsyncTask)
     *          workQueue 线程池中的任务队列，该队列主要用来存储已经被提交但是尚未执行的任务。
     *   存储在这里的任务是由ThreadPoolExecutor的execute方法提交来的。
     *   threadFactory  为线程池提供创建新线程的功能，这个我们一般使用默认即可
     *
     *   1.ArrayBlockingQueue：这个表示一个规定了大小的BlockingQueue，ArrayBlockingQueue的构造函数接受一个int类型的数据，
     *              该数据表示BlockingQueue的大小，存储在ArrayBlockingQueue中的元素按照FIFO（先进先出）的方式来进行存取。
     *   2.LinkedBlockingQueue：这个表示一个大小不确定的BlockingQueue，在LinkedBlockingQueue的构造方法中可以传
     *          一个int类型的数据，这样创建出来的LinkedBlockingQueue是有大小的，也可以不传，不传的话，
     *          LinkedBlockingQueue的大小就为Integer.MAX_VALUE
     * */
    private fun getThreadPool(tag: String):
            ThreadPoolExecutor {
        var threadPoolExecutor = threadPoolMap[tag]
        if (threadPoolExecutor == null) {
            threadPoolExecutor = ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY),
                XThreadFactory( tag),
                RejectedExecutionHandler { _, _threadPoolExecutor ->
                    Logger.t("XThreadPoolManager").w(
                        "$_threadPoolExecutor  " +
                                "RejectedExecutionHandler"
                    )
                }
            )
            //允许核心线程闲置超时时被回收
            threadPoolExecutor.allowCoreThreadTimeOut(true)
            threadPoolMap[tag] = threadPoolExecutor
        }
        return threadPoolExecutor
    }

    /**
     *  @param tag 针对每个TAG 获取对应的线程池
     *  @param runnable 对应的 runnable 任务
     * */
    fun removeTask(tag: String, runnable: Runnable) {
        if (threadPoolMap[tag] == null){
            Logger.t(TAG).i("the $tag task is not exist,yet")
        }else {
            getThreadPool(tag)?.queue?.remove(runnable)
        }
    }

    /**
     *  @param tag 针对每个TAG 获取对应的线程池
     *  @param runnable 对应的 runnable 任务
     * */
    fun addTask(tag: String, runnable: Runnable) {
        getThreadPool(tag).execute(runnable)
    }

    /**
     *   @param tag 针对每个TAG 获取对应的线程池
     *   取消 移除线程池
     * */

    //shutDown()：关闭线程池后不影响已经提交的任务
    //shutDownNow()：关闭线程池后会尝试去终止正在执行任务的线程
    fun exitThreadPool(tag: String) {
        val threadPoolExecutor = threadPoolMap[tag]
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdownNow()
            threadPoolMap.remove(tag)
        }
    }

    fun dumpCurrentTasks(){
        threadPoolMap.forEach({
            val scheduledExecutorService: ScheduledExecutorService = ScheduledThreadPoolExecutor(
                1,
                createThreadFactory("print-thread-pool-status", false)
            )
            scheduledExecutorService.scheduleAtFixedRate({
                log.info("=========================")
                log.info("ThreadPool Size: [{}]", threadPool.getPoolSize())
                log.info("Active Threads: {}", threadPool.getActiveCount())
                log.info("Number of Tasks : {}", threadPool.getCompletedTaskCount())
                log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size())
                log.info("=========================")
            }, 0, 1, TimeUnit.SECONDS)
        })
    }
}