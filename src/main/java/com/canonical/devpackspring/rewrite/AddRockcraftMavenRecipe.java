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

import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.RecipeList;
import org.openrewrite.maven.AddPlugin;

public class AddRockcraftMavenRecipe extends Recipe {

	@Override
	public @NlsRewrite.DisplayName String getDisplayName() {
		return "Add rockcraft maven plugin";
	}

	@Override
	public @NlsRewrite.Description String getDescription() {
		return "Adds rockcraft maven plugin";
	}

	@Override
	public void buildRecipeList(RecipeList recipes) {
		super.buildRecipeList(recipes);
		// CHECKSTYLE.OFF: SpringLeadingWhitespace - maven executions snippets format
		recipes.recipe(new AddPlugin("io.github.rockcrafters", "rockcraft-maven-plugin", "1.0.0", null, null, """
					<executions>
						<execution>
							<goals>
								<!-- creates rockcraft.yaml -->
								<goal>create-rock</goal>
								<!-- builds rock image -->
								<goal>build-rock</goal>
								<!-- pushes rock to the local docker daemon-->
								<goal>push-rock</goal>
							</goals>
						</execution>
					</executions>
				""", null));
		// CHECKSTYLE.ON: SpringLeadingWhitespace
	}

}
