# Spring Boot OpenFGA Servlet Example

An example Spring Boot application that demonstrates using OpenFGA Spring Boot Starter.

## Requirements

- Java 17
- Docker

## Configuration

This example is configured to connect to the OpenFGA server running on port 4000.
To use a different FGA server, update `src/main/resources/application.yaml` accordingly.

## Usage

### Start the example application:

To run a local OpenFGA instance, you can use the provided `docker-compose.yml` file:

```shell
docker-compose up -d
```

In a terminal, start the application:

```shell
./gradlew bootRun
```

This will start the application on port 8080. As part of the application startup, some data is loaded:

- Two documents, with IDs `1` and `2`
- A simple FGA authorization model, along with an authorization tuple that grants user `anne` viewer access to document `1`.

### Make requests

Execute a GET request for document 1, for which user `anne` has viewer access:

```bash
curl http://localhost:8080/documents/1 
```

You should receive a 200 response with the document:

```json
{
  "id": "1",
  "content": "this is document 1 content"
}
```

Execute a request for document 2, for which user `anne` does **not** have viewer access to:

```shell
curl http://localhost:8080/documents/2
```

You should receive a 403 response, as user `anne` does not have the required relation to document 2.

You can also create a document, for which user `anne` will be granted the owner relation for the document:

```shell
curl -d '{"id": "10", "content": "new document content"}' -H 'Content-Type: application/json' http://localhost:8080/documents
```

## Using the current starter checkout

By default, the example resolves its release-managed starter dependency from Maven Central. The
published `0.4.1` release targets Spring Boot 3, while this example uses Spring Boot 4.1. Until a
Spring Boot 4-compatible starter release is published, publish the current starter checkout to a
local Maven repository and opt in to that artifact.

From the starter repository root, publish the starter:

```shell
./gradlew publishToMavenLocal
```

Repeat this command after making changes to the starter. The starter build applies publication
signing, so `publishToMavenLocal` requires the configured signing credentials; without a configured
signatory the publication task fails.

Then, from `examples/servlet`, run the application or its tests with the local starter enabled:

```shell
./gradlew -PuseLocalStarter=true bootRun
./gradlew -PuseLocalStarter=true test
```

The property routes only `dev.openfga:openfga-spring-boot-starter` to Maven Local; all other
dependencies still resolve from Maven Central. If the starter is not present locally, Gradle fails
instead of falling back to the published release. The example's `@ServiceConnection` test support
is supplied by the starter JAR, including its `META-INF/spring.factories` registration.

The local development artifact has the same `0.4.1` coordinates as the published Spring Boot 3
release. Publishing it to your normal Maven Local repository can replace another locally installed
artifact with those coordinates. To keep the development artifact isolated, choose an absolute
repository path and pass the same `-Dmaven.repo.local` option to both builds.

From the starter repository root:

```shell
./gradlew publishToMavenLocal -Dmaven.repo.local=/absolute/path/to/openfga-maven-repository
```

From `examples/servlet`:

```shell
./gradlew -PuseLocalStarter=true bootRun -Dmaven.repo.local=/absolute/path/to/openfga-maven-repository
./gradlew -PuseLocalStarter=true test -Dmaven.repo.local=/absolute/path/to/openfga-maven-repository
```
