package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.PlatformArgumentPassedToNonNullKotlinParamIssue
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IJavaProcessor
import io.github.xyzboom.xlint.visitor.JavaXLintRecursiveElementVisitor
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtParameter

@Processor
class PlatformArgumentToNonNullKotlinParameterProcessor : IJavaProcessor {
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage.INSTANCE, KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: PsiJavaFile) {
        if (context.confidenceLevel > PlatformArgumentPassedToNonNullKotlinParamIssue.normal) return
        file.accept(Visitor(context))
    }

    private class Visitor(context: XLintContext) : JavaXLintRecursiveElementVisitor(context) {
        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            val target = expression.methodExpression.resolve() as? KtLightElement<*, *>
            val kotlinFunction = target?.kotlinOrigin as? org.jetbrains.kotlin.psi.KtNamedFunction ?: return
            val args = expression.argumentList.expressions
            val params = kotlinFunction.valueParameters
            for (index in args.indices) {
                val paramIndex = if (index < params.size) index else params.lastIndex
                if (paramIndex < 0) continue
                val kotlinParam = params[paramIndex]
                val typeText = kotlinParam.typeReference?.text ?: continue
                if (typeText.endsWith("?")) continue
                val argument = args[index]
                if (argument is PsiLiteralExpression || argument is PsiNewExpression || argument is PsiArrayInitializerExpression) continue
                val type = argument.type ?: continue
                if (type is PsiPrimitiveType || type is PsiArrayType) continue
                val resolvedValue = when (argument) {
                    is PsiReferenceExpression -> argument.resolve() as? PsiModifierListOwner
                    is PsiMethodCallExpression -> argument.methodExpression.resolve() as? PsiModifierListOwner
                    else -> null
                }
                if (resolvedValue?.modifierList?.annotations?.any { annotation ->
                        val name = annotation.qualifiedName.orEmpty()
                        name.contains("NotNull", true) || name.contains("NonNull", true) ||
                            name.contains("Nullable", true) || name.contains("CheckForNull", true)
                    } == true) continue
                context.reportIssue(
                    PlatformArgumentPassedToNonNullKotlinParamIssue(
                        expression.filePath,
                        argument.text,
                        argument.startLine,
                        kotlinFunction.name ?: continue,
                        paramIndex
                    )
                )
            }
            super.visitMethodCallExpression(expression)
        }
    }
}
