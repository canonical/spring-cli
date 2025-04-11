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
import java.nio.file.Path;

import com.canonical.devpackspring.rewrite.EnableRockcraftRefactoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cli.util.IoUtils;
import org.springframework.cli.util.TerminalMessage;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

public class RockcraftCommands {

	private final TerminalMessage terminalMessage;

	private final Path workingDir;

	private final EnableRockcraftRefactoring rockcraftService = new EnableRockcraftRefactoring();

	@Autowired
	public RockcraftCommands(TerminalMessage terminalMessage) {
		this.terminalMessage = terminalMessage;
		this.workingDir = IoUtils.getWorkingDirectory();
	}

	public RockcraftCommands(TerminalMessage terminalMessage, Path workingDir) {
		this.terminalMessage = terminalMessage;
		this.workingDir = workingDir;
	}

	@Command(command = "add", description = "Add rockcraft export plugin for the project")
	public String addRockcraft(@Option(description = "Project path") String path) throws IOException {
		Path where = (path != null) ? Path.of(path) : workingDir;
		if (rockcraftService.execute(where)) {
			return "Added rockcraft plugin to " + path;
		}
		return "Project unchanged.";
	}

}
