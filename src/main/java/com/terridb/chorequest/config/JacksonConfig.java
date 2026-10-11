package com.terridb.chorequest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.StringDeserializer;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {
    @Bean
    public SimpleModule stringTrimmingModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StringDeserializer() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
                String value = super.deserialize(p, ctxt);
                return value != null ? value.strip() : null;
            }
        });

        return module;
    }
}
