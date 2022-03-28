package com.smartwasp.xtask

/**
 *
 * @ProjectName: Guardian
 * @Package: com.smartwasp.xtask
 * @ClassName: TaskType
 * @Description: 自带的自定义的任务类型
 * @Author: Tim.WJ
 * @CreateDate: 2022/3/28 19:10
 * @UpdateUser:
 * @UpdateDate: 2022/3/28 19:10
 * @UpdateRemark:
 * @Version: 1.0
 */
enum class TaskType(val type:String) {
    TYPE_DB("dbTask"),
    TYPE_NETWORK("netTask"),
    TYPE_FILE("fileTask"),
    TYPE_CPU("cpuTask")
}