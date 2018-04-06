package com.cerner.beadledom.swagger2;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.MultibindingsScanner;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Info;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Configures a service to serve Swagger 2 documentation at '/api-docs'.
 *
 * <p>To use this, you must provide a SwaggerConfig such as the following:
 *
 * <p><pre>{@code @Provides
 *   Info provideSwaggerConfig(ServiceMetadata serviceMetadata) {
 *     Info info = new Info();
 *     info.title("Name of My Service")
 *         .description("A description of my service. My service lets you do some things. It's owned "
 *             + " by My Awesome Team")
 *         .getVersion(serviceMetadata.getBuildInfo().getVersion());
 *     return info;
 *   }
 * }</pre>
 *
 * <p>Then annotate your resources, operations, and models with the Swagger annotations.
 *
 * <p>Provides the following JAX-RS resources and providers:
 * <ul>
 *     <li>{@link SwaggerApiResource}</li>
 *     <li>{@link SwaggerSerializers}</li>
 * </ul>
 *
 * <p>Requires:
 * <ul>
 *     <li>{@link io.swagger.models.Info}</li>
 * </ul>
 *
 * <p>You may also supply set bindings for {@link io.swagger.converter.ModelConverter}.
 * These will be added to the list of model converters (before the default converter, but otherwise
 * in unspecified order).
 *
 * <p>Installs:
 * <ul>
 *   <li> {@link MultibindingsScanner} </li>
 * </ul>
 */
public class Swagger2Module extends AbstractModule {
  @Override
  protected void configure() {
    requireBinding(Info.class);

    // Create empty multibinder in case no ModelConverter bindings exist
    Multibinder.newSetBinder(binder(), ModelConverter.class);

    bind(SwaggerApiResource.class);

    bind(SwaggerSerializers.class);

    bind(JaxrsScanner.class).to(SwaggerGuiceJaxrsScanner.class);

    bind(SwaggerLifecycleHook.class).asEagerSingleton();

    install(MultibindingsScanner.asModule());
  }

  static class SwaggerLifecycleHook {
    private final JaxrsScanner jaxrsScanner;
    private final Set<ModelConverter> modelConverters;

    @Inject
    SwaggerLifecycleHook(
        JaxrsScanner jaxrsScanner,
        Set<ModelConverter> modelConverters) {
      this.jaxrsScanner = jaxrsScanner;
      this.modelConverters = modelConverters;
    }

    @PostConstruct
    public void startup() {
      for (ModelConverter modelConverter : modelConverters) {
        ModelConverters.getInstance().addConverter(modelConverter);
      }

      // Swagger uses a lot of static state / servlet context to store things. This is how we make
      // sure it is initialized with our scanner.
      new SwaggerContextService().withScanner(jaxrsScanner).initScanner();
    }
  }
}
