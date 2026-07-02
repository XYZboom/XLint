package com.github.tnoalex

import com.github.tnoalex.foundation.bundle.AbstractBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val XLINT_BUNDLE = "xlint-meta"
object XLintCoreBundle:AbstractBundle(XLINT_BUNDLE) {
    fun message(
        @PropertyKey(resourceBundle = XLINT_BUNDLE) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)

    fun lazy(
        @PropertyKey(resourceBundle = XLINT_BUNDLE) key: String,
        vararg params: Any,
    ): () -> String = getLazyMessage(key, *params)
}