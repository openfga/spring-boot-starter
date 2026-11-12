package dev.openfga.autoconfigure;

import dev.openfga.sdk.api.client.JsonSerializer;
import jakarta.annotation.Nonnull;

final class JsonSerializerFactory {
    private JsonSerializerFactory() {
        // utility class, intentionally empty
    }

    @Nonnull
    static JsonSerializer createDefaultJsonSerializer() {
        return JsonSerializer.createDefault();
    }
}
