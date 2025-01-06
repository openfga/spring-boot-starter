# OpenFGA Spring Boot Starter

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fopenfga%2Ffga-spring-boot.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fopenfga%2Ffga-spring-boot?ref=badge_shield)
[![Join our community](https://img.shields.io/badge/slack-cncf_%23openfga-40abb8.svg?logo=slack)](https://openfga.dev/community)
[![Twitter](https://img.shields.io/twitter/follow/openfga?color=%23179CF0&logo=twitter&style=flat-square "@openfga on Twitter")](https://twitter.com/openfga)

A Spring Boot Starter for OpenFGA.

## About

[OpenFGA](https://openfga.dev) is an open source Fine-Grained Authorization solution inspired
by [Google's Zanzibar paper](https://research.google/pubs/pub48190/). It was created by the FGA team
at [Auth0](https://auth0.com) based on [Auth0 Fine-Grained Authorization (FGA)](https://fga.dev), available
under [a permissive license (Apache-2)](https://github.com/openfga/rfcs/blob/main/LICENSE) and welcomes community
contributions.

OpenFGA is designed to make it easy for application builders to model their permission layer, and to add and integrate
fine-grained authorization into their applications. OpenFGA’s design is optimized for reliability and low latency at a
high scale.

## Resources

- [OpenFGA Documentation](https://openfga.dev/docs)
- [OpenFGA API Documentation](https://openfga.dev/api/service)
- [Twitter](https://twitter.com/openfga)
- [OpenFGA Community](https://openfga.dev/community)
- [Zanzibar Academy](https://zanzibar.academy)
- [Google's Zanzibar Paper (2019)](https://research.google/pubs/pub48190/)

## Installation

The OpenFGA Spring Boot Starter is available on [Maven Central](https://central.sonatype.com/).

It can be used with the following:

* Gradle (Groovy)

```groovy
implementation 'dev.openfga:openfga-spring-boot-starter:0.1.0'
```

* Gradle (Kotlin)

```kotlin
implementation("dev.openfga:openfga-spring-boot-starter:0.1.0")
```

* Apache Maven

```xml

<dependency>
    <groupId>dev.openfga</groupId>
    <artifactId>openfga-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Getting Started

### Requirements

Java >= 17 and Spring Boot >= 3

### Configuring the starter

The OpenFGA Spring Boot Starter can be configured via
standard [Spring configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config).
The configuration properties are used to create
an [OpenFgaClient](https://github.com/openfga/java-sdk/blob/main/src/main/java/dev/openfga/sdk/api/client/OpenFgaClient.java)
instance.

To initialize the OpenFGA Spring Boot Starter, please provide the configuration property `openfga.api-url`. An
`OpenFgaClient` instance will then be created with the provided configuration.

The following examples demonstrate how to configure the OpenFGA Spring Boot Starter.

#### No Credentials

```yaml
# src/main/resources/application.yaml

openfga:
  api-url: YOUR_FGA_API_URL
  store-id: YOUR_FGA_STORE_ID
  authorization-model-id: YOUR_FGA_AUTHORIZATION_MODEL_ID
```

#### API Token

```yaml
# src/main/resources/application.yaml

openfga:
  api-url: YOUR_FGA_API_URL
  store-id: YOUR_FGA_STORE_ID
  authorization-model-id: YOUR_FGA_AUTHORIZATION_MODEL_ID
  credentials:
    method: API_TOKEN # constant
    config:
      api-token: YOUR_API_TOKEN
```

#### Client Credentials

```yaml
# src/main/resources/application.yaml

openfga:
  api-url: YOUR_FGA_API_URL
  store-id: YOUR_FGA_STORE_ID
  authorization-model-id: YOUR_FGA_AUTHORIZATION_MODEL_ID
  credentials:
    method: CLIENT_CONFIGURATION # constant
    config:
      client-id: YOUR_CLIENT_ID
      client-secret: YOUR_CLIENT_SECRET
      api-token-issuer: YOUR_API_TOKEN_ISSUER
      api-audience: YOUR_API_AUDIENCE
      scopes: YOUR_SPACE_SEPERATED_SCOPES
```

#### Full Configuration Example

```yaml
# src/main/resources/application.yaml

openfga:
  api-url: YOUR_FGA_API_URL
  store-id: YOUR_FGA_STORE_ID
  authorization-model-id: YOUR_FGA_AUTHORIZATION_MODEL_ID
  user-agent: YOUR_USER_AGENT # default: openfga-sdk java/version
  read-timeout: 1m # default: 10 seconds
  connect-timeout: 15 # default: 10 seconds
  max-retries: 3 # default: no retries
  minimum-retry-delay: 1m
  http-version: HTTP_2
  default-headers:
    X-SOME-HEADER: Some Header Value
  telemetry-configuration:
    fga_client_request_model_id: YOUR_FGA_CLIENT_REQUEST_MODEL_ID
  credentials:
    method: CLIENT_CONFIGURATION # constant
    config:
      client-id: YOUR_CLIENT_ID
      client-secret: YOUR_CLIENT_SECRET
      api-token-issuer: YOUR_API_TOKEN_ISSUER
      api-audience: YOUR_API_AUDIENCE
      scopes: YOUR_SPACE_SEPERATED_SCOPES
```

### Configuration Properties

The OpenFGA Spring Boot Starter can be configured using the following properties:

#### `openfga.api-url`

- **Description**: The base URL of the OpenFGA API endpoint.
- **Example**: `https://api.openfga.example.com`

#### `openfga.store-id`

- **Description**: The unique identifier for the store in OpenFGA.
- **Example**: `store-12345`

#### `openfga.authorization-model-id`

- **Description**: The unique identifier for the authorization model in OpenFGA.
- **Example**: `auth-model-67890`

#### `openfga.user-agent`

- **Description**: The user agent string to be included in the request headers.
- **Example**: `MyApp/1.0.0`
- **Default**: `openfga-sdk java/version`

#### `openfga.read-timeout`

- **Description**: The maximum duration to wait for a read operation to complete. Default unit is seconds. Must be positive or null.
- **Example**: `30s`
- **Default**: `10s`

#### `openfga.connect-timeout`

- **Description**: The maximum duration to wait for a connection to be established. Default unit is seconds. Must be positive or null.
- **Example**: `10s`
- **Default**: `10s`

#### `openfga.max-retries`

- **Description**: The maximum number of retry attempts for failed requests. Must be positive or null. If you set this to a positive value, ensure that you also set the `minimum-retry-delay` property.
- **Example**: `5`
- **Default**: No retries

#### `openfga.minimum-retry-delay`

- **Description**: The minimum delay between retry attempts. Default unit is seconds. Must be positive or null. Only used if `max-retries` is set.
- **Example**: `500ms`
- **Default**: `10s`

#### `openfga.http-version`

- **Description**: The HTTP version to use for requests.
- **Example**: `HTTP_1_1`
- **Default**: `HTTP_2`

#### `openfga.default-headers`

- **Description**: Default headers to be included in all requests.
- **Example**:

  ```yaml
  default-headers:
    X-Custom-Header: CustomHeaderValue

  ```

#### `openfga.telemetry-configuration`

- **Description**: Configuration settings for telemetry, which help in monitoring and logging the behavior of the
  OpenFGA client.
- **Example**:

  ```yaml
  telemetry-configuration:
    fga_client_request_model_id: "example-model-id"
  ```

#### `openfga.credentials.method`

- **Description**: Specifies the authentication method to be used for connecting to the OpenFGA API.
- **Possible Values**:
  - `NONE`: No authentication.
  - `API_TOKEN`: Use an API token for authentication.
  - `CLIENT_CREDENTIALS`: Use OAuth2 client credentials for authentication.

#### `openfga.credentials.config.api-token`

- **Description**: The API token used for authenticating requests when the `API_TOKEN` method is selected.
- **Example**: `your-api-token`

#### `openfga.credentials.config.client-id`

- **Description**: The client ID used for OAuth2 authentication when the `CLIENT_CREDENTIALS` method is selected.
- **Example**: `your-client-id`

#### `openfga.credentials.config.client-secret`

- **Description**: The client secret used for OAuth2 authentication when the `CLIENT_CREDENTIALS` method is selected.
- **Example**: `your-client-secret`

#### `openfga.credentials.config.api-token-issuer`

- **Description**: The issuer of the API token used for OAuth2 authentication when the `CLIENT_CREDENTIALS` method is
  selected.
- **Example**: `https://issuer.example.com`

#### `openfga.credentials.config.api-audience`

- **Description**: The audience for the API token used for OAuth2 authentication when the `CLIENT_CREDENTIALS` method is
  selected.
- **Example**: `https://api.example.com`

#### `openfga.credentials.config.scopes`

- **Description**: The scopes required for OAuth2 authentication when the `CLIENT_CREDENTIALS` method is selected.
  Scopes are space-separated.
- **Example**: `read write`

### Using the `fgaClient` bean

Once configured, an `fgaClient` bean is available to be injected into your Spring components:

```java

@Service
public class MyService {

    @Autowired
    private OpenFgaClient fgaClient;
}
```

This can be used to interact with the FGA API, for example to write authorization data:

```java
public Document createDoc(String id) {
    // ...
    ClientWriteRequest writeRequest = new ClientWriteRequest().writes(List.of(new ClientTupleKey()
            .user(String.format("user:%s", SecurityContextHolder.getContext().getAuthentication()))
            .relation("owner")
            ._object(String.format("document:%s", id))));

    try {
        fgaClient.write(writeRequest).get();
    } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
        throw new RuntimeException("Error writing to FGA", e);
    }
    // ...
}
```

### Using the `fga` bean

The starter also creates an `fga` bean, which can be used in conjunction with Spring Security's method
security to protect access to resources using FGA:

```java
// Method body will only execute if the FGA check returns true. 403 otherwise.
@PreAuthorize("@fga.check('document', #docId, 'reader', 'user', authentication?.name)")
public Document getDocument(@PathVariable String docId) {
    return repository.findById(id);
}
```

You may also omit the user ID, in which case the name of the currently authenticated principal
will be used as the user ID:

```java
// Method body will only execute if the FGA check returns true. 403 otherwise.
@PreAuthorize("@fga.check('document', #docId, 'reader', 'user')")
public Document getDocument(@PathVariable String docId) {
    return repository.findById(id);
}
```

## Customize ApiClient and HttpClient Configuration

To customize the `ApiClient` configuration, create a `@Bean` method in your Spring Boot application:

```java
@Bean
public ApiClient apiClient(HttpClient.Builder builder, ObjectMapper mapper) {
    return new ApiClient(httpClientBuilder, objectMapper);
}
```

Similarly, to customize the `HttpClient.Builder`:

```java
@Bean
public HttpClient.Builder httpClientBuilder() {
    return HttpClient.newBuilder()
            .version(Version.HTTP_2);
}
```

## Contributing

### Issues

If you have found a bug or if you have a feature request,
please [create an issue](https://github.com/openfga/fga-spring-boot/issues). Please do not report security
vulnerabilities on the public GitHub issue tracker.

### Pull Requests

Pull requests are welcome, however, we do kindly ask that for non-trivial changes or feature additions, that you create
an [issue]((https://github.com/openfga/fga-spring-boot/issues)) first.

## Author

[OpenFGA](https://github.com/openfga)

## License

This project is licensed under the Apache-2.0 license. See
the [LICENSE](https://github.com/openfga/fga-spring-boot/blob/main/LICENSE) file for more info.
