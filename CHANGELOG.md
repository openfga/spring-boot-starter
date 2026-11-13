# Changelog

## [0.5.0](https://github.com/openfga/spring-boot-starter/compare/v0.4.1...v0.5.0) (2026-11-13)


### ⚠ BREAKING CHANGES

* **spring-boot:** upgrade to v4.1 baseline ([#201](https://github.com/openfga/spring-boot-starter/issues/201))

### Added

* **example:** add integration tests ([1c5cc5e](https://github.com/openfga/spring-boot-starter/commit/1c5cc5e7167eed07efee36ba993370678a37fcbd)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)
* **example:** always use default json serializer for initializer ([ce76511](https://github.com/openfga/spring-boot-starter/commit/ce7651109448ae733e749ddc1745ea11c840b1de)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)
* **example:** do not use ObjectMapper anymore ([8a17b33](https://github.com/openfga/spring-boot-starter/commit/8a17b339b9cd63e89608236c732f65096c84b01a)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)
* **example:** make it possible to use locally build unreleased starter version ([42e2366](https://github.com/openfga/spring-boot-starter/commit/42e2366fde5f0c30983fbcbc42f37862cd4ccde9)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)
* **readme:** add spring boot compat matrix and fix some typos ([8f68760](https://github.com/openfga/spring-boot-starter/commit/8f6876063ec1843c4e009778ebb0e85aa1305156))
* **sdk:** refactor towards JsonSerializer usage ([b0ecc90](https://github.com/openfga/spring-boot-starter/commit/b0ecc90c4762baf4751393c728b188ba3c5ffb82))
* **spring-boot:** upgrade to v4.1 baseline ([#201](https://github.com/openfga/spring-boot-starter/issues/201)) ([58b42d9](https://github.com/openfga/spring-boot-starter/commit/58b42d9aa61282f2a3ee22f13bd267221f7b52b8))


### Fixed

* **auto-configure:** have better ApiClient bean construction ([e3b6013](https://github.com/openfga/spring-boot-starter/commit/e3b6013272858cb1e343f09c338c96a01aca0818)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)
* **gradle:** check in the wrapper ([d029d31](https://github.com/openfga/spring-boot-starter/commit/d029d31800ca8597f821c2718eedabfb272dbdc4)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)
* **gradle:** use official checksum ([7545e6b](https://github.com/openfga/spring-boot-starter/commit/7545e6b2000b6e9a2ebb3d95f1439a0266035452)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)
* **gradle:** use valid wrapper distribution SHA ([baec943](https://github.com/openfga/spring-boot-starter/commit/baec943fb182614cbedca1520d2f0d6d656135a2)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)


### Changed

* **initializer:** use default internal JsonSerializer for tuple loading ([da8f7e9](https://github.com/openfga/spring-boot-starter/commit/da8f7e9d64fd4af9311d16ff88802e2c267bb0e2)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)
* **logging:** make sure trace logging params are not pre-evaluated ([16465da](https://github.com/openfga/spring-boot-starter/commit/16465dac63b636bcdf709cd4245d22ca7f67f290)), closes [#184](https://github.com/openfga/spring-boot-starter/issues/184)

## [0.4.1](https://github.com/openfga/spring-boot-starter/compare/v0.3.3...v0.4.1) (2026-08-12)


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
