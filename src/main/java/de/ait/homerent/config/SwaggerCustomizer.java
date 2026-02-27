package de.ait.homerent.config;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 16.02.2026
 * Project : HomeRent
 * ----------------------------------------------------------------------------
 */
@Component
public class SwaggerCustomizer extends SwaggerIndexPageTransformer {

    public SwaggerCustomizer(SwaggerUiConfigProperties swaggerUiConfig,
                             SwaggerUiOAuthProperties swaggerUiOAuthProperties,
                             SwaggerUiConfigParameters swaggerUiConfigParameters,
                             SwaggerWelcomeCommon swaggerWelcomeCommon,
                             ObjectMapperProvider objectMapperProvider) {
        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerUiConfigParameters, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    @NonNull
    public Resource transform(@NonNull HttpServletRequest request,
                              @NonNull Resource resource,
                              @NonNull ResourceTransformerChain transformerChain) throws IOException {
        if ("index.html".equals(resource.getFilename())) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String html = reader.lines().collect(Collectors.joining(System.lineSeparator()));


                String cssLink = "<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/swagger-custom.css\">";
                String scriptTag = "<script src=\"/js/swagger-custom.js\"></script>";

                String modifiedHtml = html.replace("</head>", cssLink + "</head>");
                modifiedHtml = modifiedHtml.replace("</body>", scriptTag + "</body>");

                return new TransformedResource(resource, modifiedHtml.getBytes());
            } catch (Exception e) {

            }
        }
        return super.transform(request, resource, transformerChain);
    }
}
