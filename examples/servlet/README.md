# Spring Boot OpenFGA Servlet Example

An example Spring Boot application that demonstrates using OpenFGA Spring Boot Starter.

## Requirements

- Java 17
- Docker

## Configuration

This example is configured to connect to the OpenFGA server running on port 4000.
To use a different FGA server, update `src/main/resources/application.yaml` accordingly.

## Usage

### Start OpenFGA server

In a terminal, start the OpenFGA Server:

```bash
docker pull openfga/openfga:latest
docker run --rm -e OPENFGA_HTTP_ADDR=0.0.0.0:4000 -p 4000:4000 -p 8081:8081 -p 3000:3000 openfga/openfga run
```

### Start the example application:

In a separate terminal, start the application:

```bash
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

```bash
curl http://localhost:8080/documents/2
```

You should receive a 403 response, as user `anne` does not have the required relation to document 2.

You can also create a document, for which user `anne` will be granted the owner relation for the document:

```bash
curl -d '{"id": "10", "content": "new document content"}' -H 'Content-Type: application/json' http://localhost:8080/documents
```

## Using example with local unpublished starter

To run the example using a non-published version of the Okta FGA Spring Boot Starter, first publish the starter to your local maven repository.

In the root directory of this repository, run:

```bash
./gradlew assemble publishToMavenLocal
```

Update `examples/servlet/build.gradle` to use your local maven repository:

```groovy
repositories {
  mavenLocal()
  mavenCentral()
}
```

You can then run the application as documented above.
