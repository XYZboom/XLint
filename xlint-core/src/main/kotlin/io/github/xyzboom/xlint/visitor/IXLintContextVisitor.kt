package io.github.xyzboom.xlint.visitor

import io.github.xyzboom.xlint.XLintContext
import org.jetbrains.kotlin.analysis.api.KaSession

interface IXLintContextVisitor {
    val context: XLintContext
}

inline fun <R> IXLintContextVisitor.analyze(action: KaSession.() -> R): R {
    return context.analyze(action)
}