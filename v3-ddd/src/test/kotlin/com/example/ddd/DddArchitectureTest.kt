package com.example.ddd

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

/**
 * ArchUnit tests for the V3 DDD Architecture.
 *
 * Property 13: Isolamento do domain na arquitetura DDD (ArchUnit)
 * Validates: Requirements 8.1, 8.6, 8.7
 */
@AnalyzeClasses(packages = ["com.example.ddd"], importOptions = [ImportOption.DoNotIncludeTests::class])
class DddArchitectureTest {

    /**
     * Rule 1 (dominioNaoDependeDeFrameworks):
     * No class in `..domain..` may depend on `..infrastructure..`, `..application..`,
     * `org.springframework..`, or `jakarta.persistence..`
     */
    @ArchTest
    val dominioNaoDependeDeFrameworks: ArchRule =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..infrastructure..",
                "..application..",
                "org.springframework..",
                "jakarta.persistence.."
            )
            .`as`("Domain classes must not depend on infrastructure, application, Spring, or JPA")

    /**
     * Rule 2 (applicationNaoDependeDeInfrastructure):
     * No class in `..application..` may depend on `..infrastructure..`
     */
    @ArchTest
    val applicationNaoDependeDeInfrastructure: ArchRule =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .`as`("Application classes must not depend on infrastructure")

    /**
     * Rule 3 (repositoryImplsEstaoNaInfrastructure):
     * Classes with name `*RepositoryImpl` must reside in `..infrastructure.repositories..`
     */
    @ArchTest
    val repositoryImplsEstaoNaInfrastructure: ArchRule =
        classes().that().haveSimpleNameEndingWith("RepositoryImpl")
            .should().resideInAPackage("..infrastructure.repositories..")
            .`as`("RepositoryImpl classes must reside in infrastructure.repositories")

    /**
     * Rule 4 (entidadesDomainSemJpa):
     * No class in `..domain..` may be annotated with `@Entity`
     */
    @ArchTest
    val entidadesDomainSemJpa: ArchRule =
        noClasses().that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("jakarta.persistence.Entity")
            .`as`("Domain classes must not be annotated with @Entity")
}
