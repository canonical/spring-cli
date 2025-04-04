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

import org.junit.jupiter.api.Test;

import org.springframework.cli.util.StubTerminalMessage;
import org.springframework.shell.table.Table;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildCommandTests {

	@Test
	public void testListPlugins() throws IOException {
		StubTerminalMessage terminalMessage = new StubTerminalMessage();
		BuildCommands commands = new BuildCommands(terminalMessage, null);
		Table ret = commands.list();
		String plugins = ret.render(80);
		assertThat(plugins).contains("rockcraft");
		assertThat(plugins).contains("io.github.rockcrafters.rockcraft");
	}

}
