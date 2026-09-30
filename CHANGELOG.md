# Changelog

## 3.0.9

### Selective OIDC Identity Support

- `queryable:source` now detects the `io.quarkus:quarkus-oidc` dependency in the effective Maven project, including inherited dependencies and active profiles. Test-scope dependencies and `dependencyManagement` alone do not enable this feature.
- When OIDC is present, the existing V3 base API receives an injected `SecurityIdentity`, a `getCurrentUser()` implementation returning that identity, and `getCurrentUsername()` returning the principal name or `"system"` when the identity or principal is null.
- Updates add missing imports without duplication, preserve custom identity methods and methods annotated with `@QExclude`, and leave V4 APIs unchanged. Without OIDC, the existing API is not modified.

### Tests And Documentation

- Added a generator regression test and a Quarkus/H2 integration fixture for multi-property ordering such as `surname asc, name asc`.
- Verified real GET requests with default ordering, query-parameter overrides, mixed ASC/DESC directions, and pagination. This confirms existing sorting behavior rather than changing its implementation.
- Added integration coverage with and without OIDC, including CDI injection and username fallback, as part of `mvn verify` with Java 21.
- Updated the README, agent guide, and GitHub Pages documentation to version 3.0.9 and automated creation of missing GitHub releases before Maven Central deployment.

**Full Changelog**: https://github.com/n-essio/queryable/compare/3.0.8...3.0.9

## 3.0.8

### Model Exclusions And Filter Fixes

- Added `queryable:source -DexcludeClasses=Customer,Order` to skip selected model classes before parsing their metadata or regenerating their sources.
- Exclusions take precedence over `-Dclasses` and `@QInclude`; `@QExclude` continues to be respected.
- Corrected generated `@QList` Hibernate parameter types to match the model field type instead of always using String, and corrected primitive `int` query generation.
- Added regression tests for exclusions, skipped metadata parsing, and list filters.

### Documentation And Release Automation

- Added an `AGENTS.md` guide covering installation, generation commands, annotation selection, and repository validation conventions.
- Updated version references and usage examples, and added GitHub release creation to the publishing workflow.

**Full Changelog**: https://github.com/n-essio/queryable/compare/3.0.7...3.0.8

## 3.0.7

### Selective Source Generation

- Added `queryable:source -Dclasses=Customer,Order` to generate sources for selected model classes using comma-separated simple Java class names, including classes discovered in model subpackages.
- Whitespace and duplicate names are ignored; unknown class names are ignored. An explicit selection takes precedence over `@QInclude`, while `@QExclude` remains respected.
- Preserved the normal all-eligible-models behavior when no explicit selection is supplied.
- Added regression tests for class-name parsing and model selection, and documented the new command.

### Release Automation

- Updated plugin and installation references to 3.0.7 and enabled the Maven Central workflow on version-tag pushes.

**Full Changelog**: https://github.com/n-essio/queryable/compare/3.0.6...3.0.7