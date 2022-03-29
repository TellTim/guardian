package com.telltim.xtask

enum class TaskType (val type:String) {
    TYPE_DB("dbTask"),
    TYPE_NETWORK("netTask"),
    TYPE_FILE("fileTask"),
    TYPE_CPU("cpuTask")
}