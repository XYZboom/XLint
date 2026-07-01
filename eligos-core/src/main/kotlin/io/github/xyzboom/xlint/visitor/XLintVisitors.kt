package io.github.xyzboom.xlint.visitor

import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.JavaRecursiveElementVisitor
import io.github.xyzboom.xlint.XLintContext
import org.jetbrains.kotlin.psi.KtTreeVisitor
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtVisitor

open class KtXLintTreeVisitor<D>(
    override val context: XLintContext
) : KtTreeVisitor<D>(), IXLintContextVisitor

open class KtXLintTreeVisitorVoid(
    override val context: XLintContext
) : KtTreeVisitorVoid(), IXLintContextVisitor

open class KtXLintVisitor<R, D>(
    override val context: XLintContext
) : KtVisitor<R, D>(), IXLintContextVisitor

open class JavaXLintRecursiveElementVisitor(
    override val context: XLintContext
): JavaRecursiveElementVisitor(), IXLintContextVisitor

open class JavaXLintElementVisitor(
    override val context: XLintContext
): JavaElementVisitor(), IXLintContextVisitor