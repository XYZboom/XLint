package com.github.tnoalex.processor.java

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.java.MissingNullabilityAnnotationIssue
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IJavaProcessor

@Processor
class MissingNullabilityAnnotationProcessor: IJavaProcessor {
    override val severity: Severity = Severity.SUGGESTION

    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage.INSTANCE)

    companion object {
        val annos = listOf("NonNull", "Nullable")
    }

    context(context: XLintContext)
    override fun process(file: PsiJavaFile) {
        file.accept(object: JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                if (method.annotations.all {
                        it.qualifiedName?.split(".")?.last() !in annos
                    }) {
                    context.reportIssue(
                        MissingNullabilityAnnotationIssue(
                            file.filePath,
                            method.containingClass?.qualifiedName,
                            method.name,
                            method.startLine
                        )
                    )
                }
                super.visitMethod(method)
            }
        })
    }
}