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

package com.canonical.devpackspring.rewrite.visitors;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.canonical.devpackspring.rewrite.StatementUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.gradle.GradleParser;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.kotlin.KotlinIsoVisitor;
import org.openrewrite.kotlin.KotlinParser;
import org.openrewrite.kotlin.tree.K;

public class KotlinAddPluginVisitor extends KotlinIsoVisitor<ExecutionContext> {

	private final String pluginTemplateKotlin = "plugins {\n\tid(\"%s\") version \"%s\"\n}\n";

	private final AddPluginVisitor visitor;

	private final SourceFile templateSource;

	public KotlinAddPluginVisitor(String pluginName, String pluginVersion) {
		Parser.Builder builder = GradleParser.builder()
			.kotlinParser(KotlinParser.builder().logCompilationWarningsAndErrors(true));
		Parser parser = builder.build();
		InMemoryExecutionContext context = new InMemoryExecutionContext();

		templateSource = parser
			.parseInputs(
					Arrays.asList(Parser.Input.fromString(Paths.get("/tmp/build.gradle.kts"),
							String.format(pluginTemplateKotlin, pluginName, pluginVersion))),
					Paths.get("/tmp"), context)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Could not parse as Gradle"));

		List<Statement> statements = ((K.CompilationUnit) templateSource).getStatements();
		J.Block block = (J.Block) statements.get(0);
		K.MethodInvocation stm = (K.MethodInvocation) block.getStatements().get(0);
		K.Lambda lambda = (K.Lambda) stm.getArguments().get(0);
		K.Block kBlock = (K.Block) lambda.getBody();
		visitor = new AddPluginVisitor(pluginName, kBlock.getStatements());
	}

	@Override
	public @Nullable J postVisit(@NonNull J tree, ExecutionContext executionContext) {
		return visitor.postVisit(tree, executionContext, (t, context) -> {
			if (Boolean.TRUE.equals(context.getMessage(AddPluginVisitor.HAS_PLUGIN_BLOCK))) {
				return tree;
			}

			if (tree instanceof K.CompilationUnit unit) {
				if (!unit.getSourcePath().toString().equals("build.gradle.kts")) {
					return tree;
				}
				List<Statement> statements = StatementUtil.append(unit.getStatements(),
						((K.CompilationUnit) templateSource).getStatements());
				return unit.withStatements(statements);
			}
			return t;
		});
	}

}
