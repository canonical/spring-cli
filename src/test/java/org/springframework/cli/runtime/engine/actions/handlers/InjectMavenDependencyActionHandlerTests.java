/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cli.runtime.engine.actions.handlers;

import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cli.support.CommandRunner;
import org.springframework.cli.support.MockConfigurations.MockBaseConfig;
import org.springframework.cli.support.MockConfigurations.MockUserConfig;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("maven-dependency")
class InjectMavenDependencyActionHandlerTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(MockBaseConfig.class);

	@Test
	void injectMavenDependency(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path workingDir) {
		this.contextRunner.withUserConfiguration(MockUserConfig.class).run((context) -> {

			CommandRunner commandRunner = new CommandRunner.Builder(context).prepareProject("rest-service", workingDir)
				.installCommandGroup("inject-maven")
				.executeCommand("dependency/add")
				.build();
			commandRunner.run();

			Path pomPath = workingDir.resolve("pom.xml");
			verifyMavenArtifactId(pomPath);

		});
	}

	@Test
	void injectMavenDependencyUsingVariables(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path workingDir) {
		this.contextRunner.withUserConfiguration(MockUserConfig.class).run((context) -> {

			CommandRunner commandRunner = new CommandRunner.Builder(context).prepareProject("rest-service", workingDir)
				.installCommandGroup("inject-maven")
				.executeCommand("dependency/add-using-var")
				.withArguments("runtime-scope", "runtime")
				.build();
			commandRunner.run();

			Path pomPath = workingDir.resolve("pom.xml");
			verifyMavenArtifactId(pomPath);
			assertThat(pomPath).content().contains("<scope>runtime</scope>");
		});
	}

	private static void verifyMavenArtifactId(Path pomPath) {
		assertThat(pomPath).content().contains("spring-boot-starter-data-jpa");
		assertThat(pomPath).content().contains("spring-boot-starter-test");
		assertThat(pomPath).content().contains("com.h2database");
	}

}
