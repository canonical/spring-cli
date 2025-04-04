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

package com.canonical.devpackspring.build;

import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginDescriptorContainerTests {

	@Test
	public void testLoadContainer() {
		PluginDescriptorContainer container = new PluginDescriptorContainer(new InputStreamReader(
				getClass().getResourceAsStream("/com/canonical/devpackspring/build/test-plugin.yaml")));
		assertThat(container.plugins(BuildSystem.gradle)).contains("checkStyle", "rockcraft");
		PluginDescriptor description = container.get("rockcraft", BuildSystem.gradle);
		// validate description field mapping
		assertThat(description.id()).isEqualTo("io.github.rockcrafters.rockcraft");
		assertThat(description.version()).isEqualTo("1.0.0");
		assertThat(description.classpath())
			.isEqualTo("io.github.rockcrafters.rockcraft:io.github.rockcrafters.rockcraft.gradle.plugin:1.0.0");
		assertThat(description.className()).isEqualTo("com.canonical.rockcraft.gradle.RockcraftPlugin");
		assertThat(description.repository()).isEqualTo("gradlePluginPortal()");
		assertThat(description.defaultTask()).isEqualTo("build-rock");
		assertThat(description.tasks()).contains("create-rock", "build-rock", "create-build-rock");
		assertThat(description.defaultConfiguration()).contains("rockcraft");
		assertThat(description.description()).contains("plugin description");
	}

}
