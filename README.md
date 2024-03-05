# OpenFGA Spring Boot Starter

A Spring Boot Starter for OpenFGA.

## Configuration

Configure your application properties:

```yaml
openfga:
  api-url: FGA_API_URL
  store-id: STORE_ID
  authorization-model-id: AUTHORIZATION_MODEL_ID
  credentials:
    api-token: API-TOKEN # takes precedence if set
    client-id: CLIENT_ID
    client-secret: CLIENT_SECRET
    api-token-issuer: API_TOKEN_ISSUER
    api-audience: API_AUDIENCE
    scopes: SCOPE1 SCOPE2
```

Your application can then inject the configured `openFgaClient`:

```java
@Service
public class MyService {
    
    @Autowired
    private OpenFgaClient openFgaClient;
}
```
