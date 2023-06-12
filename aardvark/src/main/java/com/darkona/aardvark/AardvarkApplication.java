package com.darkona.aardvark;

import com.github.JanLoebel.jsonschemavalidation.provider.DefaultJsonSchemaProvider;
import com.github.JanLoebel.jsonschemavalidation.provider.JsonSchemaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class AardvarkApplication {

	public static void main(String[] args) {
		SpringApplication.run(AardvarkApplication.class, args);
	}

	@Bean
	public JsonSchemaProvider JsonSchemaProviderBean(){
		return new DefaultJsonSchemaProvider();
	}

}
