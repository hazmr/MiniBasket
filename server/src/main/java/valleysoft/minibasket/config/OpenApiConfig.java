package valleysoft.minibasket.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import valleysoft.minibasket.common.exception.ErrorCode;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info = @Info(title = "MiniBasket API", version = "v1", description = "Demo shop backend"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer errorContract() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            openApi.getComponents()
                    .addSchemas("ErrorCode", errorCodeSchema())
                    .addSchemas("Violation", violationSchema())
                    .addSchemas("Problem", problemSchema());
        };
    }

    private static Schema<String> errorCodeSchema() {
        StringSchema schema = new StringSchema();
        Stream.of(ErrorCode.values()).forEach(code -> schema.addEnumItem(code.name()));
        schema.description("Every errorCode the API can return. Clients choose their wording by this value. "
                + "{placeholders} arrive in the error's params.\n\n"
                + Stream.of(ErrorCode.values())
                        .map(code -> "- " + code.name() + " (" + code.status().value() + "): " + code.template())
                        .collect(Collectors.joining("\n")));
        return schema;
    }

    private static Schema<Object> violationSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.description("One invalid field, e.g. {field: \"password\", code: \"Size\", params: {min: 8, max: 72}}");
        schema.addProperty("field", new StringSchema());
        schema.addProperty("code", new StringSchema().description("The Bean Validation constraint, e.g. NotBlank, Size, Email"));
        schema.addProperty("params", new ObjectSchema().additionalProperties(true));
        schema.addProperty("message", new StringSchema().description("English text for developers"));
        schema.setRequired(List.of("field", "code", "message"));
        return schema;
    }

    private static Schema<Object> problemSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.description("The body of every error response (RFC 9457 Problem Details), content type application/problem+json");
        schema.addProperty("type", new StringSchema());
        schema.addProperty("title", new StringSchema());
        schema.addProperty("status", new IntegerSchema());
        schema.addProperty("detail", new StringSchema().description("English text for developers and logs; clients show their own wording"));
        schema.addProperty("instance", new StringSchema());
        schema.addProperty("errorCode", new Schema<String>().$ref("#/components/schemas/ErrorCode"));
        schema.addProperty("params", new ObjectSchema().additionalProperties(true)
                .description("Values for the error's {placeholders}, e.g. {available: 2, productName: \"Nova X1\"}"));
        schema.addProperty("traceId", new StringSchema().description("Same as the X-Request-Id header; quote it when reporting a problem"));
        schema.addProperty("violations", new ArraySchema().items(new Schema<Object>().$ref("#/components/schemas/Violation")));
        schema.setRequired(List.of("status", "errorCode"));
        return schema;
    }
}
