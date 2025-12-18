package aletheia.project.Aletheia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@SpringBootApplication
public class AletheiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AletheiaApplication.class, args);
	}

	@Bean
	public LayoutDialect layoutDialect() {
	return new LayoutDialect();
	}

}
