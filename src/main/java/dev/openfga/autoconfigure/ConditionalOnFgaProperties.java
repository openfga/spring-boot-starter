package dev.openfga.autoconfigure;

import dev.openfga.sdk.api.client.OpenFgaClient;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

/**
 * Conditional that activates the OpenFGA beans when a connection to OpenFGA can be resolved, either
 * because the {@code openfga.api-url} property is set or because an {@link OpenFgaConnectionDetails}
 * bean (for example, one contributed by a Testcontainers {@code @ServiceConnection}) is present.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnClass(OpenFgaClient.class)
@Conditional(ConditionalOnFgaProperties.FgaConnectionCondition.class)
public @interface ConditionalOnFgaProperties {

    /**
     * Matches when either the {@code openfga.api-url} property is set or an
     * {@link OpenFgaConnectionDetails} bean is available.
     */
    class FgaConnectionCondition extends AnyNestedCondition {

        FgaConnectionCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(name = "openfga.api-url")
        static class ApiUrlProperty {}

        @ConditionalOnBean(OpenFgaConnectionDetails.class)
        static class ConnectionDetailsBean {}
    }
}
