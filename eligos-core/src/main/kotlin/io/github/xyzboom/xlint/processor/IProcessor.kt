package io.github.xyzboom.xlint.processor

import com.github.tnoalex.issues.Severity
import io.github.xyzboom.xlint.XLintContext

interface IProcessor {
    context(context: XLintContext)
    fun onBeforeProcess() {}
    context(context: XLintContext)
    fun onAfterProcess() {}
    /**
     * Because an issue processor can process several types of issues,
     * the severity here is the lowest one.
     */
    val severity: Severity
        get() = Severity.SUGGESTION
}