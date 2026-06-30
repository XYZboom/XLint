package io.github.xyzboom.xlint.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Processor(
    val name: String = ""
)
