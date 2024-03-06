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
