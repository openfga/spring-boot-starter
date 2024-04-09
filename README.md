# OpenFGA Spring Boot Starter

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fopenfga%2Ffga-spring-boot.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fopenfga%2Ffga-spring-boot?ref=badge_shield)
[![Join our community](https://img.shields.io/badge/slack-cncf_%23openfga-40abb8.svg?logo=slack)](https://openfga.dev/community)
[![Twitter](https://img.shields.io/twitter/follow/openfga?color=%23179CF0&logo=twitter&style=flat-square "@openfga on Twitter")](https://twitter.com/openfga)

A Spring Boot Starter for OpenFGA.

## About

[OpenFGA](https://openfga.dev) is an open source Fine-Grained Authorization solution inspired by [Google's Zanzibar paper](https://research.google/pubs/pub48190/). It was created by the FGA team at [Auth0](https://auth0.com) based on [Auth0 Fine-Grained Authorization (FGA)](https://fga.dev), available under [a permissive license (Apache-2)](https://github.com/openfga/rfcs/blob/main/LICENSE) and welcomes community contributions.

OpenFGA is designed to make it easy for application builders to model their permission layer, and to add and integrate fine-grained authorization into their applications. OpenFGA’s design is optimized for reliability and low latency at a high scale.

## Resources

- [OpenFGA Documentation](https://openfga.dev/docs)
- [OpenFGA API Documentation](https://openfga.dev/api/service)
- [Twitter](https://twitter.com/openfga)
- [OpenFGA Community](https://openfga.dev/community)
- [Zanzibar Academy](https://zanzibar.academy)
- [Google's Zanzibar Paper (2019)](https://research.google/pubs/pub48190/)

## Getting Started

### Requirements

Java 17 and Spring Boot 3

### Configuring the starter

The OpenFGA Spring Boot Starter can be configured via standard [Spring configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config).
The configuration properties are used to create an [OpenFgaClient](https://github.com/openfga/java-sdk/blob/main/src/main/java/dev/openfga/sdk/api/client/OpenFgaClient.java) instance.

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
    ClientWriteRequest writeRequest =  new ClientWriteRequest()
            .writes(List.of(new ClientTupleKey()
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

## Contributing

### Issues

If you have found a bug or if you have a feature request, please [create an issue](https://github.com/openfga/fga-spring-boot/issues). Please do not report security vulnerabilities on the public GitHub issue tracker.

### Pull Requests

Pull requests are welcome, however we do kindly ask that for non-trivial changes or feature additions, that you create an [issue]((https://github.com/openfga/fga-spring-boot/issues)) first.

## Author

[OpenFGA](https://github.com/openfga)

## License

This project is licensed under the Apache-2.0 license. See the [LICENSE](https://github.com/openfga/fga-spring-boot/blob/main/LICENSE) file for more info.
