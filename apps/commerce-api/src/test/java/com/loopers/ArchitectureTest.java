package com.loopers;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.loopers", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // 1. 계층형 아키텍처 의존성 검증
    // Interfaces → Application → Domain ← Infrastructure
    // Config, Support 패키지는 모든 레이어에서 참조 가능
    @ArchTest
    static final ArchRule layered_architecture_is_respected = layeredArchitecture()
        .consideringOnlyDependenciesInAnyPackage("com.loopers..")
        .layer("Interfaces").definedBy("..interfaces..")
        .layer("Application").definedBy("..application..")
        .layer("Domain").definedBy("..domain..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .layer("Config").definedBy("..config..")
        .layer("Support").definedBy("..support..")
        .whereLayer("Interfaces").mayNotBeAccessedByAnyLayer()
        .whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Interfaces", "Config")
        .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
        .whereLayer("Config").mayNotBeAccessedByAnyLayer()
        .whereLayer("Support").mayOnlyBeAccessedByLayers("Interfaces", "Application", "Domain", "Infrastructure", "Config");

    // 2. Domain 계층 독립성 (DIP 핵심)
    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
}
