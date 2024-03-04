package dev.openfga.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.is;

public class FgaAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeanConfiguredIfMissingProperties() {
        this.contextRunner
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    assertThat(context.containsBean("openFgaClient"), is(false));
                });
    }

    @Test
    public void beanConfiguredIfPropertiesPresent() {
        this.contextRunner
                .withPropertyValues("openfga.fgaApiUrl=https://fga-api-url")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    assertThat(context.containsBean("openFgaClient"), is(true));
                });
    }
}
