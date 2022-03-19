package com.telltim.startup

interface OnProjectListener {
    fun onProjectStart()

    fun onProjectFinish()

    fun onStageFinish()
}