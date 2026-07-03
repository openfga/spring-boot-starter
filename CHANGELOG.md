# Changelog

## [0.3.2](https://github.com/openfga/spring-boot-starter/compare/v0.3.1...v0.3.2) (2026-07-03)


### Added

* add OpenFGA Testcontainers service connection ([3ab552c](https://github.com/openfga/spring-boot-starter/commit/3ab552cce7fdd9ed13d22fa6ea5e7022144b86fc)), closes [#58](https://github.com/openfga/spring-boot-starter/issues/58)
* add OpenFGA Testcontainers service connection ([#170](https://github.com/openfga/spring-boot-starter/issues/170)) ([42d93e2](https://github.com/openfga/spring-boot-starter/commit/42d93e23636dbcf34cf56dbd65f1a9a45e317b6e))


### Fixed

* support Spring Boot 4 by dropping removed PropertyMapper APIs ([396e9fa](https://github.com/openfga/spring-boot-starter/commit/396e9fa2f47767db056be2c7f978e1bbb51ec30c))
* support Spring Boot 4 by dropping removed PropertyMapper APIs ([#164](https://github.com/openfga/spring-boot-starter/issues/164)) ([b893624](https://github.com/openfga/spring-boot-starter/commit/b8936249729865385d9c90e9fff864f89a47c018))
* typo in changelog ([b0bcc4e](https://github.com/openfga/spring-boot-starter/commit/b0bcc4e7fbe6603912c2a23b0a28b295151c8b40))


### Documentation

* clarify activation in autoconfig javadoc, pin testcontainers image in README ([54de65c](https://github.com/openfga/spring-boot-starter/commit/54de65cafac30ccf5c1ec0af09e154495dc5f506))


### Miscellaneous

* release 0.3.2 ([9bcee5a](https://github.com/openfga/spring-boot-starter/commit/9bcee5a111109c20d161a83c5a76e7bc3f208b3f))

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
