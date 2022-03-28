// Copyright (C), 2019-2022, Tim.WJ
// FileName   : XThreadPoolManager
// Author     : Tim.WJ
// Create At  : 2022/3/22 23:44
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
package com.smartwasp.xtask

import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.annotation.NonNull
import com.orhanobut.logger.Logger
import com.smartwasp.xtask.factory.XThreadFactory
import java.util.concurrent.*


/**
 *
 * @ProjectName: Guardian
 * @Package: com.smartwasp.xtask
 * @ClassName: XThreadTaskManager
 * @Description:自定义线程池管理类
 * 获取io型，cpu型，单核的线程池，和自定义的线程池
 *     支持打印线程池的状态，支持schedule，支持future。支持关闭
 * CPU 密集型程序的最佳线程数 最佳线程数 = CPU 核数（逻辑）+ 1（经验值）
 * I/O 密集型程序的最佳线程数就是 最佳线程数 = ((线程等待时间+线程CPU时间)/线程CPU时间)* CPU数目
 *                                    = CPU核心数 * (1+CPU利用率)
 *                                    = CPU核心数 * (1 + (线程等待时间/CPU耗时))
 * Executors 返回线程池对象的弊端如下：
 * FixedThreadPool 和 SingleThreadExecutor ：
 *     允许请求的队列长度为 Integer.MAX_VALUE,可能堆积大量的请求，从而导致 OOM。
 * CachedThreadPool 和 ScheduledThreadPool ：
 *     允许创建的线程数量为 Integer.MAX_VALUE ，可能会创建大量线程，从而导致 OOM。
 * @Author: Tim.WJ
 * @CreateDate: 2022/3/28 11:55
 * @UpdateUser:Tim.WJ
 * @UpdateDate: 2022/3/28 11:55
 * @UpdateRemark:
 * @Version: 1.0
 */
class XThreadTaskManager {

    companion object {
        val instance: XThreadTaskManager
            get() = SingletonHolder.holder
        const val TAG = "XThreadTaskManager"
        const val DUMP_TASK = "dumpTask"
    }

    private object SingletonHolder {
        val holder = XThreadTaskManager()
    }

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
    private val QUEUE_CAPACITY = 128

    private val mDiskIO: Executor? by lazy { null }

    private val mNetworkIO: Executor? by lazy { null }

    private val mMainThread: Executor? by lazy { MainThreadExecutor() }

    /**
     * 线程池map
     */
    private var threadPoolMap = hashMapOf<String, ThreadPoolExecutor>()

    init {
        // 打印的线程池
        addThreadPool(
            DUMP_TASK, Process.THREAD_PRIORITY_BACKGROUND,
            ScheduledThreadPoolExecutor(
                1,
                // 如果队列满了，会将最早进入队列的任务删掉腾出空间，再尝试加入队列
                ThreadPoolExecutor.DiscardOldestPolicy()
            )
        )
    }

    /**
     *   @param tag 针对每个TAG 获取对应的线程池
     *   @param corePoolSize  线程池中核心线程的数量 除非allowCoreThreadTimeOut被设置为true，否则它闲着也不会死
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
    private fun getThreadPool(tag: String, priority: Int = Process.THREAD_PRIORITY_DEFAULT):
            ThreadPoolExecutor {
        var threadPoolExecutor = threadPoolMap[tag]
        if (threadPoolExecutor == null) {
            threadPoolExecutor = ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY),
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

    private fun addThreadPool(
        taskName: String,
        priority: Int = Process.THREAD_PRIORITY_DEFAULT,
        _threadPoolExecutor: ThreadPoolExecutor
    ): ThreadPoolExecutor? {
        val tempThreadPoolExecutor = threadPoolMap[taskName]
        tempThreadPoolExecutor ?: kotlin.run {
            threadPoolMap[taskName] = _threadPoolExecutor.apply {
                threadFactory = XThreadFactory(taskName, priority)
                allowCoreThreadTimeOut(true)
            }
            Logger.t(TAG).d("addThreadPool for $taskName")
        }
        return tempThreadPoolExecutor
    }

    /**
     *  获取自带的自定义线程池,不关闭
     *  @param tag 针对每个TAG 获取对应的线程池
     *  @param runnable 对应的 runnable 任务
     * */
    fun addTask(tag: TaskType, runnable: Runnable) {
        getThreadPool(tag.type).execute(runnable)
    }

    /**
     *  获取自定义线程池,需要手动关闭
     *  @param tag 针对每个TAG 获取对应的线程池
     *  @param runnable 对应的 runnable 任务
     * */
    fun addCustomTask(tag: String, runnable: Runnable) {
        getThreadPool(tag).execute(runnable)
    }

    /**
     * 关闭自定义线程池
     */
    fun removeCustomTask(tag: String, runnable: Runnable) {
        // todo
    }

    /**
     *  获取自定义定时线程池,需要手动关闭
     *  @param tag 针对每个TAG 获取对应的线程池
     *  @param runnable 对应的 runnable 任务
     * */
    fun addCustomScheduleTask(tag: String, runnable: Runnable) {
        getThreadPool(tag).execute(runnable)
    }

    /**
     * 关闭自定义线程池
     */
    fun removeCustomScheduleTask(tag: String, runnable: Runnable) {
    // todo
    }


    /**
     * 打印当前线程池的状态
     */
    fun dumpCurrentTasks() {
        val dumpStr = StringBuffer("线程池信息\n")
        threadPoolMap.forEach { (poolName, threadPoolExecutor) ->
            run {
                dumpStr.append("ThreadPool name          : ${poolName}\n")
                dumpStr.append("ThreadPool Size          : ${threadPoolExecutor.poolSize}\n")
                dumpStr.append("Active Threads           : ${threadPoolExecutor.activeCount}\n")
                dumpStr.append("Number of Tasks Completed: ${threadPoolExecutor.completedTaskCount}\n")
                dumpStr.append("Number of Tasks in Queue : ${threadPoolExecutor.queue.size}\n")
            }
        }
        dumpStr.append("\n")
        Logger.t(TAG).i(dumpStr.toString())
    }

    /**
     * 开启打印
     */
    @Synchronized
    fun startDumpTasks() {
        try {
            threadPoolMap[DUMP_TASK] ?: kotlin.run {
                val tempScheduledThreadPoolExecutor = addThreadPool(
                    DUMP_TASK, Process.THREAD_PRIORITY_BACKGROUND,
                    ScheduledThreadPoolExecutor(
                        1,
                        // 如果队列满了，会将最早进入队列的任务删掉腾出空间，再尝试加入队列
                        ThreadPoolExecutor.DiscardOldestPolicy()
                    )
                ) as ScheduledThreadPoolExecutor

                tempScheduledThreadPoolExecutor.scheduleAtFixedRate(
                    { dumpCurrentTasks() },
                    0,
                    1,
                    TimeUnit.SECONDS
                )
            }
        } catch (e: Exception) {
            Logger.t(TAG).e("startDumpTasks exception ${e.message}")
        } finally {
            Logger.t(TAG).i("startDumpTasks success")
        }
    }

    /**
     * 停止打印
     */
    @Synchronized
    fun stopDumpTasks() {
        threadPoolMap[DUMP_TASK]?.let {
            try {
                it.shutdown()
                // (所有的任务都结束的时候，返回TRUE)
                if (!it.awaitTermination(0, TimeUnit.MILLISECONDS)) {
                    // 超时的时候向线程池中所有的线程发出中断(interrupted)。
                    it.shutdownNow()
                }
            } catch (e: Exception) {
                Logger.t(TAG).e("stopDumpTasks exception ${e.message}")
            } finally {
                it.shutdownNow()
                threadPoolMap.remove(DUMP_TASK)
                Logger.t(TAG).i("stopDumpTasks success")
            }
        }
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
        override fun execute(@NonNull command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

}