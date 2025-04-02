/*
 * Copyright 2025 the original author or authors.
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

package com.canonical.devpackspring.rewrite;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.gradle.GradleParser;
import org.openrewrite.groovy.GroovyParser;
import org.openrewrite.kotlin.KotlinParser;
import org.openrewrite.maven.MavenParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnableRockcraftRefactoring {

	private static final Logger logger = LoggerFactory.getLogger(EnableRockcraftRefactoring.class);

	public EnableRockcraftRefactoring() {
	}

	public boolean execute(Path baseDir) throws IOException {
		InMemoryExecutionContext context = new InMemoryExecutionContext(new Consumer<Throwable>() {
			@Override
			public void accept(Throwable throwable) {
				logger.error(throwable.getMessage(), throwable);
			}
		});

		List<SourceFile> files = parseGradle(baseDir, context);
		if (!files.isEmpty()) {
			boolean kotlinDsl = files.stream()
				.filter(x -> x.getSourcePath().toString().equals("build.gradle"))
				.findAny()
				.isEmpty();
			return RecipeUtil.applyRecipe(baseDir, new AddRockcraftGradleRecipe(kotlinDsl), files, context);
		}
		files = parseMaven(baseDir, context);
		return RecipeUtil.applyRecipe(baseDir, new AddRockcraftMavenRecipe(), files, context);
	}

	private static List<SourceFile> parseMaven(Path baseDir, InMemoryExecutionContext context) {
		Parser p = MavenParser.builder().build();
		List<Path> files = Arrays.stream(baseDir.toFile().listFiles(file -> "pom.xml".equals(file.getName())))
			.map(x -> x.toPath())
			.toList();

		return p.parse(files, baseDir, context).toList();
	}

	private static List<SourceFile> parseGradle(Path baseDir, InMemoryExecutionContext context) {
		Parser.Builder builder = GradleParser.builder()
			.groovyParser(GroovyParser.builder().logCompilationWarningsAndErrors(true))
			.kotlinParser(KotlinParser.builder().logCompilationWarningsAndErrors(true));

		Parser p = builder.build();
		final HashSet<String> gradleNames = new HashSet<>(Arrays.asList("build.gradle", "build.gradle.kts",
				"settings.gradle", "settings.gradle.kts", "init.gradle", "init.gradle.kts"));
		List<Path> files = Arrays.stream(baseDir.toFile().listFiles(file -> gradleNames.contains(file.getName())))
			.map(x -> x.toPath())
			.toList();

		return p.parse(files, baseDir, context).toList();
	}

}
