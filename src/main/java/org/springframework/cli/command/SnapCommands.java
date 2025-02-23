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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

import com.canonical.devpackspring.snap.Manifest;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

/**
 * This command is responsible for the manipulation of the content snaps
 */
@Command(command = "snap", group = "Devpack")
public class SnapCommands {

	@Command(command = "list", description = "List available content snaps.")
	public Table list() {
		try {
			var header = Stream.<String[]>of(new String[] { "Installed", "Name", "Channel", "Version", "Description" });
			var manifest = new Manifest();
			var snaps = manifest.load(loadManifest());
			var rows = snaps.stream()
				.sorted((x, y) -> x.name().compareTo(y.name()))
				.map(x -> new String[] { x.installed() ? "✓" : " ", x.name(), x.channel(), x.version(), x.summary() });
			String[][] data = Stream.concat(header, rows).toArray(String[][]::new);
			TableModel model = new ArrayTableModel(data);
			TableBuilder tableBuilder = new TableBuilder(model);
			return tableBuilder.addFullBorder(BorderStyle.fancy_light).build();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage());
		}

	}

	private static String loadManifest() throws IOException {
		var ret = new StringBuilder();
		try (BufferedReader r = new BufferedReader(
				new FileReader("/snap/devpack-for-spring-manifest/current/supported.yaml"))) {
			String line;
			while ((line = r.readLine()) != null) {
				ret.append(line);
				ret.append("\n");
			}
		}
		return ret.toString();
	}

}
