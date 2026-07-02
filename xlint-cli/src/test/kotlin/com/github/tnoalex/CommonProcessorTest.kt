package com.github.tnoalex

import com.github.tnoalex.issues.common.CircularReferencesIssue
import com.github.tnoalex.issues.common.ExcessiveParamsIssue
import com.github.tnoalex.issues.common.UnusedImportIssue
import com.github.tnoalex.processor.common.CircularReferencesProcessor
import com.github.tnoalex.processor.common.TooManyParametersProcessor
import com.github.tnoalex.processor.common.UnUsedImportProcessor
import com.intellij.psi.PsiJavaFile
import io.github.xyzboom.xlint.processor.IJavaProcessor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.*

@Execution(ExecutionMode.SAME_THREAD)
class CommonProcessorTest {

    @Test
    fun testCircularReferences() {
        runWithCompilerEnv("resources@circularRceferences") { env ->
            val processor = CircularReferencesProcessor()
            for (psiFile in env.allSourceFiles) {
                when (psiFile) {
                    is PsiJavaFile -> (processor as IJavaProcessor).process(psiFile)
                    is KtFile -> (processor as IKotlinProcessor).process(psiFile)
                }
            }
            processor.onAfterProcess()
            val issues = collectIssues<CircularReferencesIssue>(this)
            assertEquals(2, issues.size)
            var result = issues.firstOrNull {
                it.affectedFiles.all { f -> f.contains("pkg0") }
            }?.refMatrix?.second?.toTypedArray()
            result?.let { Arrays.sort(it, Comparator.comparing { list -> list.joinToString() }) }
            assertArrayEquals(
                arrayOf(arrayListOf(0, 1), arrayListOf(1, 0)),
                result
            )
            result = issues.firstOrNull {
                it.affectedFiles.all { f -> f.contains("pkg1") }
            }?.refMatrix?.second?.toTypedArray()
            result?.let { Arrays.sort(it, Comparator.comparing { list -> list.joinToString() }) }
            assertArrayEquals(
                arrayOf(arrayListOf(0, 0, 1), arrayListOf(0, 1, 0), arrayListOf(1, 0, 0)),
                result
            )
        }
    }

    @Test
    fun testTooManyParameters() {
        runWithCompilerEnv("resources@toomanyParams") { env ->
            val processor = TooManyParametersProcessor()
            for (psiFile in env.allSourceFiles) {
                when (psiFile) {
                    is PsiJavaFile -> (processor as IJavaProcessor).process(psiFile)
                    is KtFile -> (processor as IKotlinProcessor).process(psiFile)
                }
            }
            processor.onAfterProcess()
            val issues = collectIssues<ExcessiveParamsIssue>(this)
            assertEquals(3, issues.size)
            assertArrayEquals(
                arrayOf(8, 6),
                issues.firstOrNull {
                    it.functionSignature == "toomanyparams.pkg0.TooManyParams0.funP0(p0,p1,p2,p3,p4,p5,p6,p7)"
                }?.let { arrayOf(it.arity, it.startLine) }
            )
            assertArrayEquals(
                arrayOf(8, 4),
                issues.firstOrNull {
                    it.functionSignature == "toomanyparams.pkg1.funP1(p0,p1,p2,p3,p4,p5,p6,p7)"
                }?.let { arrayOf(it.arity, it.startLine) }
            )
        }
    }

    @Test
    fun testUnUsedImport() {
        runWithCompilerEnv("resources@unusedImport") { env ->
            val processor = UnUsedImportProcessor()
            for (psiFile in env.allSourceFiles) {
                when (psiFile) {
                    is PsiJavaFile -> (processor as IJavaProcessor).process(psiFile)
                    is KtFile -> (processor as IKotlinProcessor).process(psiFile)
                }
            }
            processor.onAfterProcess()
            val unusedImportIssues = collectIssues<UnusedImportIssue>(this)
            assertEquals(3, unusedImportIssues.size)
            assertArrayEquals(
                arrayOf("java.util.concurrent.*;"),
                unusedImportIssues.firstOrNull {
                    it.affectedFiles.all { f -> f.endsWith("UnUsedImportJava.java") }
                }?.unusedImports?.toTypedArray()
            )
            assertArrayEquals(
                arrayOf("unusedimport.pkg2.*", "java.util.jar.*"),
                unusedImportIssues.firstOrNull {
                    it.affectedFiles.all { f -> f.endsWith("UnusedImport0.kt") }
                }?.unusedImports?.toTypedArray()
            )
        }
    }
}