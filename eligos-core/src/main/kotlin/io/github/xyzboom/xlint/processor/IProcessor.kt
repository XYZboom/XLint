package io.github.xyzboom.xlint.processor

interface IProcessor {
    fun onBeforeProcess() {}
    fun onAfterProcess() {}
}