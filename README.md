# OpenFGA Spring Boot Starter

A Spring Boot Starter for OpenFGA.

## Configuration

No authorization:

```yaml
openfga:
  api-url: YOUR_FGA_API_URL
  store-id: YOUR_FGA_STORE_ID
  authorization-model-id: YOUR_FGA_AUTHORIZATION_MODEL_ID
```

API token authorization:

```yaml
openfga:
  api-url: YOUR_FGA_API_URL
  store-id: YOUR_FGA_STORE_ID
  authorization-model-id: YOUR_FGA_AUTHORIZATION_MODEL_ID
  credentials:
    method: API_TOKEN
    config:
      api-token: YOUR_API_TOKEN
```

Client credentials authorization:

```yaml
openfga:
  api-url: YOUR_FGA_API_URL
  store-id: YOUR_FGA_STORE_ID
  authorization-model-id: YOUR_FGA_AUTHORIZATION_MODEL_ID
  credentials:
    method: CLIENT_CONFIGURATION
    config:
        client-id: YOUR_CLIENT_ID
        client-secret: YOUR_CLIENT_SECRET
        api-token-issuer: YOUR_API_TOKEN_ISSUER
        api-audience: YOUR_API_AUDIENCE
        scopes: YOUR_SPACE_SEPERATED_SCOPES
```

Your application can then inject the configured `openFgaClient`:

```java
@Service
public class MyService {
    
    @Autowired
    private OpenFgaClient openFgaClient;
}
```

## Usage

Once configured, your application can inject the configured `openFgaClient` bean into any component.
This can be used to interact with the FGA API, for example to write authorization data:

```java
public Document createDoc(String id) {
    // ...
    ClientWriteRequest writeRequest =  new ClientWriteRequest()
            .writes(List.of(new ClientTupleKey()
                    .user(String.format("user:%s", SecurityContextHolder.getContext().getAuthentication()))
                    .relation("reader")
                    ._object(String.format("document:%s", id))));

    try {
        fgaClient.write(writeRequest).get();
    } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
        throw new RuntimeException("Error writing to FGA", e);
    }
    // ...
}
```

This starter also creates an `openFga` bean, which can be used in conjunction with Spring Security's method
security to protect access to resources using FGA:

```java
// Method body will only execute if the FGA check returns true. 403 otherwise.
@PreAuthorize("@openFga.check('document', #docId, 'reader', 'user', 'authentication?.name')")
public Document getDocument(@PathVariable String docId) {
    return repository.findById(id);
}
```

You may also omit the user ID, in which case the name of the currently authenticated principal 
will be used as the user ID:

```java
// Method body will only execute if the FGA check returns true. 403 otherwise.
@PreAuthorize("@openFga.check('document', #docId, 'reader', 'user')")
public Document getDocument(@PathVariable String docId) {
    return repository.findById(id);
}
```
