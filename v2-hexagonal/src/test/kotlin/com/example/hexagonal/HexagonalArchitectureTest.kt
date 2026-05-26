package com.example.hexagonal

import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

/**
 * ArchUnit tests for the V2 Hexagonal Architecture.
 *
 * Validates Requirements 7.1, 7.4, 7.6
 *
 * **Validates: Requirements 7.1, 7.4, 7.6**
 */
@AnalyzeClasses(packages = ["com.example.hexagonal"])
class HexagonalArchitectureTest {

    /**
     * Rule 1 — Domain isolation:
     * No class in `..domain..` may depend on `..application..`, `..infrastructure..`,
     * `org.springframework..`, or `jakarta.persistence..`
     */
    @ArchTest
    val domainShouldNotDependOnApplicationOrInfrastructure: ArchRule =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..application..",
                "..infrastructure..",
                "org.springframework..",
                "jakarta.persistence.."
            )
            .`as`("Domain classes must not depend on application, infrastructure, Spring, or JPA")

    /**
     * Rule 2 — Application isolation:
     * No class in `..application..` may depend on `..infrastructure..`
     */
    @ArchTest
    val applicationShouldNotDependOnInfrastructure: ArchRule =
        noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .`as`("Application classes must not depend on infrastructure")

    /**
     * Rule 3 — Adapter implements port:
     * Classes in `..infrastructure.repositories..` whose name ends with `Adapter`
     * must implement an interface from `..application.ports..`
     */
    @ArchTest
    val adaptersShouldImplementPorts: ArchRule =
        classes().that().resideInAPackage("..infrastructure.repositories..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().implement(resideInAPackage("..application.ports.."))
            .`as`("Adapter classes in infrastructure.repositories must implement a port from application.ports")

    /**
     * Rule 4 — Controllers don't access repositories directly:
     * Controllers in `..infrastructure.web.controllers..` must not access
     * `..infrastructure.repositories..` directly
     */
    @ArchTest
    val controllersShouldNotAccessRepositoriesDirectly: ArchRule =
        noClasses().that().resideInAPackage("..infrastructure.web.controllers..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure.repositories..")
            .`as`("Controllers must not access infrastructure repositories directly")
}
