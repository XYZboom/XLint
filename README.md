# <conter class="img"><img src="./doc/imgs/icon.svg"/></center> XLint

![GitHub License](https://img.shields.io/github/license/TnoAlex/XLint)

XLint is the next generation of [Eligos](https://github.com/TnoAlex/Eligos), re-architected with a new foundation for better extensibility and performance.

> **Note:** XLint is currently under active development. Not all features from Eligos have been migrated yet.

## Why XLint?

XLint inherits all code smell detection capabilities from Eligos and rebuilds them on a cleaner, modular architecture:

- **New processor pipeline** — rules are now declared via KSP annotations instead of manual registration
- **Simplified rule API** — implement `IProcessor` and add a single annotation
- **Better separation** — core analysis, rule definitions, and reporting are fully decoupled

## Migration Status

| Feature | Eligos | XLint |
|---------|--------|-------|
| CLI | ✅ | ✅ |
| Rule extension via annotations | ❌ (manual) | ✅ (KSP) |
| IntelliJ IDEA Plugin | ✅ | 🚧 (coming soon) |
| New rule API | ❌ | ✅ |
| All original code smells | ✅ | ✅ (see below) |

## Supported Code Smells

XLint currently detects the same set of code smells as Eligos:

<table>
    <tr align="center">
        <td><b>Type</b></td>
        <td><b>Label</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td>Circular References</td>
        <td>🟨</td>
        <td>Two or more classes or files have interdependencies that form a closed loop</td>
    </tr>
    <tr>
        <td>Excessive Parameters</td>
        <td>🟨</td>
        <td>A method has too many arguments</td>
    </tr>
    <tr>
        <td>Unused Import</td>
        <td>🟨</td>
        <td>Classes, attributes, methods, or packages that have been imported into a file, but have never been used</td>
    </tr>
    <tr>
        <td>Complex Method</td>
        <td>🟨</td>
        <td>The complexity of the loop is too large</td>
    </tr>
    <tr>
        <td>Provide Immutable Collection</td>
        <td>🟩</td>
        <td>Kotlin provides immutable collection types when Java calls Kotlin's API</td>
    </tr>
    <tr >
        <td>Internal Exposed</td>
        <td>🟩</td>
        <td>Java exposes Kotlin internal declarations as public</td>
    </tr>
    <tr>
        <td>Uncertain Nullable Platform Expression Usage</td>
        <td>🟩</td>
        <td>Kotlin locally calls a Java method that returns a null-agnostic type, and uses this result directly in a Kotlin method that expects completely non-null arguments</td>
    </tr>
    <tr>
        <td>Uncertain Nullable Platform Caller</td>
        <td>🟩</td>
        <td>Call a method or access a property whose caller expression type is a platform type in Kotlin</td>
    </tr>
    <tr>
        <td>Nullable Passed To Platform Parameter</td>
        <td>🟩</td>
        <td>Kotlin passed a nullable parameter to a Java method which takes a platform parameter</td>
    </tr>
   <tr>
       <td>Uncertain Nullable Platform Type In Property</td>
       <td>🟩</td>
       <td>Kotlin calls a null-agnostic Java method or property and uses this value as the return value of the getter for a class property</td>
    </tr>
    <tr>
        <td>Non JVMStatic Companion Function</td>
        <td>🟩</td>
        <td>Public functions in a companion object must be annotated with @JvmStatic to be exposed as a static method. Without the annotation, these functions are only available as instance methods on a static Companion field</td>
    </tr>
    <tr>
        <td>Non JVMField Companion Value</td>
        <td>🟩</td>
        <td>Public, non-const properties which are effective constants in a companion object must be annotated with @JvmField to be exposed as a static field</td>
    </tr>
    <tr>
        <td>Incomprehensible JavaFacade Name</td>
        <td>🟩</td>
        <td>When a file contains top-level functions or properties, always annotate it with @file:JvmName("Foo") to provide a nice name. By default, top-level members in a file MyClass.kt will end up in MyClassKt which is unappealing and leaks the language as an implementation detail</td>
    </tr>
    <tr>
        <td>Ignored Exception</td>
        <td>🟩</td>
        <td>Functions which can throw checked exceptions should document them with @Throws. Runtime exceptions should be documented in KDoc. Be mindful of the APIs a function delegates to as they may throw checked exceptions which Kotlin otherwise silently allows to propagate</td>
    </tr>
    <tr >
        <td>When Instead Of Cascade If</td>
        <td>🟪</td>
        <td>If statements with too many branches should be replaced with when statements</td>
    </tr>
    <tr >
        <td>Implicit Single Expression Function</td>
          <td>🟪</td>
        <td>Kotlin's one-expression method returns a value of type other than Unit, but does not specify the return type</td>
    </tr>
    <tr >
        <td>Object Extends Throwable</td>
          <td>🟪</td>
        <td>A class decorated with Kotlin `object` inherits from Throwable</td>
    </tr>
    <tr >
        <td>Optimized Tail Recursion</td>
          <td>🟪</td>
        <td>A tail-recursive function in Kotlin does not indicate that it is tail-recursive via `tailrec`</td>
    </tr>
</table>

| Legend | Meaning |
|--------|---------|
| 🟨 | Common to both Java and Kotlin |
| 🟩 | Arises from Kotlin-Java interop |
| 🟪 | Kotlin-specific |

## Quick Start

```bash
git clone https://github.com/TnoAlex/XLint.git
cd XLint
./gradlew build
```

Find the executable CLI jar at `xlint-cli/build/libs/`.

### Docker

```bash
docker build -t xlint:1.0 .
docker run -v /path/to/project:/dist/project \
           -v /path/to/output:/dist/result \
           xlint:1.0 kotlin ./project ./result --with java
```

Prerequisites:

| Gradle | Kotlin | JDK |
|--------|--------|-----|
| 8.0+   | 2.4.x  | 17+ |

## CLI Usage

```
xlint --help
```

Key options:

| Option | Description |
|--------|-------------|
| `-w, --with` | Secondary languages (e.g., `java`) |
| `-ecp, --class-path` | Classpath for external dependencies |
| `-f, --format` | Output format: JSON, XML, HTML, TEXT |
| `-r, --rules` | Path to custom rule configuration |
| `-sl, --severity-level` | Minimum severity to report |
| `-cl, --confidence-level` | Minimum confidence to report |
| `--enable-xlint` | Use the new XLint pipeline (experimental) |

## Extending XLint

XLint offers a simpler rule development experience through KSP-based annotation processing:

```kotlin
@Processor
class MyProcessor : IProcessor {
    override fun process(file: VirtualFile, context: ProcessorContext) {
        // your analysis logic
        context.reportIssue(MyIssue(affectedFiles))
    }
}
```

No manual service registration needed — the `@Processor` annotation is picked up automatically.

For the classic API (compatible with Eligos), see [Execution Process](./doc/execution_process.md).

---

*XLint is the successor of [Eligos](https://github.com/TnoAlex/Eligos), built from the ground up for the next generation of Kotlin static analysis.*