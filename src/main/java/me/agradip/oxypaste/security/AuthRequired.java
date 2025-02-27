package me.agradip.oxypaste.security;

import me.agradip.oxypaste.model.Token;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthRequired {
    boolean strict() default true;
    Token.TokenType tokenType() default Token.TokenType.API;
}
