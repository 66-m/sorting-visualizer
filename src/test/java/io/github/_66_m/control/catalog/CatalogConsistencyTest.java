package io.github._66_m.control.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.github._66_m.control.model.ArrayController;
import io.github._66_m.sortingalgorithms.SortingAlgorithm;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** The catalogs are the single source of ids and names; keep them unique and complete. */
class CatalogConsistencyTest {

  @Test
  void algorithmIdsAndNamesAreUnique() {
    assertUnique(AlgorithmCatalog.all().stream().map(AlgorithmDescriptor::id).toList(), "ids");
    assertUnique(
        AlgorithmCatalog.all().stream().map(AlgorithmDescriptor::displayName).toList(), "names");
  }

  @Test
  void visualizationIdsAndNamesAreUnique() {
    assertUnique(
        VisualizationCatalog.all().stream().map(VisualizationDescriptor::id).toList(), "ids");
    assertUnique(
        VisualizationCatalog.all().stream().map(VisualizationDescriptor::displayName).toList(),
        "names");
  }

  @Test
  void everyAlgorithmClassIsRegisteredInTheCatalog() {
    Set<String> implemented =
        new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github._66_m.sortingalgorithms")
                .stream()
                .filter(c -> c.isAssignableTo(SortingAlgorithm.class))
                .filter(c -> !c.getModifiers().contains(JavaModifier.ABSTRACT))
                .filter(c -> c.getEnclosingClass().isEmpty())
                .map(JavaClass::getName)
                .collect(Collectors.toSet());

    Set<String> registered =
        AlgorithmCatalog.all().stream()
            .map(d -> d.factory().apply(new ArrayController(8), null).getClass().getName())
            .collect(Collectors.toSet());

    assertEquals(implemented, registered);
  }

  @Test
  void catalogStampsInstancesWithTheirDisplayName() {
    for (AlgorithmDescriptor d : AlgorithmCatalog.all()) {
      SortingAlgorithm algorithm = d.factory().apply(new ArrayController(8), null);
      assertEquals(d.displayName(), algorithm.getName(), d.id());
    }
  }

  @Test
  void unknownIdsAreReportedAndFallBackToTheFirstEntry() {
    assertTrue(AlgorithmCatalog.find("no-such-sort").isEmpty());
    assertEquals(AlgorithmCatalog.all().get(0), AlgorithmCatalog.findByIdOrDefault("no-such-sort"));
    assertFalse(VisualizationCatalog.find("no-such-viz").isPresent());
    assertEquals(
        VisualizationCatalog.all().get(0), VisualizationCatalog.findByIdOrDefault("no-such-viz"));
  }

  private static void assertUnique(List<String> values, String what) {
    Set<String> seen = new HashSet<>();
    for (String v : values) {
      assertTrue(seen.add(v), "duplicate catalog " + what + ": " + v);
    }
  }
}
