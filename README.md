# OpenFGA Spring Boot Starter

A Spring Boot Starter for OpenFGA.

## Configuration

Configure your application properties:

```yaml
openfga:
  api-url: FGA-API-URL
  store-id: STORE-ID
  authorization-model-id: AUTHORIZATION-MODEL-ID
  credentials:
    api-token: API-TOKEN # takes precedence if set
    client-id: CLIENT-ID
    client-secret: CLIENT-SECRET
    api-token-issuer: API-TOKEN-ISSUER
    api-audience: API-AUDIENCE
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
