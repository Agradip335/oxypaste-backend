package me.agradip.oxypaste;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "My API",
				version = "1.0",
				description = "This is a sample API for managing items."
		)
)
public class OxypasteApplication {

	public static void main(String[] args) {
		SpringApplication.run(OxypasteApplication.class, args);
	}
}
