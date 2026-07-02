package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.ImplicitSingleExprFunctionIssue
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.Language
import com.intellij.psi.util.PsiTreeUtil
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import io.github.xyzboom.xlint.visitor.KtXLintTreeVisitorVoid
import io.github.xyzboom.xlint.visitor.analyze
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeReference
import org.slf4j.LoggerFactory


@Processor
class ImplicitSingleExprFunctionProcessor : IKotlinProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: KtFile) {
        file.accept(SingleExprFunctionVisitor(context))
    }

    private class SingleExprFunctionVisitor(context: XLintContext) : KtXLintTreeVisitorVoid(context) {
        override fun visitNamedFunction(function: KtNamedFunction) {
            if (PsiTreeUtil.getChildOfType(
                    function,
                    KtTypeReference::class.java
                ) != null
            ) return super.visitNamedFunction(function)
            if (PsiTreeUtil.getChildOfType(
                    function,
                    KtBlockExpression::class.java
                ) != null
            ) return super.visitNamedFunction(function)
            analyze {
                val returnType = function.symbol.returnType

                if (returnType.isUnitType)
                    return@analyze

                context.reportIssue(
                    ImplicitSingleExprFunctionIssue(
                        function.containingKtFile.virtualFilePath,
                        function.text,
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
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ImplicitSingleExprFunctionProcessor::class.java)
    }
}