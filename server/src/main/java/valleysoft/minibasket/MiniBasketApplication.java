package valleysoft.minibasket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MiniBasketApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniBasketApplication.class, args);
	}

}
