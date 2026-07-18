package io.github.compilerstuck;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture rules enforcing separation between core algorithm/model logic
 * and UI/rendering frameworks (Swing, AWT, Processing).
 */
@AnalyzeClasses(packages = "io.github.compilerstuck")
class ArchitectureTest {

    @ArchTest
    static final ArchRule algorithms_no_swing =
            noClasses().that().resideInAPackage("..SortingAlgorithms..")
                    .should().dependOnClassesThat().resideInAnyPackage("javax.swing..", "java.awt..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule algorithms_no_processing =
            noClasses().that().resideInAPackage("..SortingAlgorithms..")
                    .should().dependOnClassesThat().resideInAPackage("processing..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule algorithms_no_maincontroller =
            noClasses().that().resideInAPackage("..SortingAlgorithms..")
                    .should().dependOnClassesThat().haveSimpleName("MainController")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule model_no_swing =
            noClasses().that().resideInAPackage("..Control.model..")
                    .should().dependOnClassesThat().resideInAnyPackage("javax.swing..", "processing..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule shuffle_no_processing =
            noClasses().that().resideInAPackage("..Control.shuffle..")
                    .should().dependOnClassesThat().resideInAnyPackage("processing..", "javax.swing..")
                    .allowEmptyShould(true);
}
