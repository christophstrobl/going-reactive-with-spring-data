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

import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import rx.Observable;
import rx.internal.reactivestreams.PublisherAdapter;

import java.time.Duration;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
@EnableReactiveMongoRepositories(considerNestedRepositories = true)
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired PersonRepository repository;
	@Autowired MongoTemplate template;

	@Override
	public void run(String... args) throws Exception {

		template.createCollection(Person.class, CollectionOptions.empty().capped().maxDocuments(10000).size(10000));

		String[] names = { "Eddard", "Catelyn", "Jon", "Rob", "Sansa", "Aria", "Bran", "Rickon" };

		Random ramdom = new Random();
		Flux<Person> starks = Flux.fromStream(Stream.generate(() -> names[ramdom.nextInt(names.length)]).map(Person::new));

		Flux.interval(Duration.ofSeconds(1)) //
				.zipWith(starks) //
				.map(Tuple2::getT2) //
				.flatMap(repository::save) //
				.doOnNext(System.out::println) //
				.subscribe();

		System.out.println("Winter is Coming!");
	}

	@Bean
	RouterFunction<ServerResponse> routerFunction(PersonHandler requestHandler) {

		return RouterFunctions //
				.route(RequestPredicates.GET("/"), requestHandler::fluxPersons)
				.andRoute(RequestPredicates.GET("/rx"), requestHandler::rxPersons)
				.andRoute(RequestPredicates.GET("/stream"), requestHandler::streamPersons);
	}

	@Component
	@RequiredArgsConstructor
	static class PersonHandler {

		final PersonRepository personRepository;

		Mono<ServerResponse> fluxPersons(ServerRequest request) {

			return ServerResponse.ok() //
					.body(personRepository.findAllByName(request.queryParam("name").orElse("")), Person.class);
		}

		Mono<ServerResponse> rxPersons(ServerRequest request) {

			return ServerResponse.ok() //
					.body(new PublisherAdapter(personRepository.findByName(request.queryParam("name").orElse(""))), Person.class);
		}

		Mono<ServerResponse> streamPersons(ServerRequest request) {

			return ServerResponse.ok() //
					.contentType(MediaType.TEXT_EVENT_STREAM) //
					.body(personRepository.findBy(), Person.class);
		}
	}

	interface PersonRepository extends ReactiveCrudRepository<Person, String> {

		Flux<Person> findAllByName(String name);

		Observable<Person> findByName(String name);

		@Tailable
		Flux<Person> findBy();
	}

	@Document
	@Data
	@RequiredArgsConstructor
	static class Person {

		@Id String id;
		final String name;
	}
}
