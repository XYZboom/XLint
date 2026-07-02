package com.github.tnoalex.processor.common

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.common.UnusedImportIssue
import com.github.tnoalex.processor.utils.refCanNotResolveWarn
import com.github.tnoalex.processor.utils.referenceExpressionSelfOrInChildren
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsFileImpl
import com.intellij.psi.util.PsiTreeUtil
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IJavaProcessor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.slf4j.LoggerFactory

@Processor
class UnUsedImportProcessor : IJavaProcessor, IKotlinProcessor {
    override val severity: Severity = Severity.CODE_SMELL

    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage.INSTANCE, KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: PsiJavaFile) {
        val importList = PsiTreeUtil.getChildOfType(file, PsiImportList::class.java) ?: return
        val importRefs = HashSet<PsiElement>()
        val importsMap = HashMap<PsiElement, String>()

        importList.importStatements.forEach {
            it.importReference?.resolve()?.let { r ->
                importRefs.add(r)
                importsMap[r] = it.text.removePrefix("import").trim()
            }
        }
        file.acceptChildren(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                if (PsiTreeUtil.getParentOfType(reference, PsiPackageStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                if (PsiTreeUtil.getParentOfType(reference, PsiImportStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                try {
                    reference.resolve()?.let {
                        resolveImports(it, importRefs)
                    }
                } catch (_: RuntimeException) {
                    logger.refCanNotResolveWarn(reference)
                }
                super.visitReferenceElement(reference)
            }
        })
        if (importRefs.isNotEmpty()) {
            context.reportIssue(
                UnusedImportIssue(
                    hashSetOf(file.virtualFile.path),
                    importRefs.map { importsMap[it]!! })
            )
        }
    }

    context(context: XLintContext)
    override fun process(file: KtFile) {
        val importList = PsiTreeUtil.getChildOfType(file, KtImportList::class.java) ?: return
        val importsRefs = HashSet<PsiElement>()
        val importsMap = HashMap<PsiElement, String>()
        importList.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                val parentText =
                    PsiTreeUtil.getParentOfType(expression, KtImportDirective::class.java)!!.text.removePrefix("import")
                        .trim()
                if (parentText.contains("*")) { // import ccc.xxx.*
                    val lastPackage = parentText.removeSuffix(".*").split(".").last()
                    if (expression.text != lastPackage) return
                    (expression.references.first().resolve() as? PsiPackage)?.let {
                        importsRefs.add(it)
                        importsMap[it] = parentText
                    }
                }
                if (parentText.endsWith(expression.text)) { // import ccc.xx.AA
                    expression.references.first().resolve()?.let {
                        importsRefs.add(it)
                        importsMap[it] = parentText
                    }
                }
            }
        })


        file.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression.isInImportDirective())
                    return super.visitReferenceExpression(expression)
                if (PsiTreeUtil.getParentOfType(expression, KtPackageDirective::class.java) != null)
                    return super.visitReferenceExpression(expression)
                expression.referenceExpressionSelfOrInChildren().forEach {
                    try {
                        it.references.forEach { ref ->
                            ref.resolve()?.let { r ->
                                resolveImports(r, importsRefs)
                            }
                        }
                    } catch (_: RuntimeException) {
                        logger.refCanNotResolveWarn(expression)
                    }
                }
                super.visitReferenceExpression(expression)
            }
        })
        if (importsRefs.isNotEmpty()) {
            context.reportIssue(
                UnusedImportIssue(
                    hashSetOf(file.virtualFilePath),
                    importsRefs.map { importsMap[it]!! })
            )
        }
    }

    companion object {
        fun resolveImports(element: PsiElement, importsRefs: HashSet<PsiElement>) {
            if (!importsRefs.contains(element)) { //import from cc.zz.*
                if (element is PsiCompiledElement) { // lib import
                    PsiTreeUtil.getParentOfType(element, ClsFileImpl::class.java)?.let {
                        importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageName }
                        return
                    }
                    PsiTreeUtil.getParentOfType(element, KtDecompiledFile::class.java)?.let {
                        importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageFqName.asString() }
                        return
                    }

                } else { // src import
                    PsiTreeUtil.getParentOfType(element, KtFile::class.java)?.let {
                        importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageFqName.asString() }
                        return
                    }
                    PsiTreeUtil.getParentOfType(element, PsiJavaFile::class.java)?.let {
                        importsRefs.removeIf { rf -> rf is PsiPackage && rf.qualifiedName == it.packageName }
                    }
                }
            } else importsRefs.remove(element) //import from cc.zz.AA
        }

        private val logger = LoggerFactory.getLogger(UnUsedImportProcessor::class.java)
    }
}
