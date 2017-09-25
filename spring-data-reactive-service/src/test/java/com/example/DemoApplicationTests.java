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

import reactor.test.StepVerifier;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.DemoApplication.Person;
import com.example.DemoApplication.PersonRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Autowired PersonRepository repository;

	@Test
	public void contextLoads() {}

	@Test // save and find aria stark - could be tough
	public void saveAndFindAll() {

		// save aria and assert it has really been done
		StepVerifier.create(repository.save(new Person("Aria"))) //
				.expectNextCount(1) //
				.verifyComplete();

		// and load her afterwards
		StepVerifier.create(repository.findAllByName("Aria").take(1)) //
				.consumeNextWith(value -> Assert.assertTrue(value.getName().equals("Aria"))) //
				.verifyComplete();
	}

}
