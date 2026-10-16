package com.github.tnoalex

import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformExpressionUsageIssue
import com.github.tnoalex.processor.kotlin.withJava.UncertainNullablePlatformTypeProcessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JavaGetterPlatformUsageTest {
    @Test
    fun reportsJavaGetterResultsAtNonNullUseSites() {
        runWithCompilerEnv("resources@javaGetterPlatformUsage") { env ->
            runProcessorOnAllKtFiles(env, this, UncertainNullablePlatformTypeProcessor())
            val issues = collectIssues<UncertainNullablePlatformExpressionUsageIssue>(this)
            assertEquals(setOf(10, 15, 19, 22, 24), issues.map { it.startLine }.toSet())
            assertEquals(5, issues.size, "Report each unsafe use once, without flagging safe uses")
            assertTrue(issues.all { it.affectedFiles.single().endsWith("usage.kt") })
        }
    }
}
