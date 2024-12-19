package dev.openfga.autoconfigure;

import dev.openfga.sdk.api.client.OpenFgaClient;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(name = {"openfga.api-url"})
@ConditionalOnClass(OpenFgaClient.class)
public @interface ConditionalOnFgaProperties {}
