# Queryable agent guide

This file is the machine-oriented entry point for agents working on Queryable or integrating it into a Quarkus project.

## Supported baseline

- Queryable Maven plugin: `3.0.12`
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
    <version>3.0.12</version>
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

## Model subpackages (V3 and V4)

`<useModelSubPackage>true</useModelSubPackage>` in the plugin configuration (or
`-DuseModelSubPackage=true`) mirrors each entity's full relative directory below
`sourceModelDirectory` into the REST package below `sourceRestDirectory`. Default:
`false` (flat REST package). Class names remain `{EntityName}ServiceRs`.
Custom roots and multiple levels are supported. Root entities stay flat.
Switching the option relocates existing services and preserves manual methods,
`@QExclude` and V3 preserved-filter hooks; both locations existing is an error.
Update consuming-code imports and package-local dependencies when services move.
REST URLs, constants and entity packages are unchanged. Duplicate simple entity
names remain unsupported by discovery.

## Optional QEEX error catalog

QEEX generates REST exception catalogs, independently of entity filters. See the [website example](https://queryable.dev/docs/getting-started.html#qeex) and [QEEX reference](https://queryable.dev/docs/#qeex).

After `queryable:install`, install QEEX support:

```bash
./mvnw queryable:qeexinstall
```

**Destructive installation:** this goal deletes and replaces `{groupId}.api.service.RsResponseService`. Preserve customizations before running or rerunning it.

Define an interface using the generated `{groupId}.api.qeex.annotations.QeexExceptionBundle` and `QeexMessage` annotations. Give each method an explicit stable `id`, HTTP `code` and `message`; arguments use `String.format` placeholders. Generate its CDI implementation with:

```bash
./mvnw queryable:qeexsource
```

- Keep `src/main/resources/application.properties` present. Configure `qeex.project`, `qeex.default.id`, `qeex.default.code`, `qeex.default.message` and `qeex.default.language` explicitly.
- Interfaces are scanned under `src/main/java`; implementations are generated alongside them. Edit the interface, not the generated implementation. Rerun `qeexsource` after catalog changes; `queryable:source` does not generate QEEX implementations.
- Inject the catalog and throw its checked `QeexWebException` out of the endpoint to use the exception's HTTP status. QEEX-aware `RsResponseService` error helpers instead return HTTP 500.
- Do not promise automatic translations or per-message configuration overrides: generated methods do not call the corresponding `QeexConfig` helpers. Omitted method statuses currently generate 500; generated language falls back to `en`.
- The custom `language` header is not `Accept-Language`. Its application-scoped filter retains mutable state between requests and is unsafe for per-request language isolation.
- The exception mapper concatenates JSON without escaping message values. Do not treat arbitrary user-supplied message arguments as safely serialized.
- The catch-all mapper uses default configuration but leaves the error ID at 0; `qeex.default.id` is not assigned there.
- Authoritative code on the plugin's `main` branch: `src/main/java/it/ness/queryable/plugin/qeex/`, `src/main/java/it/ness/queryable/builder/QeexBuilder.java`, `src/main/resources/templates/qeex/` and `src/main/resources/templates/qeex-bundle/`.

## Annotation selection

Use annotations from `it.ness.queryable.annotations` and verify exact attributes in their Java definitions.

- Field filters: `@Q`, `@QLike`, `@QLikeList`, `@QList`, `@QLogicalDelete`, `@QNil`, `@QNotNil`.
- Class controls: `@QInclude`, `@QExclude`, `@QOrderBy`, `@QRs`.
- Generated test values: `@QT`.
- V3 manual filters: `@QPreserveFilters({"Entity.filter.name"})` protects existing
  `@FilterDef` / `@Filter` pairs by exact literal name. `@QPreservedFilter("Entity.filter.name")`
  binds a concrete non-static `void` service method taking `PanacheQuery<Entity>`;
  regenerated `getSearch` invokes it after automatic filters. Move inline custom
  blocks to these methods before regenerating; missing hooks warn and do not activate
  filters. Invalid definitions or bindings fail generation. V4 is unchanged.

Do not infer generated filter names or HTTP parameters from annotation names alone. Consult [https://queryable.dev/docs/#annotation-reference](https://queryable.dev/docs/#annotation-reference), [API.MD](https://github.com/n-essio/queryable/blob/main/API.MD), and the corresponding builder implementation.

## Change discipline

- Keep V3 and V4 behavior separate unless the task explicitly requires both.
- Preserve existing Maven goal names and public annotation APIs unless a breaking change is requested.
- When changing the plugin version, update `pom.xml`, `Constants.Q_VERSION`, version assertions, README examples, and website documentation together.
- Validate plugin descriptor generation with `mvn verify` after changing Mojo parameters.
