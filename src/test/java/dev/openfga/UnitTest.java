package dev.openfga;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Documented
@Retention(RUNTIME)
@Inherited
@Target(TYPE)
@ExtendWith(MockitoExtension.class)
public @interface UnitTest {}
