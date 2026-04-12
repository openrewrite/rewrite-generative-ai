<p align="center">
  <a href="https://docs.openrewrite.org">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-dark.svg">
      <source media="(prefers-color-scheme: light)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg">
      <img alt="OpenRewrite Logo" src="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg" width='600px'>
    </picture>
  </a>
</p>

<div align="center">
  <h1>rewrite-generative-ai</h1>
</div>

<div align="center">

<!-- Keep the gap above this line, otherwise they won't render correctly! -->
[![ci](https://github.com/openrewrite/rewrite-generative-ai/actions/workflows/ci.yml/badge.svg)](https://github.com/openrewrite/rewrite-generative-ai/actions/workflows/ci.yml)
[![Contributing Guide](https://img.shields.io/badge/Contributing-Guide-informational)](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md)
</div>

## What is this?

This repository contains experimental [OpenRewrite](https://docs.openrewrite.org/) recipes that explore integrating generative AI into automated code transformations. It serves as a proof of concept for combining large language models with OpenRewrite's deterministic refactoring framework.

> [!CAUTION]
> These recipes are **experimental** and **not recommended for production use**. Because LLM outputs are non-deterministic, recipe results vary between runs — including producing incorrect changes or modifying code that should be left alone. At scale, this leads to unpredictable diffs that are difficult to review. See [Limitations](#limitations) for details.

## Recipes

| Recipe | Description |
|--------|-------------|
| `org.openrewrite.ai.DefaultComesLast` | Restructures Java `switch` statements to place the `default` case last, using GPT-4o to generate the reordered code. |

## Quick start

### Prerequisites

- Java 21+
- An [OpenAI API key](https://platform.openai.com/api-keys)

### Clone and build

```bash
git clone https://github.com/openrewrite/rewrite-generative-ai.git
cd rewrite-generative-ai
./gradlew build
```

### Run the tests

The tests call the OpenAI API and are disabled in CI. To run them locally, create a file at `src/test/resources/token.txt` containing your OpenAI API key, then:

```bash
./gradlew test
```

### Using the recipe programmatically

The OpenAI token must be injected into the `ExecutionContext` before running the recipe. There is no recipe option or environment variable for it — it must be set in code:

```java
ExecutionContext ctx = new InMemoryExecutionContext();
GenerativeCodeExecutionContextView.view(ctx)
    .setOpenApiToken("<your-openai-api-key>");

// Then run the recipe as usual
new DefaultComesLast().run(sourceFiles, ctx);
```

> [!NOTE]
> This recipe is **not published** to Maven Central or the Moderne recipe catalog for general use. You need to build it from source or publish it to your local Maven repository with `./gradlew publishToMavenLocal`.

## Limitations

Because the recipe delegates code editing to an LLM, results are **non-deterministic**:

- Running the same recipe on the same code produces different outputs each time.
- The model may introduce subtle, incorrect changes — even when no change is needed.
- At scale across many repositories, this creates a large volume of unpredictable diffs that are impractical to review.

The test suite uses `@RepeatedTest(20)` to illustrate this: even a simple switch reordering task does not produce consistent results across 20 runs.

## Contributing

See the [Contributing Guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md).
