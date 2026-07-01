package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.ComplexKotlinFunctionIssue
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.Language
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.config.ConfigProvider
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.slf4j.LoggerFactory

@Processor
class KotlinMccabeComplexityProcessor : IKotlinProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage.INSTANCE)

    private var maxCyclomaticComplexity = 10
    private var currentComplexity = 1

    override fun configure(config: ConfigProvider) {
        this.maxCyclomaticComplexity = config.getInt("function.maxCyclomaticComplexity", 10)
    }

    context(context: XLintContext)
    override fun process(file: KtFile) {
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                function.accept(ktCComplexityVisitor)
                if (currentComplexity >= maxCyclomaticComplexity) {
                    context.reportIssue(
                        ComplexKotlinFunctionIssue(
                            function.containingKtFile.virtualFilePath,
                            function.fqName?.asString() ?: let {
                                logger.nameCanNotResolveWarn("function",function)
                                "unknown func"
                            },
                            function.valueParameters.map {
                                it.name ?: let {
                                    logger.nameCanNotResolveWarn("parameter",function)
                                    ""
                                }
                            },
                            function.startLine,
                            currentComplexity
                        )
                    )
                }
                currentComplexity = 1
            }
        })
    }

    private fun computeConditionComplexity(condition: KtExpression?) {
        condition?.acceptChildren(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression is KtOperationReferenceExpression) {
                    if (expression.operationSignTokenType == KtTokens.ANDAND
                        || expression.operationSignTokenType == KtTokens.OROR
                    ) {
                        currentComplexity++
                    }
                }
            }
        })
    }

    private val ktCComplexityVisitor = object : KtTreeVisitorVoid() {
        override fun visitForExpression(expression: KtForExpression) {
            currentComplexity++
            super.visitForExpression(expression)
        }

        override fun visitWhileExpression(expression: KtWhileExpression) {
            currentComplexity++
            computeConditionComplexity(expression.condition)
            super.visitWhileExpression(expression)
        }

        override fun visitIfExpression(expression: KtIfExpression) {
            currentComplexity++
            computeConditionComplexity(expression.condition)
            super.visitIfExpression(expression)
        }


        override fun visitWhenExpression(expression: KtWhenExpression) {
            currentComplexity += expression.entries.size
            super.visitWhenExpression(expression)
        }

        override fun visitTryExpression(expression: KtTryExpression) {
            currentComplexity++
            super.visitTryExpression(expression)
        }

        override fun visitDoWhileExpression(expression: KtDoWhileExpression) {
            currentComplexity++
            computeConditionComplexity(expression.condition)
            super.visitDoWhileExpression(expression)
        }

    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(KotlinMccabeComplexityProcessor::class.java)
    }
}