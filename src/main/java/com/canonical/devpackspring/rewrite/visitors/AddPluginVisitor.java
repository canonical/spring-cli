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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;

public class AddPluginVisitor {

	public static final String HAS_PLUGIN_BLOCK = "has_plugin_block";

	public static final String METHOD_ID = "id";

	public static final String METHOD_PLUGINS = "plugins";

	public List<Statement> statements;

	private final Set<String> appliedPlugins = new HashSet<>();

	private final String pluginName;

	public AddPluginVisitor(String pluginName, List<Statement> statements) {
		this.pluginName = pluginName;
		this.statements = statements;
	}

	public @Nullable J postVisit(@NonNull J tree, ExecutionContext executionContext,
			BiFunction<J, ExecutionContext, J> action) {
		if (tree instanceof J.MethodInvocation) {
			J.MethodInvocation call = (J.MethodInvocation) tree;
			if (METHOD_PLUGINS.equals(call.getSimpleName())) {
				executionContext.putMessage(HAS_PLUGIN_BLOCK, true);
				new JavaIsoVisitor<ExecutionContext>() {
					@Override
					public @NotNull J.MethodInvocation visitMethodInvocation(J.MethodInvocation method,
							ExecutionContext executionContext) {
						if (METHOD_ID.equals(method.getSimpleName())) {
							Expression pluginName = method.getArguments().get(0);
							appliedPlugins.add(pluginName.toString());
						}
						return method;
					}
				}.visitMethodInvocation(call, executionContext);
				if (appliedPlugins.contains(pluginName)) {
					return tree;
				}
				return addPluginCall(executionContext, call);
			}
		}
		return action.apply(tree, executionContext);
	}

	private J.@NotNull MethodInvocation addPluginCall(ExecutionContext executionContext, J.MethodInvocation call) {
		J.Lambda lambda = (J.Lambda) call.getArguments().get(0);
		J.Block block = (J.Block) lambda.getBody();

		final Space prefix = block.getStatements().isEmpty() ? Space.format("\n\t")
				: block.getStatements().get(0).getPrefix();
		List<Statement> newStatements = new ArrayList<>();
		newStatements.addAll(block.getStatements());
		newStatements
			.addAll(Arrays.asList(statements.stream().map(x -> x.withPrefix(prefix)).toArray(Statement[]::new)));

		block = block.withStatements(newStatements);
		lambda = lambda.withBody(block);
		return call.withArguments(List.of(lambda));
	}

}
