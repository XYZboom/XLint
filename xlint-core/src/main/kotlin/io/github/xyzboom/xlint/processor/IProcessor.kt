package io.github.xyzboom.xlint.processor

import com.github.tnoalex.issues.Severity
import com.intellij.lang.Language
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.config.ConfigProvider

interface IProcessor {
    context(context: XLintContext)
    fun onBeforeProcess() {
    }

    context(context: XLintContext)
    fun onAfterProcess() {
    }

    /**
     * Configure this processor with the project-level [ConfigProvider].
     * Called once before any [process] call.
     * Default no-op — override if the processor needs config values.
     *
     * Each processor is responsible for reading the keys it cares about
     * and falling back to a sensible default when a key is absent.
     */
    fun configure(config: ConfigProvider) {
    }

    /**
     * Because an issue processor can process several types of issues,
     * the severity here is the lowest one.
     */
    val severity: Severity
        get() = Severity.SUGGESTION
    val supportLanguage: List<Language>
        get() = listOf(Language.ANY)
}