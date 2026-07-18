package io.github.compilerstuck;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Architecture rules enforcing separation between core algorithm/model logic and UI/rendering
 * frameworks (Swing, AWT, Processing).
 */
@AnalyzeClasses(packages = "io.github.compilerstuck")
class ArchitectureTest {

  @ArchTest
  static final ArchRule algorithms_no_swing =
      noClasses()
          .that()
          .resideInAPackage("..sortingalgorithms..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("javax.swing..", "java.awt..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule algorithms_no_processing =
      noClasses()
          .that()
          .resideInAPackage("..sortingalgorithms..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("processing..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule algorithms_no_maincontroller =
      noClasses()
          .that()
          .resideInAPackage("..sortingalgorithms..")
          .should()
          .dependOnClassesThat()
          .haveSimpleName("MainController")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule model_no_swing =
      noClasses()
          .that()
          .resideInAPackage("..control.model..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("javax.swing..", "processing..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule shuffle_no_processing =
      noClasses()
          .that()
          .resideInAPackage("..control.shuffle..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("processing..", "javax.swing..")
          .allowEmptyShould(true);
}
