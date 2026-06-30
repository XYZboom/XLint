package io.github.xyzboom.xlint

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.ksp.writeTo
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.annotations.ProcessorProvider
import io.github.xyzboom.xlint.processor.IProcessor

class XLintSymbolProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val processorClasses = resolver.getSymbolsWithAnnotation(Processor::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
        val providerClass = resolver.getSymbolsWithAnnotation(ProcessorProvider::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull()
        if (providerClass == null) {
            return emptyList()
        }
        val processorMap = HashMap<String, KSClassDeclaration>()
        for (processorClass in processorClasses) {
            val processorAnno = processorClass.getAnnotationsByType(Processor::class).first()
            val name = processorAnno.name.ifEmpty {
                processorClass.simpleName.getShortName()
            }
            processorMap[name] = processorClass
        }
        // Generate the code for the companion object
        val providerName = providerClass.simpleName.asString()
        val mapType = Map::class.asClassName()
            .parameterizedBy(
                String::class.asClassName(),
                LambdaTypeName.get(
                    parameters = emptyList(),
                    returnType = IProcessor::class.asClassName()
                )
            )
        val processorEntries = processorMap.entries
        val propertyContentBuilder = StringBuilder().apply {
            append("lazy {\n")
            append("buildMap {\n")
            repeat(processorEntries.size) {
                append("put(%S, ::%T)\n")
            }
            append("}\n}\n")
        }
        val propertySpec = PropertySpec.builder("xLintProcessorMap", mapType)
            .delegate(
                propertyContentBuilder.toString(),
                *processorEntries.flatMap { listOf(it.key, it.value.asType(emptyList()).toTypeName()) }.toTypedArray()
            )
            .build()
        val fileName = "${providerName}XLintKSPGenerated"
        val fileSpec = FileSpec.builder(providerClass.packageName.asString(), fileName)
            .addProperty(propertySpec)
            .build()
        fileSpec.writeTo(codeGenerator, false)
        return emptyList()
    }

}