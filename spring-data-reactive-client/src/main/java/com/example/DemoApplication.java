/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	WebClient client() {
		return WebClient.create("http://localhost:8080/");
	}

	@Bean
	CommandLineRunner run(WebClient client) {

		return (args) -> {

			client.get() //
					.uri(builder -> builder.path("/").queryParam("name", "Eddard").build()) //
					.retrieve() //
					.bodyToFlux(Person.class) //
					.subscribe(System.out::println);
		};

	}

	@Data
	@AllArgsConstructor
	static class Person {
		String id;
		String name;
	}
}
