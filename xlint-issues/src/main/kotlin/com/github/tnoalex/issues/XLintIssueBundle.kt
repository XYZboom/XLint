package com.github.tnoalex.issues

import com.github.tnoalex.foundation.bundle.AbstractBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val XLINT_ISSUES = "strings.issue.xlint-issue"

object XLintIssueBundle : AbstractBundle(XLINT_ISSUES) {
    fun message(
        @PropertyKey(resourceBundle = XLINT_ISSUES) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)

    fun lazy(
        @PropertyKey(resourceBundle = XLINT_ISSUES) key: String,
        vararg params: Any,
    ): () -> String = getLazyMessage(key, *params)
}