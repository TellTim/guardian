/**
 * Copyright (C), 2019-2022, Tim.WJ
 * FileName   : ThreadManager
 * Author     : Tim.WJ
 * Create At  : 2022/3/16 17:28
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
</desc></version></time></author> */
package cn.ycbjie.ycthreadpoollib

import cn.ycbjie.ycthreadpoollib.log.LogCallback

/**
 * @Description:  线程管理类
 */
class ThreadManager private constructor() {

    companion object {
        val instance: ThreadManager
            get() = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = ThreadManager()
    }

    private var executor: PoolThread? = null

    fun initThreadPool() {
        // 创建一个独立的实例进行使用
        executor = PoolThread.ThreadBuilder
            .createFixed(5)
            .setPriority(Thread.MAX_PRIORITY)
            .setCallback(LogCallback())
            .build()
    }

    /**
     * 获取线程池管理器对象，统一的管理器维护所有的线程池
     * @return executor对象
     */
    fun getExecutor(): PoolThread? {
        if (executor == null) {
            executor = PoolThread.ThreadBuilder
                .createFixed(5)
                .setPriority(Thread.MAX_PRIORITY)
                .setCallback(LogCallback())
                .build()
        }
        return executor
    }
    // 工作线程
}