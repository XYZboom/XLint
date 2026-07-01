package io.github.xyzboom.xlint

import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.statistics.Statistics
import org.jetbrains.kotlin.analysis.api.KaSession

/**
 * Compatibility interface for migrating to XLint
 */
interface IContext {
    var session: KaSession
    val issues: Set<Issue>
    val stats: List<Statistics>

    /**
     * todo: confidenceLevel should be val after migration to XLint
     */
    var confidenceLevel: ConfidenceLevel
    fun reportIssue(issue: Issue)
    fun reportIssues(issue: List<Issue>)
    fun resetContext()
    fun reportStatistics(statistics: Statistics)
}