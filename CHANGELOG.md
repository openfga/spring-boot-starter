# Changelog

## [0.4.0](https://github.com/openfga/spring-boot-starter/compare/v0.3.3...v0.4.0) (2026-08-12)

> [!NOTE]
> This release establishes the Spring Boot 3.4 baseline for the upcoming Jackson 3 / Spring Boot 4 work. Spring Boot 4 is **not yet supported** — the PropertyMapper fix below only removes one binary incompatibility. Progress is tracked in [#184](https://github.com/openfga/spring-boot-starter/issues/184).

### Added

* support contextual tuples and context in checks ([#169](https://github.com/openfga/spring-boot-starter/issues/169)) ([bb47703](https://github.com/openfga/spring-boot-starter/commit/bb477039ac6fc141dd4fe26842a41fb436c5840a))
* support initial model and tuple loading ([#171](https://github.com/openfga/spring-boot-starter/issues/171)) ([30ebb9d](https://github.com/openfga/spring-boot-starter/commit/30ebb9dbf4304ab33dd3fb2fb14146e288ef54d8))


### Fixed

* bump openfga-sdk to 0.9.11 and use transactions() API ([0821c03](https://github.com/openfga/spring-boot-starter/commit/0821c035d39f45bd32dd98651455538524e544ec)), closes [#186](https://github.com/openfga/spring-boot-starter/issues/186)
* **example:** make sure to use Gradle wrapper 9.6.1 for example ([96a1ca4](https://github.com/openfga/spring-boot-starter/commit/96a1ca413cf4aadb70ab799c04211c1a7895e820)), closes [#183](https://github.com/openfga/spring-boot-starter/issues/183)
* **example:** use Java plugin instead of 'java-library' ([659f973](https://github.com/openfga/spring-boot-starter/commit/659f97323b2b6cb320762d901aa35f1961835245)), closes [#183](https://github.com/openfga/spring-boot-starter/issues/183)
* **jackson:** fix deprecation for spring 4.1 baseline with ObjectMapper ([1e67e02](https://github.com/openfga/spring-boot-starter/commit/1e67e0230524947447b4b0dfeff6a903448d1456))
* **spring-boot:** do not rely on internal property mapper API ([c35bf9d](https://github.com/openfga/spring-boot-starter/commit/c35bf9d50a5a321ece5086225d6f0752ccac6e7d))
* **spring:** make PropertyMapper usage Spring Boot 4 compatible ([4547db3](https://github.com/openfga/spring-boot-starter/commit/4547db377947b19cbb88139060c6c79ece74d5f9)), closes [#183](https://github.com/openfga/spring-boot-starter/issues/183)


### Documentation

* document openfga.initialization properties in README ([e3403b9](https://github.com/openfga/spring-boot-starter/commit/e3403b942886f90db4f2112778af679539e0831d))

## [0.3.3](https://github.com/openfga/spring-boot-starter/compare/v0.3.1...v0.3.3) (2026-07-22)

### Added

* add OpenFGA Testcontainers service connection ([#170](https://github.com/openfga/spring-boot-starter/issues/170)) ([42d93e2](https://github.com/openfga/spring-boot-starter/commit/42d93e23636dbcf34cf56dbd65f1a9a45e317b6e))

### Fixed

* support Spring Boot 4 by dropping removed PropertyMapper APIs ([#164](https://github.com/openfga/spring-boot-starter/issues/164)) ([b893624](https://github.com/openfga/spring-boot-starter/commit/b8936249729865385d9c90e9fff864f89a47c018))

### Documentation

* clarify activation in autoconfig javadoc, pin testcontainers image in README ([54de65c](https://github.com/openfga/spring-boot-starter/commit/54de65cafac30ccf5c1ec0af09e154495dc5f506))

> [!NOTE]  
> The starter artifact version is bumped from v0.3.1 to v0.3.3 due to issues in the release pipeline.

## v0.3.1

### [0.3.1](https://github.com/openfga/spring-boot-starter/releases/tag/v0.3.1) (2026-03-31)

- chore: minor ci changes & dependency updates

## v0.3.0

### [0.3.0](https://github.com/openfga/spring-boot-starter/releases/tag/v0.3.0) (2025-12-15)

- feat: improved error handling
- chore: update dependencies

## v0.2.0

### [0.2.0](https://github.com/openfga/spring-boot-starter/releases/tag/v0.2.0) (2025-03-18)

- feat: improved error handling (#90) - thanks to @holgerstolzenberg for this!
- feat(deps)!: update java SDK version (#94)

> [!WARNING]  
> This version includes version 0.8.1 of the [OpenFGA Java SDK](https://github.com/openfga/java-sdk/), which contains breaking changes to the OpenFGA Java SDK related to the SDK's batch check implementation. Please see #94 for additional information.

## v0.1.0

### [0.1.0](https://github.com/openfga/spring-boot-starter/releases/tag/v0.1.0) (2025-01-03)

- feat: add support for additional properties and support telemetry (#80) - big shout out and thanks to @dheid for this!
- fix: use AutoConfiguration instead (#64) - thanks @eddumelendez!
- feat!: update OpenFGA Java version and spring version dependencies (#74)

> [!WARNING]  
> This version includes version 0.7.2 of the [OpenFGA Java SDK](https://github.com/openfga/java-sdk/), which contains breaking changes to the OpenFGA Java SDK. Please see #66 for additional information.

## v0.0.1

### [0.0.1](https://github.com/openfga/spring-boot-starter/releases/tag/v0.0.1) (2024-04-09)

This is an initial beta release of the OpenFGA Spring Boot Starter. It provides:

- autoconfiguration of an `OpenFga` client
- exposes an `fga` bean to enable adding simple FGA checks to standard Spring Security method security.

For usage instructions, see the [README](./README.md).
