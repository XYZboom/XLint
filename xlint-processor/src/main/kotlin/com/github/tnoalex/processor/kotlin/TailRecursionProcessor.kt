package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.OptimizedTailRecursionIssue
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.referenceExpressionSelfOrInChildren
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.Language
import com.intellij.psi.util.PsiTreeUtil
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.slf4j.LoggerFactory


@Processor
class TailRecursionProcessor : IKotlinProcessor {
    override val severity: Severity
        get() = Severity.SUGGESTION
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: KtFile) {
        if (context.confidenceLevel <= OptimizedTailRecursionIssue.normal) {
            file.accept(object : KtTreeVisitorVoid() {
                override fun visitNamedFunction(function: KtNamedFunction) {
                    if (function.hasModifier(KtTokens.TAILREC_KEYWORD)) return super.visitNamedFunction(function)
                    val isTailRecursion = findRecursion(function)
                    if (isTailRecursion) {
                        context.reportIssue(
                            OptimizedTailRecursionIssue(
                                file.virtualFilePath,
                                function.fqName?.asString() ?: let {
                                    logger.nameCanNotResolveWarn("function", function)
                                    "unknown func"
                                },
                                function.valueParameters.map {
                                    it.name ?: let {
                                        logger.nameCanNotResolveWarn("parameter", function)
                                        ""
                                    }
                                },
                                function.startLine
                            )
                        )
                    }
                    super.visitNamedFunction(function)
                }
            })
        }
    }

    private fun findRecursion(function: KtNamedFunction): Boolean {
        var isTailRecursion = false
        var foundReturnExpression = false
        function.acceptChildren(object : KtTreeVisitorVoid() {
            override fun visitReturnExpression(expression: KtReturnExpression) {
                if (isTailRecursion) return
                foundReturnExpression = true
                val callExpressions = ArrayList<KtCallExpression>()
                expression.acceptChildren(object : KtTreeVisitorVoid() {
                    override fun visitCallExpression(expression: KtCallExpression) {
                        callExpressions.add(expression)
                    }
                })
                if (callExpressions.isNotEmpty()) {
                    var containsOtherCall = false
                    callExpressions.forEach loop@{
                        it.referenceExpressionSelfOrInChildren().forEach { ref ->
                            ref.references.forEach innerLoop@{ r ->
                                val resolve = r.resolve() ?: let {
                                    containsOtherCall = true
                                    return@innerLoop
                                }
                                if (resolve == function) {
                                    PsiTreeUtil.getParentOfType(it, KtOperationExpression::class.java)?.let {
                                        return
                                    }
                                    isTailRecursion = true
                                    return
                                } else containsOtherCall = true
                            }
                        }
                    }
                    if (!containsOtherCall) isTailRecursion = true
                }
            }
        })
        return isTailRecursion && foundReturnExpression
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(TailRecursionProcessor::class.java)
    }
}