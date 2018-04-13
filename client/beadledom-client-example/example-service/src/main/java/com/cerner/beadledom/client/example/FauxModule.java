package com.cerner.beadledom.client.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import javax.inject.Singleton;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;

/**
 * Guice Module to Configure the Faux Service.
 *
 * @author John Leacox
 * @since 1.0
 */
public class FauxModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new RequestScopeModule());
    bind(ResourceOneImpl.class);
    bind(ResourceTwoImpl.class);
  }

  @Provides
  @Singleton
  ObjectMapper provideObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    return objectMapper;
  }

  @Provides
  @Singleton
  JacksonJsonProvider provideJacksonJsonProvider(ObjectMapper objectMapper) {
    return new JacksonJsonProvider(objectMapper);
  }
}
