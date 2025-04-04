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

package org.springframework.cli.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.canonical.devpackspring.build.BuildSystem;
import com.canonical.devpackspring.build.PluginDescriptorContainer;
import org.jetbrains.annotations.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cli.util.IoUtils;
import org.springframework.cli.util.TerminalMessage;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

@Command(command = "build", group = "Build Plugins")
public class BuildCommands {

	private final Path workingDir;

	private final TerminalMessage terminalMessage;

	private final ComponentFlow.Builder componentFlowBuilder;

	private PluginDescriptorContainer container;

	@Autowired
	public BuildCommands(TerminalMessage terminalMessage, ComponentFlow.Builder componentFlowBuilder)
			throws IOException {
		this.terminalMessage = terminalMessage;
		this.componentFlowBuilder = componentFlowBuilder;
		this.workingDir = IoUtils.getWorkingDirectory();
		this.container = null;
		try (InputStream stream = getClass()
			.getResourceAsStream("/com/canonical/devpackspring/plugin-configuration.yaml")) {
			this.container = new PluginDescriptorContainer(new InputStreamReader(stream));
		}
	}

	@Command(command = "list", description = "List build plugins supported by the snap")
	public Table list() {
		// TODO: check build system and print applicable plugins
		var header = Stream.<String[]>of(new String[] { "Name", "Id", "Description", "Build System" });
		var gradlePlugins = getPlugins(BuildSystem.gradle);
		var mavenPlugins = getPlugins(BuildSystem.maven);
		var data = Stream.concat(Stream.concat(header, gradlePlugins), mavenPlugins).toArray(String[][]::new);
		TableModel model = new ArrayTableModel(data);
		TableBuilder tableBuilder = new TableBuilder(model);
		return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
	}

	private @NotNull Stream<String[]> getPlugins(BuildSystem buildSystem) {
		var rows = container.plugins(buildSystem).stream().map(x -> {
			var desc = container.get(x, buildSystem);
			return new String[] { x, desc.id(), desc.description(), buildSystem.name() };
		});
		return rows;
	}

}
