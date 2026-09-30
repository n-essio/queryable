# Queryable agent guide

This file is the machine-oriented entry point for agents working on Queryable or integrating it into a Quarkus project.

## Supported baseline

- Queryable Maven plugin: `3.0.9`
- Quarkus examples: `3.39.5`
- Java: `21`
- Maven coordinates: `it.n-ess.queryable:queryable-maven-plugin`

Use [https://queryable.dev/docs/](https://queryable.dev/docs/) for the human-readable installation guide and annotation reference. Use [API.MD](https://github.com/n-essio/queryable/blob/main/API.MD) for generated HTTP query parameter rules.

## Repository map

- Maven goals: `src/main/java/it/ness/queryable/plugin/`
- Shared goal parameters: `src/main/java/it/ness/queryable/model/api/Parameters.java`
- V3 source builder: `src/main/java/it/ness/queryable/builder/QueryableV3Builder.java`
- Model discovery and selection: `src/main/java/it/ness/queryable/util/ModelFilesV3.java`
- Annotations: `src/main/java/it/ness/queryable/annotations/`
- Generated source templates: `src/main/resources/templates/`
- Unit tests: `src/test/java/`
- Integration test project: `src/it/simple-it/`

## Build and validation

Run the full local verification with JDK 21:

```bash
mvn verify
```

For behavior changes, add or update focused JUnit tests under `src/test/java/`, then run `mvn verify`. Do not edit generated files under `target/`.

## Install in a Quarkus project

Add the plugin to the consuming project's `pom.xml`:

```xml
<plugin>
    <groupId>it.n-ess.queryable</groupId>
    <artifactId>queryable-maven-plugin</artifactId>
    <version>3.0.9</version>
</plugin>
```

Initialize the minimal API and sample entity once:

```bash
./mvnw queryable:install
```

The default model package is `{groupId}.{artifactId}.model`. The default REST package is `{groupId}.{artifactId}.service.rs`.

## Generate sources

Process every eligible model class:

```bash
./mvnw queryable:source
```

Process one or more selected model classes:

```bash
./mvnw queryable:source -Dclasses=Customer,Order
```

Exclude selected model classes:

```bash
./mvnw queryable:source -DexcludeClasses=Customer,Order
```

Rules for `classes` and `excludeClasses`:

- Pass comma-separated simple Java class names, without package names or `.java`.
- Whitespace and duplicate names are ignored.
- Unknown names are ignored.
- An explicit list takes precedence over `@QInclude`.
- `excludeClasses` takes precedence over `classes` and `@QInclude`.
- Explicitly excluded classes are not parsed or regenerated.
- `@QExclude` is always respected.
- Omitting both parameters preserves the normal all-eligible-classes behavior.

## Annotation selection

Use annotations from `it.ness.queryable.annotations` and verify exact attributes in their Java definitions.

- Field filters: `@Q`, `@QLike`, `@QLikeList`, `@QList`, `@QLogicalDelete`, `@QNil`, `@QNotNil`.
- Class controls: `@QInclude`, `@QExclude`, `@QOrderBy`, `@QRs`.
- Generated test values: `@QT`.

Do not infer generated filter names or HTTP parameters from annotation names alone. Consult [https://queryable.dev/docs/#annotation-reference](https://queryable.dev/docs/#annotation-reference), [API.MD](https://github.com/n-essio/queryable/blob/main/API.MD), and the corresponding builder implementation.

## Change discipline

- Keep V3 and V4 behavior separate unless the task explicitly requires both.
- Preserve existing Maven goal names and public annotation APIs unless a breaking change is requested.
- When changing the plugin version, update `pom.xml`, `Constants.Q_VERSION`, version assertions, README examples, and website documentation together.
- Validate plugin descriptor generation with `mvn verify` after changing Mojo parameters.
