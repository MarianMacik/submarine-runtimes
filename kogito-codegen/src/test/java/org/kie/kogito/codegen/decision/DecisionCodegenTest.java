/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.decision;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.GeneratorContext;
import org.kie.kogito.grafana.JGrafana;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DecisionCodegenTest {

    @Test
    public void generateAllFiles() throws Exception {

        GeneratorContext context = stronglyTypedContext();

        DecisionCodegen codeGenerator = DecisionCodegen.ofPath(Paths.get("src/test/resources/decision/models/vacationDays").toAbsolutePath());
        codeGenerator.setContext(context);

        List<GeneratedFile> generatedFiles = codeGenerator.generate();
        assertEquals(5, generatedFiles.size());

        assertIterableEquals(Arrays.asList(
                "decision/InputSet.java",
                "decision/TEmployee.java",
                "decision/TAddress.java",
                "decision/TPayroll.java",
                "decision/VacationsResource.java"
                             ),
                             fileNames(generatedFiles)
        );

        ClassOrInterfaceDeclaration classDeclaration = codeGenerator.moduleGenerator().classDeclaration();
        assertNotNull(classDeclaration);
    }

    private GeneratorContext stronglyTypedContext() {
        Properties properties = new Properties();
        properties.put(DecisionCodegen.STRONGLY_TYPED_CONFIGURATION_KEY, Boolean.TRUE.toString());
        return GeneratorContext.ofProperties(properties);
    }

    private List<String> fileNames(List<GeneratedFile> generatedFiles) {
        return generatedFiles.stream().map(GeneratedFile::relativePath).collect(Collectors.toList());
    }

    @Test
    public void doNotGenerateTypesafeInfo() throws Exception {
        GeneratorContext context = stronglyTypedContext();

        DecisionCodegen codeGenerator = DecisionCodegen.ofPath(Paths.get("src/test/resources/decision/alltypes/").toAbsolutePath());
        codeGenerator.setContext(context);

        List<GeneratedFile> generatedFiles = codeGenerator.generate();
        assertEquals(2, generatedFiles.size());

        assertIterableEquals(Arrays.asList("http_58_47_47www_46trisotech_46com_47definitions_47__4f5608e9_454d74_454c22_45a47e_45ab657257fc9c/InputSet.java",
                                           "http_58_47_47www_46trisotech_46com_47definitions_47__4f5608e9_454d74_454c22_45a47e_45ab657257fc9c/OneOfEachTypeResource.java"),
                             fileNames(generatedFiles)
        );

        ClassOrInterfaceDeclaration classDeclaration = codeGenerator.moduleGenerator().classDeclaration();
        assertNotNull(classDeclaration);
    }

    @Test
    public void GivenADMNModel_WhenMonitoringIsActive_ThenGrafanaDashboardsAreGenerated() throws Exception {
        DecisionCodegen codeGenerator = DecisionCodegen.ofPath(Paths.get("src/test/resources/decision/models/vacationDays").toAbsolutePath()).withMonitoring(true);

        List<GeneratedFile> generatedFiles = codeGenerator.generate();

        List<GeneratedFile> dashboards = generatedFiles.stream().filter(x -> x.getType() == GeneratedFile.Type.RESOURCE).collect(Collectors.toList());

        assertEquals(2, dashboards.size());

        JGrafana vacationOperationalDashboard = JGrafana.parse(new String(dashboards.stream().filter(x -> x.relativePath().contains("operational-dashboard-Vacations.json")).findFirst().get().contents()));

        assertEquals(6, vacationOperationalDashboard.getDashboard().panels.size());

        JGrafana vacationDomainDashboard = JGrafana.parse(new String(dashboards.stream().filter(x -> x.relativePath().contains("domain-dashboard-Vacations.json")).findFirst().get().contents()));

        assertEquals(1, vacationDomainDashboard.getDashboard().panels.size());
    }

    @Test
    public void resilientToDuplicateDMNIDs() throws Exception {
        DecisionCodegen codeGenerator = DecisionCodegen.ofPath(Paths.get("src/test/resources/decision-test20200507").toAbsolutePath());

        List<GeneratedFile> generatedFiles = codeGenerator.generate();
        assertEquals(2, generatedFiles.size());

        ClassOrInterfaceDeclaration classDeclaration = codeGenerator.moduleGenerator().classDeclaration();
        assertNotNull(classDeclaration);
    }

    @Test
    public void emptyName() throws Exception {
        DecisionCodegen codeGenerator = DecisionCodegen.ofPath(Paths.get("src/test/resources/decision-empty-name").toAbsolutePath());
        RuntimeException re = Assertions.assertThrows(RuntimeException.class, () -> {
            codeGenerator.generate();
        });
        assertEquals("Model name should not be empty", re.getMessage());
    }
}
