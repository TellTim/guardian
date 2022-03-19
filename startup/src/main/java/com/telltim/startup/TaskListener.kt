package com.telltim.startup

interface TaskListener {

    fun onWaitRunning(task:  AppBootUpTask?)
    fun onStart(task:  AppBootUpTask?)

    /**
     * @param task
     * @param dw Time to wait for execution
     * @param df Time consuming task execution
     */
    fun onFinish(task:  AppBootUpTask?, dw: Long, df: Long)

}