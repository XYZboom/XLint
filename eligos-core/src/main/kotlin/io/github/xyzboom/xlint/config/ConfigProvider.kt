package io.github.xyzboom.xlint.config

/**
 * Provider for processor-level configuration.
 *
 * Processors receive this via [io.github.xyzboom.xlint.processor.IProcessor.configure]
 * and use typed getters to read the values they need, falling back to their own defaults
 * when a key is not present.
 *
 * The backing data comes from `config.yaml` on the classpath, optionally merged
 * with a user-provided rules file at CLI time.
 */
interface ConfigProvider {
    fun getInt(key: String, default: Int): Int
    fun getString(key: String, default: String): String
}
