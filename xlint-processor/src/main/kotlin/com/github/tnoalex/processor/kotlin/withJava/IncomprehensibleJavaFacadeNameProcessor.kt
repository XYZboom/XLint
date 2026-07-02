package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.IncomprehensibleJavaFacadeNameIssue
import com.github.tnoalex.processor.utils.filePath
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolVisibility
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

@Processor
class IncomprehensibleJavaFacadeNameProcessor : IKotlinProcessor {
    override val severity: Severity = Severity.SUGGESTION
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage.INSTANCE, KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: KtFile) {
        context.analyze {
            val javaFacadeName = file.javaFileFacadeFqName.shortName().asString()
            if (!javaFacadeName.endsWith("Kt")) return@analyze
            val namedFunctions = file.getChildrenOfType<KtNamedFunction>()
                .filter { it.symbol.visibility == KaSymbolVisibility.PUBLIC }
            val ktProperties = file.getChildrenOfType<KtProperty>()
                .filter { it.symbol.visibility == KaSymbolVisibility.PUBLIC }
            if (namedFunctions.isEmpty() && ktProperties.isEmpty()) return@analyze

            context.reportIssue(
                IncomprehensibleJavaFacadeNameIssue(
                    file.filePath,
                    javaFacadeName,
                    ktProperties.isNotEmpty(),
                    namedFunctions.isNotEmpty()
                )
            )
        }
    }
}