# Changelog

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
