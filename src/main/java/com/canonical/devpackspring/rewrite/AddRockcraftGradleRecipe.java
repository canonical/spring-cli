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

import com.canonical.devpackspring.rewrite.visitors.GroovyAddPluginVisitor;
import com.canonical.devpackspring.rewrite.visitors.KotlinAddPluginVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class AddRockcraftGradleRecipe extends Recipe {

	private final String PLUGIN_ID = "io.github.rockcrafters.rockcraft";

	private final String PLUGIN_VERSION = "1.0.0";

	private final TreeVisitor<?, ExecutionContext> visitor;

	public AddRockcraftGradleRecipe(boolean kotlin) {
		if (kotlin) {
			visitor = new KotlinAddPluginVisitor(PLUGIN_ID, PLUGIN_VERSION);
		}
		else {
			visitor = new GroovyAddPluginVisitor(PLUGIN_ID, PLUGIN_VERSION);
		}
	}

	@Override
	public @NlsRewrite.DisplayName String getDisplayName() {
		return "Add rockcraft support";
	}

	@Override
	public @NlsRewrite.Description String getDescription() {
		return "Add rockcraft plugin support to the project";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return visitor;
	}

}
