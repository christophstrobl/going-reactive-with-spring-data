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
import reactor.util.function.Tuple2;
import rx.Observable;

import java.time.Duration;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableReactiveMongoRepositories(considerNestedRepositories = true)
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired PersonRepository repository;

	@Override
	public void run(String... args) throws Exception {

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

	@RestController
	@RequiredArgsConstructor
	static class PersonController {

		final PersonRepository repository;

		@GetMapping("/") /* curl localhost:8080/?name=Eddard */
		Flux<Person> fluxPersons(String name) {
			return repository.findAllByName(name);
		}

		@GetMapping("/rx") /* curl localhost:8080/rx?name=Eddard */
		Observable<Person> rxPersons(String name) {
			return repository.findByName(name);
		}

		// TODO: It would be really cool if we could just stream the data!
	}

	interface PersonRepository extends ReactiveCrudRepository<Person, String> {

		Flux<Person> findAllByName(String name);

		Observable<Person> findByName(String name);
	}

	@Document
	@Data
	@RequiredArgsConstructor
	static class Person {

		@Id String id;
		final String name;
	}
}
