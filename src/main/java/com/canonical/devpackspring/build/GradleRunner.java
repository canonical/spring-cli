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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;
import org.jline.utils.AttributedStyle;

import org.springframework.cli.util.TerminalMessage;

public abstract class GradleRunner {

	private static final String GRADLE_VERSION = "8.13";

	public static boolean run(Path baseDir, PluginDescriptor desc, String task, TerminalMessage message)
			throws IOException {
		DefaultGradleConnector connector = (DefaultGradleConnector) GradleConnector.newConnector();
		connector.daemonMaxIdleTime(1, TimeUnit.SECONDS);
		connector.setVerboseLogging(true);
		OutputStream terminalStream = new TerminalOutputStream(message, new AttributedStyle().foregroundDefault());
		OutputStream terminalStreamError = new TerminalOutputStream(message,
				new AttributedStyle().foreground(AttributedStyle.RED));

		if (task == null) {
			task = desc.defaultTask();
		}
		Path initScript = null;
		try {
			initScript = Files.createTempFile("init", ".gradle.kts");
			writeTemporaryInitFile(initScript, desc);

			if (Files.exists(baseDir.resolve("gradle/wrapper/gradle-wrapper.properties"))) {
				connector.useBuildDistribution();
			}
			else {
				connector.useGradleVersion(GRADLE_VERSION);
			}
			try (ProjectConnection connection = connector.forProjectDirectory(baseDir.toFile()).connect()) {
				BuildLauncher buildLauncher = connection.newBuild()
					.setStandardOutput(terminalStream)
					.setStandardError(terminalStreamError)
					.addArguments("--init-script", initScript.toString(), task)
					.forTasks(task);
				buildLauncher.run();
				connection.close();
				return true;
			}
		}
		catch (RuntimeException ex) {
			try (PrintStream ps = new PrintStream(terminalStreamError)) {
				// skip runtime exception itself - it only prints
				// that task execution failed
				ps.print(ex.getCause().getMessage());
			}
			return false;
		}
		finally {
			connector.disconnect();
			if (initScript != null) {
				Files.deleteIfExists(initScript);
			}
		}
	}

	private static void writeTemporaryInitFile(Path file, PluginDescriptor desc) throws IOException {
		StringBuilder template = new StringBuilder();

		if (desc.repository() != null) {
			StringBuilder dependencies = new StringBuilder();
			readTemplate(dependencies, "/com/canonical/devpackspring/apply-plugin-deps.gradle.kts");
			template.append(String.format(dependencies.toString(), desc.repository(), desc.classpath()));
		}

		readTemplate(template, "/com/canonical/devpackspring/apply-plugin.gradle.kts");
		Files.writeString(file, String.format(template.toString(), desc.id(), desc.className(), desc.id(),
				(desc.defaultConfiguration() != null) ? desc.defaultConfiguration() : ""));
	}

	private static void readTemplate(StringBuilder template, String source) throws IOException {
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(GradleRunner.class.getResourceAsStream(source)))) {
			String line;
			while ((line = r.readLine()) != null) {
				template.append(line);
				template.append(System.lineSeparator());
			}
		}
	}

}
