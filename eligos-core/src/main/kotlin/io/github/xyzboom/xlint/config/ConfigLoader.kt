package io.github.xyzboom.xlint.config

import org.yaml.snakeyaml.Yaml
import java.io.File

fun loadConfig(extendRulePath: File? = null): ConfigProvider {
    val yaml = Yaml()
    val defaultStream = ConfigProvider::class.java.classLoader.getResourceAsStream("config.yaml")
        ?: throw IllegalStateException("config.yaml not found on classpath")
    @Suppress("UNCHECKED_CAST")
    val map: MutableMap<String, Any?> = yaml.load(defaultStream)
    if (extendRulePath != null) {
        @Suppress("UNCHECKED_CAST")
        val userMap: Map<String, Any?> = yaml.load(extendRulePath.inputStream())
        mergeMaps(map, userMap)
    }
    return YamlConfigProvider(map)
}

@Suppress("UNCHECKED_CAST")
private fun mergeMaps(base: MutableMap<String, Any?>, overlay: Map<String, Any?>) {
    for (key in overlay.keys) {
        if (base.containsKey(key) && base[key] is Map<*, *> && overlay[key] is Map<*, *>) {
            mergeMaps(base[key] as MutableMap<String, Any?>, overlay[key] as Map<String, Any?>)
        } else {
            base[key] = overlay[key]
        }
    }
}

private class YamlConfigProvider(private val root: Map<String, Any?>) : ConfigProvider {
    override fun getInt(key: String, default: Int): Int {
        return (walkMap(key) as? Number)?.toInt() ?: default
    }

    override fun getString(key: String, default: String): String {
        return (walkMap(key) as? String) ?: default
    }

    @Suppress("UNCHECKED_CAST")
    private fun walkMap(key: String): Any? {
        var current: Any? = root
        for (part in key.split(".")) {
            if (current !is Map<*, *>) return null
            current = current[part]
        }
        return current
    }
}
