package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.*

class PlatformArgumentPassedToNonNullKotlinParamIssue(
    affectedFile: String,
    content: String,
    val startLine: Int,
    val kotlinFunctionName: String,
    val parameterIndex: Int
) : Issue(
    XLintIssueBundle.message("issue.name.PlatformArgumentPassedToNonNullKotlinParamIssue"),
    Severity.CODE_SMELL,
    normal,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    companion object {
        @JvmStatic
        val normal: ConfidenceLevel = ConfidenceLevel.MEDIUM
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as PlatformArgumentPassedToNonNullKotlinParamIssue
        return startLine == other.startLine && kotlinFunctionName == other.kotlinFunctionName && parameterIndex == other.parameterIndex
    }

    override fun hashCode(): Int = 31 * (31 * (31 * super.hashCode() + startLine) + kotlinFunctionName.hashCode()) + parameterIndex
}
