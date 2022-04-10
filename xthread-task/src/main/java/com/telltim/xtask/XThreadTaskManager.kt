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

/*　
*  几个重要的参数
*   corePoolSize  线程池中核心线程的数量 除非allowCoreThreadTimeOut被设置为true，否则它闲着也不会死
*   maximumPoolSize  线程池中最大线程数量，等待队列的任务塞满了之后，才会触发开启非核心线程，
*   直到总线程数达到 maximumPoolSize
*   keepAliveTime 非核心线程的超时时长，
*   当系统中非核心线程闲置时间超过keepAliveTime之后，则会被回收
*   如果ThreadPoolExecutor的allowCoreThreadTimeOut属性设置为true，则该参数也作用于核心线程的超时时长
*   unit 第三个参数的单位，有纳秒、微秒、毫秒、秒、分、时、天等
*   queueSize 等待队列的长度 一般128 (参考 AsyncTask)
*          workQueue 线程池中的任务队列，该队列主要用来存储已经被提交但是尚未执行的任务。
*   存储在这里的任务是由ThreadPoolExecutor的execute方法提交来的。
*   threadFactory  为线程池提供创建新线程的功能，这个我们一般使用默认即可
*
*   1.ArrayBlockingQueue：这个表示一个规定了大小的BlockingQueue，ArrayBlockingQueue的构造函数接受一个int类型的数据，
*              该数据表示BlockingQueue的大小，存储在ArrayBlockingQueue中的元素按照FIFO（先进先出）的方式来进行存取。
*   2.LinkedBlockingQueue：这个表示一个大小不确定的BlockingQueue，在LinkedBlockingQueue的构造方法中可以传
*          一个int类型的数据，这样创建出来的LinkedBlockingQueue是有大小的，也可以不传，不传的话，
*          LinkedBlockingQueue的大小就为Integer.MAX_VALUE
* 形象的比喻如下:
* 比如去火车站买票, 有10个售票窗口, 但只有5个窗口对外开放. 那么对外开放的5个窗口称为核心线程数,
* 而最大线程数是10个窗口.
* 如果5个窗口都被占用, 那么后来的人就必须在后面排队, 但后来售票厅人越来越多, 已经人满为患, 就类似于线程队列已满.
* 这时候火车站站长下令, 把剩下的5个窗口也打开, 也就是目前已经有10个窗口同时运行. 后来又来了一批人,
* 10个窗口也处理不过来了, 而且售票厅人已经满了, 这时候站长就下令封锁入口,不允许其他人再进来, 这就是线程异常处理策略.
* 而线程存活时间指的是, 允许售票员休息的最长时间, 以此限制售票员偷懒的行为.
*/
package com.telltim.xtask

import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.annotation.NonNull
import com.orhanobut.logger.Logger
import com.telltim.xtask.exception.InvalidTaskTypeException
import com.telltim.xtask.exception.MaxThreadPoolSizeException
import com.telltim.xtask.factory.XThreadFactory
import java.util.concurrent.*


/**
 *
 * @ProjectName: Guardian
 * @Package: com.smartwasp.xtask
 * @ClassName: XThreadTaskManager
 * @Description:自定义线程池管理类
 * 获取io型，cpu型，单核的线程池，和自定义的线程池
 *     支持打印线程池的状态，支持schedule，支持future。支持关闭
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
        private const val TAG = "XThreadTaskManager"
        private const val DUMP_TASK = "dumpTask"
    }

    private object SingletonHolder {
        val holder = XThreadTaskManager()
    }

    /**
     * cpu数量
     * */
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    /**
     * 假定数据库的CPU的利用率是1/2,则CPU_CORE_SIZE = CPU_COUNT*(1 + 1/0.5)
     */
    private val DB_THREAD_POOL_SIZE = 3 * CPU_COUNT

    /**
     * 假定数据库的CPU的利用率是1,则CPU_CORE_SIZE = CPU_COUNT*(1 + 1/1)
     */
    private val NET_THREAD_POOL_SIZE = 2 * CPU_COUNT

    /**
     * 假定数据库的CPU的利用率是1/3,则CPU_CORE_SIZE = CPU_COUNT*(1 + 1/0.33)
     */
    private val FILE_THREAD_POOL_SIZE = 4 * CPU_COUNT

    /**
     * CPU密集型的核心线程池大小
     */
    private val CPU_THREAD_POOL_SIZE = CPU_COUNT + 1

    /**
     * 假定用户自定义线程池的CPU的利用率是1,则CPU_CORE_SIZE = CPU_COUNT*(1 + 1/1)
     */
    private val CUSTOM_THREAD_POOL_SIZE = 2 * CPU_COUNT

    /**
     * 缓冲队列数
     */
    private val QUEUE_CAPACITY = 128

    /**
     * 数据库的线程池
     */
    private var mDbIOExecutor: ThreadPoolExecutor?
        get() = null

    /**
     * 网络的线程池
     */
    private var mNetworkIOExecutor: ThreadPoolExecutor?
        get() = null

    /**
     * 文件io的线程池
     */
    private var mFileThreadPoolExecutor: ThreadPoolExecutor?
        get() = null

    /**
     * CPU密集型的线程池
     */
    private var cpuThreadPoolExecutor: ThreadPoolExecutor?
        get() = null

    /**
     * UI主线程
     */
    val mMainThread: Executor by lazy { MainThreadExecutor() }

    /**
     * 线程池map
     */
    private var threadPoolMap = hashMapOf<String, ThreadPoolExecutor>()

    /**
     * CPU 密集型程序的最佳线程数 最佳线程数 = CPU 核数(逻辑)+ 1(经验值),可以视为线程等待时间近乎0
     * I/O 密集型程序的最佳线程数就是 最佳线程数 = ((线程等待时间 + 线程CPU时间)/线程CPU时间)* CPU数目
     *                                    = CPU核心数 * (1 + (线程等待时间/线程CPU时间))
     */
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
        mDbIOExecutor = generateThreadPoolExecutor(TaskType.TYPE_DB)
        mFileThreadPoolExecutor = generateThreadPoolExecutor(TaskType.TYPE_FILE)
        mNetworkIOExecutor = generateThreadPoolExecutor(TaskType.TYPE_NETWORK)
        cpuThreadPoolExecutor = generateThreadPoolExecutor(TaskType.TYPE_CPU)
    }

    private fun generateThreadPoolExecutor(taskType: TaskType): ThreadPoolExecutor {
        when (taskType) {
            TaskType.TYPE_CPU -> {
                return addThreadPool(taskType.type, Process.THREAD_PRIORITY_DEFAULT,
                    ThreadPoolExecutor(
                        CPU_THREAD_POOL_SIZE,
                        CPU_COUNT + CPU_THREAD_POOL_SIZE,
                        0L,
                        TimeUnit.MILLISECONDS,
                        ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY),
                        RejectedExecutionHandler { _, _threadPoolExecutor ->
                            Logger.t(TAG).w(
                                "[${taskType.type}]:$_threadPoolExecutor  " +
                                        "RejectedExecutionHandler"
                            )
                        }
                    ))
            }
            TaskType.TYPE_DB -> {
                return addThreadPool(taskType.type, Process.THREAD_PRIORITY_DEFAULT,
                    ThreadPoolExecutor(
                        DB_THREAD_POOL_SIZE,
                        CPU_COUNT + DB_THREAD_POOL_SIZE,
                        0L,
                        TimeUnit.MILLISECONDS,
                        ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY),
                        RejectedExecutionHandler { _, _threadPoolExecutor ->
                            Logger.t(TAG).w(
                                "[${taskType.type}]:$_threadPoolExecutor  " +
                                        "RejectedExecutionHandler"
                            )
                        }
                    ))
            }
            TaskType.TYPE_NETWORK -> {
                return addThreadPool(taskType.type, Process.THREAD_PRIORITY_DEFAULT,
                    ThreadPoolExecutor(
                        NET_THREAD_POOL_SIZE,
                        CPU_COUNT + NET_THREAD_POOL_SIZE,
                        0L,
                        TimeUnit.MILLISECONDS,
                        ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY),
                        RejectedExecutionHandler { _, _threadPoolExecutor ->
                            Logger.t(TAG).w(
                                "[${taskType.type}]:$_threadPoolExecutor  " +
                                        "RejectedExecutionHandler"
                            )
                        }
                    ))
            }
            TaskType.TYPE_FILE -> {
                return addThreadPool(taskType.type, Process.THREAD_PRIORITY_DEFAULT,
                    ThreadPoolExecutor(
                        FILE_THREAD_POOL_SIZE,
                        CPU_COUNT + FILE_THREAD_POOL_SIZE,
                        0L,
                        TimeUnit.MILLISECONDS,
                        ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY),
                        RejectedExecutionHandler { _, _threadPoolExecutor ->
                            Logger.t(TAG).w(
                                "[${taskType.type}]:$_threadPoolExecutor  " +
                                        "RejectedExecutionHandler"
                            )
                        }
                    ))
            }
            else-> {
                throw InvalidTaskTypeException("${taskType.type} has not been implement")
            }
        }
    }

    private fun addThreadPool(
        taskName: String,
        priority: Int = Process.THREAD_PRIORITY_DEFAULT,
        _threadPoolExecutor: ThreadPoolExecutor
    ): ThreadPoolExecutor {
        var tempThreadPoolExecutor = threadPoolMap[taskName]
        if (tempThreadPoolExecutor == null) {
            tempThreadPoolExecutor = _threadPoolExecutor.apply {
                threadFactory = XThreadFactory(taskName, priority)
                //允许核心线程闲置超时时被回收
                allowCoreThreadTimeOut(true)
            }
            Logger.t(TAG).d("addThreadPool for $taskName")
            threadPoolMap[taskName] = tempThreadPoolExecutor
        }
        return tempThreadPoolExecutor
    }

    private fun getThreadPool(tag: String):
            ThreadPoolExecutor {
        threadPoolMap[tag]?.let { return it } ?: kotlin.run {
            Logger.t(TAG).i("$tag threadPool does not exist ,yet.Create it.")

            if (threadPoolMap.size > 8) {
                throw MaxThreadPoolSizeException(
                    "Sorry,The ThreadPool attain max size.Advance to" +
                            " running with specific task,such as addDbTask,addFileTask,addNetTask,addCpuTask "
                )
            }

            when (tag) {
                TaskType.TYPE_DB.type -> {
                    Logger.t(TAG).w("threadPool does not contain $tag")
                    mDbIOExecutor = generateThreadPoolExecutor(TaskType.TYPE_DB)
                    return mDbIOExecutor as ThreadPoolExecutor
                }
                TaskType.TYPE_NETWORK.type -> {
                    Logger.t(TAG).w("threadPool does not contain $tag")
                    mNetworkIOExecutor = generateThreadPoolExecutor(TaskType.TYPE_NETWORK)
                    return mNetworkIOExecutor as ThreadPoolExecutor
                }
                TaskType.TYPE_FILE.type -> {
                    Logger.t(TAG).w("threadPool does not contain $tag")
                    mFileThreadPoolExecutor = generateThreadPoolExecutor(TaskType.TYPE_FILE)
                    return mFileThreadPoolExecutor as ThreadPoolExecutor
                }
                TaskType.TYPE_CPU.type -> {
                    Logger.t(TAG).w("threadPool does not contain $tag")
                    cpuThreadPoolExecutor = generateThreadPoolExecutor(TaskType.TYPE_CPU)
                    return cpuThreadPoolExecutor as ThreadPoolExecutor
                }
                else->  {
                    return addThreadPool(tag, Process.THREAD_PRIORITY_DEFAULT,
                        ThreadPoolExecutor(
                            CUSTOM_THREAD_POOL_SIZE,
                            CPU_COUNT + CUSTOM_THREAD_POOL_SIZE,
                            0L,
                            TimeUnit.MILLISECONDS,
                            ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY),
                            RejectedExecutionHandler { _, _threadPoolExecutor ->
                                Logger.t(TAG).w(
                                    "[$tag]:$_threadPoolExecutor  " +
                                            "RejectedExecutionHandler"
                                )
                            }
                        ))
                }
            }
        }
    }


    /**
     *  获取自带的自定义线程池,不关闭,不对外暴露TaskType
     *  @param tag 针对每个TAG 获取对应的线程池
     *  @param runnable 对应的 runnable 任务
     * */
    private fun addInternalTask(tag: TaskType, runnable: Runnable) {
        getThreadPool(tag.type).execute(runnable)
    }

    /**
     * 添加数据库的任务
     */
    fun addDbTask(runnable: Runnable) {
        addInternalTask(TaskType.TYPE_DB, runnable)
    }

    /**
     * 添加文件的任务
     */
    fun addFileTask(runnable: Runnable) {
        addInternalTask(TaskType.TYPE_FILE, runnable)
    }

    /**
     * 添加网络的任务
     */
    fun addNetTask(runnable: Runnable) {
        addInternalTask(TaskType.TYPE_NETWORK, runnable)
    }

    /**
     * 添加CPU计算型的任务,例如加解密,编解码,排序等
     */
    fun addCpuTask(runnable: Runnable) {
        addInternalTask(TaskType.TYPE_CPU, runnable)
    }

    /**
     *  获取自定义线程池,需要手动关闭
     *  @param tag 针对每个TAG 获取对应的线程池
     *  @param runnable 对应的 runnable 任务
     *  增加"_"的前缀,区分内部自定义的线程池,同时避免外部使用了同名的tag
     * */
    fun addCustomTask(tag: String, runnable: Runnable) {
        val customTag = "_$tag"
        getThreadPool(customTag).execute(runnable)
    }

    /**
     * 关闭自定义线程池
     * 增加"_"的前缀,区分内部自定义的线程池,同时避免外部使用了同名的tag
     */
    fun removeCustomTask(tag: String, runnable: Runnable) {
        val customTag = "_$tag"
    }

    /**
     *  获取自定义定时线程池,需要手动关闭
     *  @param tag 针对每个TAG 获取对应的线程池
     *  @param runnable 对应的 runnable 任务
     *  增加"_"的前缀,区分内部自定义的线程池,同时避免外部使用了同名的tag
     * */
    fun addCustomScheduleTask(tag: String, runnable: Runnable) {
        val customTag = "_$tag"
    }

    /**
     * 关闭自定义线程池
     * 增加"_"的前缀,区分内部自定义的线程池,同时避免外部使用了同名的tag
     */
    fun removeCustomScheduleTask(tag: String, runnable: Runnable) {
        val customTag = "_$tag"
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