package com.github.tnoalex.processor

import com.github.tnoalex.foundation.bundle.AbstractBundle
import org.jetbrains.annotations.PropertyKey

private const val XLINT_PROCESSOR = "strings.processor.xlint-processor"

object XLintProcessorBundle : AbstractBundle(XLINT_PROCESSOR) {
    fun message(
        @PropertyKey(resourceBundle = XLINT_PROCESSOR) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)

    fun lazy(
        @PropertyKey(resourceBundle = XLINT_PROCESSOR) key: String,
        vararg params: Any,
    ): () -> String = getLazyMessage(key, *params)
}