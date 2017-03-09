/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.project.converter;

import com.redhat.ipaas.connector.catalog.ConnectorCatalog;
import com.redhat.ipaas.connector.catalog.ConnectorCatalogProperties;
import com.redhat.ipaas.model.connection.Action;
import com.redhat.ipaas.model.connection.Connection;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.model.integration.Step;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultIntegrationToProjectConverterTest {

    @Test
    public void testConvert() throws Exception {
        Map<String, byte[]> files = new DefaultIntegrationToProjectConverter(new ConnectorCatalog(new ConnectorCatalogProperties())).convert(
            new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .gitRepo("https://ourgithhost.somewhere/test.git")
                .steps(
                    Arrays.asList(
                        new Step.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties("{\"timerName\": \"every\"}").build()).configuredProperties("{\"period\": \"5000\"}").action(new Action.Builder().camelConnectorPrefix("periodic-timer").camelConnectorGAV("com.redhat.ipaas:timer-connector:0.2.1").build()).build(),
                        new Step.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties("{}").build()).configuredProperties("{\"httpUri\": \"http://localhost:8080/hello\"}").action(new Action.Builder().camelConnectorPrefix("http-get").camelConnectorGAV("com.redhat.ipaas:http-get-connector:0.2.1").build()).build(),
                        new Step.Builder().stepKind("log").configuredProperties("{\"message\": \"Hello World! ${body}\"}").build(),
                        new Step.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties("{}").build()).configuredProperties("{\"httpUri\": \"http://localhost:8080/bye\"}").action(new Action.Builder().camelConnectorPrefix("http-post").camelConnectorGAV("com.redhat.ipaas:http-post-connector:0.2.1").build()).build()
                    )
                )
                .build()
        );
        assertFileContents(files.get("README.md"), "test-README.md");
        assertFileContents(files.get("src/main/java/com/redhat/ipaas/example/Application.java"), "test-Application.java");
        assertFileContents(files.get("src/main/resources/application.yml"), "test-application.yml");
        assertFileContents(files.get("src/main/resources/funktion.yml"), "test-funktion.yml");
    }

    private void assertFileContents(byte[] actualContents, String expectedFileName) throws Exception {
        assertThat(new String(actualContents)).isEqualTo(
            new String(Files.readAllBytes(
                Paths.get(this.getClass().getResource(expectedFileName).toURI())
            ), StandardCharsets.UTF_8)
        );
    }

}
