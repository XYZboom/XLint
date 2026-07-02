package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.CompareDataObjectWithReferenceIssue
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.Language
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import io.github.xyzboom.xlint.visitor.KtXLintTreeVisitorVoid
import io.github.xyzboom.xlint.visitor.analyze
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.slf4j.LoggerFactory

@Processor
class CompareDataObjectWithReferenceProcessor : IKotlinProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: KtFile) {
        file.accept(CompareExpressionVisitor(context))
    }

    private  class CompareExpressionVisitor(context: XLintContext) : KtXLintTreeVisitorVoid(context) {
        override fun visitBinaryExpression(expression: KtBinaryExpression) {
            val left = expression.left ?: return super.visitBinaryExpression(expression)
            val operator = expression.operationToken
            if (operator != KtTokens.EQEQEQ) {
                return super.visitBinaryExpression(expression)
            }
            val right = expression.right ?: return super.visitBinaryExpression(expression)

            analyze {
                val leftRef = getTargetIfIsDataObject(left)
                val rightRef = getTargetIfIsDataObject(right)
                if (leftRef != null && rightRef != null) {
                    context.reportIssue(
                        CompareDataObjectWithReferenceIssue(
                            expression.filePath,
                            expression.text,
                            leftRef.name.asString(),
                            rightRef.name.asString(),
                            expression.startLine
                        )
                    )
                }
            }
            super.visitBinaryExpression(expression)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CompareDataObjectWithReferenceProcessor::class.java)

        private fun KaSession.getTargetIfIsDataObject(expr: KtExpression): KaVariableSymbol? {
            val ref = expr.mainReference ?: return null
            val symbol = ref.resolveToSymbol()
            if (symbol !is KaVariableSymbol) return null
            val typeSymbol = symbol.returnType.symbol as? KaClassSymbol ?: return null
            if (typeSymbol.classKind.isObject) {
                return symbol
            }
            return null
        }
    }
}