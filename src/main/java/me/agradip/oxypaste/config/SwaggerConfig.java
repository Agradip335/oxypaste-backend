package me.agradip.oxypaste.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.util.IOUtil;
import me.agradip.oxypaste.util.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {
    @Autowired
    private AppConfig appConfig;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OxyPaste API")
                        .version("1.0")
                        .description(IOUtil.readResourceFromCp("documents/API-DESCRIPTION.md"))
                ).components(new Components()
                        .addSecuritySchemes("BearerAuthentication",
                                new SecurityScheme()
                                        .name("API")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .scheme("bearer")
                                        .description("Your API Token")
                        )
                );
    }

//    @Bean
//    public OpenApiCustomizer openApiCustomizer() {
//        return openApi -> {
//            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
//                return;
//            }
//
//            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
//            String apiResponseName = ResponsesDto.ApiResponse.class.getSimpleName(); // Get class name dynamically
//
//            // Temporary storage to avoid modifying map while iterating
//            Map<String, Schema<?>> updatedSchemas = new HashMap<>();
//
//            schemas.forEach((name, schema) -> {
//                if (!name.equals(apiResponseName)) { // Only wrap non-ApiResponse schemas
//                    Schema<Object> wrappedSchema = new ObjectSchema();
//                    wrappedSchema.$ref("#/components/schemas/" + apiResponseName);
//
//                    // Create the data property correctly as a reference to the original schema
//                    Schema<Object> dataSchema = new Schema<>();
//                    dataSchema.$ref("#/components/schemas/" + name);
//
//                    // Add data field properly
//                    wrappedSchema.setProperties(Map.of("data", dataSchema));
//
//                    updatedSchemas.put(name, wrappedSchema); // Replace original schema
//                }
//            });
//
//            // Apply modifications
//            schemas.putAll(updatedSchemas);
//        };
//    }
}