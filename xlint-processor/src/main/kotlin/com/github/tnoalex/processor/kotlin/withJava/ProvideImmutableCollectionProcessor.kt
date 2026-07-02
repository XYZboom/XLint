package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.ProvideImmutableCollectionIssue
import com.github.tnoalex.processor.utils.*
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.util.PsiTreeUtil
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IJavaProcessor
import io.github.xyzboom.xlint.visitor.JavaXLintRecursiveElementVisitor
import io.github.xyzboom.xlint.visitor.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.slf4j.LoggerFactory

@Processor
class ProvideImmutableCollectionProcessor : IJavaProcessor {
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage.INSTANCE, KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: PsiJavaFile) {
        if (context.confidenceLevel > ProvideImmutableCollectionIssue.normal) {
            return
        }
        file.accept(JavaFileVisitorVoid(context))
    }

    private class JavaFileVisitorVoid(context: XLintContext) : JavaXLintRecursiveElementVisitor(context) {
        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            val targetFunc = try {
                expression.methodExpression.resolve() ?: let {
                    logger.refCanNotResolveWarn(expression)
                    return super.visitMethodCallExpression(expression)
                }
            } catch (_: RuntimeException) {
                logger.refCanNotResolveWarn(expression)
            }
            if (targetFunc !is KtLightElement<*, *>) return super.visitMethodCallExpression(expression)
            val ktOrigin = targetFunc.kotlinOrigin ?: let {// maybe kotlin enum
                logger.kotlinOriginCanNotResolveWarn("expression", expression)
                return super.visitMethodCallExpression(expression)
            }
            analyze {
                if (ktOrigin !is KtDeclaration) return@analyze
                val returnType = when (val symbol = ktOrigin.symbol) {
                    is KaCallableSymbol -> symbol.returnType
                    else -> null
                }

                if (returnType == null) {
                    logger.nameCanNotResolveWarn("return type", expression)
                    return@analyze
                }
                if (returnType.symbol?.classId !in KOTLIN_IMMUTABLE_CLASS_IDS)
                    return@analyze
                val className = PsiTreeUtil.getParentOfType(expression, PsiClass::class.java)?.qualifiedName
                    ?: "AnonymousInnerClass"
                context.reportIssue(
                    ProvideImmutableCollectionIssue(
                        hashSetOf(expression.filePath, ktOrigin.filePath),
                        (ktOrigin as KtCallableDeclaration).fqName?.asString() ?: let {
                            logger.nameCanNotResolveWarn("function", ktOrigin)
                            "unknown func name"
                        },
                        ktOrigin is KtNamedFunction,
                        ktOrigin is KtParameter,
                        expression.startLine,
                        expression.text,
                        className
                    )
                )
            }
            super.visitMethodCallExpression(expression)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ProvideImmutableCollectionProcessor::class.java)

        private val KOTLIN_IMMUTABLE_CLASS_IDS =
            listOf("kotlin/collections/List", "kotlin/collections/Set", "kotlin/collections/Map")
                .map { ClassId.fromString(it) }
    }
}