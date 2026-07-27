package io.github.compilerstuck;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Architecture rules enforcing separation between core algorithm/model logic and UI/rendering
 * frameworks (AWT, Processing legacy, JavaFX, libGDX).
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
  static final ArchRule algorithms_no_gdx =
      noClasses()
          .that()
          .resideInAPackage("..sortingalgorithms..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.badlogic.gdx..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule algorithms_no_composition_root =
      noClasses()
          .that()
          .resideInAPackage("..sortingalgorithms..")
          .should()
          .dependOnClassesThat()
          .haveSimpleName("SortingVisualizerGame")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule model_no_swing =
      noClasses()
          .that()
          .resideInAPackage("..control.model..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("javax.swing..", "processing..", "com.badlogic.gdx..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule shuffle_no_processing =
      noClasses()
          .that()
          .resideInAPackage("..control.shuffle..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("processing..", "javax.swing..", "com.badlogic.gdx..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule algorithms_no_javafx =
      noClasses()
          .that()
          .resideInAPackage("..sortingalgorithms..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("javafx..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule model_no_javafx =
      noClasses()
          .that()
          .resideInAPackage("..control.model..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("javafx..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule shuffle_no_javafx =
      noClasses()
          .that()
          .resideInAPackage("..control.shuffle..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("javafx..")
          .allowEmptyShould(true);

  /** Phase 2 view-models must stay headless (G9). Package reserved in Phase 1. */
  @ArchTest
  static final ArchRule settingsfx_vm_no_javafx =
      noClasses()
          .that()
          .resideInAPackage("..control.ui.settingsfx.vm..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("javafx..", "com.badlogic.gdx..")
          .allowEmptyShould(true);

  /** Phase 5: visuals must not own Pixmap / Gdx.files I/O. */
  @ArchTest
  static final ArchRule visual_no_pixmap =
      noClasses()
          .that()
          .resideInAPackage("..visual..")
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("com.badlogic.gdx.graphics.Pixmap")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule visual_no_gdx_files =
      noClasses()
          .that()
          .resideInAPackage("..visual..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.badlogic.gdx.files..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule visual_no_gdx_app =
      noClasses()
          .that()
          .resideInAPackage("..visual..")
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("com.badlogic.gdx.Gdx")
          .allowEmptyShould(true);
}
