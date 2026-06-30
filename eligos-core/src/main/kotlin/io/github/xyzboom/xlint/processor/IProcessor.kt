package io.github.xyzboom.xlint.processor

import com.github.tnoalex.issues.Severity

interface IProcessor {
    fun onBeforeProcess() {}
    fun onAfterProcess() {}
    /**
     * Because an issue processor can process several types of issues,
     * the severity here is the lowest one.
     */
    val severity: Severity
        get() = Severity.SUGGESTION
}