package dev.openfga.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.configuration.ClientCheckOptions;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.model.ConsistencyPreference;
import dev.openfga.sdk.api.model.CreateStoreRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.openfga.OpenFGAContainer;

@Testcontainers(disabledWithoutDocker = true)
class OpenFgaInitializerContainerTest {

    private static final String TUPLES = "classpath:fga/tuples.json";
    private static final String DURING_GRANT = "2023-01-01T00:30:00Z";
    private static final String AFTER_GRANT = "2023-01-01T02:00:00Z";

    @Container
    static OpenFGAContainer openfga = new OpenFGAContainer("openfga/openfga:v1.21.0");

    @TempDir
    Path directory;

    private OpenFgaClient client;
    private String storeId;

    @BeforeEach
    void createStoreWithReaderOfDocument2() throws Exception {
        client = new OpenFgaClient(new ClientConfiguration().apiUrl(openfga.getHttpEndpoint()));
        storeId = client.createStore(new CreateStoreRequest().name("initializer-test"))
                .get()
                .getId();
        client.setStoreId(storeId);

        initialize();
        client.writeTuples(List.of(
                        new ClientTupleKey().user("user:123").relation("reader")._object("document:2")))
                .get();
    }

    @AfterEach
    void deleteStore() throws Exception {
        client.deleteStore().get();
    }

    @Test
    void appliesTupleFileOnEveryStartup() throws Exception {
        initialize(TUPLES);

        assertThat(canRead("document:1", DURING_GRANT)).isTrue();
        assertThat(canRead("document:1", AFTER_GRANT)).isFalse();
        assertThat(canRead("document:2", DURING_GRANT)).isFalse();

        initialize(TUPLES);

        assertThat(canRead("document:1", DURING_GRANT)).isTrue();
        assertThat(canRead("document:1", AFTER_GRANT)).isFalse();
        assertThat(canRead("document:2", DURING_GRANT)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("tupleFilesWithUnknownProperties")
    void rejectsUnknownPropertiesWithoutChangingTuples(String tuples, String message) throws Exception {
        var file = Files.writeString(directory.resolve("tuples.json"), tuples);

        assertThatThrownBy(() -> initialize(file.toUri().toString()))
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage(message);

        assertThat(canRead("document:1", DURING_GRANT)).isFalse();
        assertThat(canRead("document:2", DURING_GRANT)).isTrue();
    }

    static Stream<Arguments> tupleFilesWithUnknownProperties() {
        return Stream.of(
                Arguments.of("""
                        {"write": [{"user": "user:123", "relation": "reader", "object": "document:1"}]}
                        """, "Unknown property in OpenFGA initial tuples: write"),
                Arguments.of("""
                        {"writes": [{"user": "user:123", "relation": "reader", "object": "document:1",
                                     "conditon": {"name": "non_expired_grant"}}]}
                        """, "Unknown property in OpenFGA initial tuple write: conditon"),
                Arguments.of("""
                        {"writes": [{"user": "user:123", "relation": "reader", "object": "document:1",
                                     "condition": {"name": "non_expired_grant", "contxt": {}}}]}
                        """, "Unknown property in OpenFGA initial tuple condition: contxt"),
                Arguments.of("""
                        {"deletes": [{"user": "user:123", "relation": "reader", "object": "document:2",
                                      "condition": {"name": "non_expired_grant"}}]}
                        """, "Unknown property in OpenFGA initial tuple delete: condition"));
    }

    private boolean canRead(String document, String currentTime) throws Exception {
        var request = new ClientCheckRequest()
                .user("user:123")
                .relation("reader")
                ._object(document)
                .context(Map.of("current_time", currentTime));
        var options = new ClientCheckOptions().consistency(ConsistencyPreference.HIGHER_CONSISTENCY);
        return client.check(request, options).get().getAllowed();
    }

    private void initialize() {
        start();
    }

    private void initialize(String tuplesLocation) {
        start("--openfga.initialization.tuples-location=" + tuplesLocation);
    }

    private void start(String... arguments) {
        var common = Stream.of(
                "--spring.main.banner-mode=off",
                "--openfga.api-url=" + openfga.getHttpEndpoint(),
                "--openfga.store-id=" + storeId,
                "--openfga.initialization.mode=embedded",
                "--openfga.initialization.model-location=classpath:fga/model.json");
        new SpringApplicationBuilder(TestConfig.class)
                .web(WebApplicationType.NONE)
                .run(Stream.concat(common, Stream.of(arguments)).toArray(String[]::new))
                .close();
    }

    @SpringBootConfiguration
    @ImportAutoConfiguration(OpenFgaAutoConfiguration.class)
    static class TestConfig {}
}
