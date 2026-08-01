package io.github.compilerstuck.control.ui.settingsfx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RunAllOrderDialogDirtyTest {

  @Test
  void matchingOrderAndSelectionIsClean() {
    List<RunAllOrderDialog.DraftRow> rows =
        List.of(
            new RunAllOrderDialog.DraftRow("a", "A", true),
            new RunAllOrderDialog.DraftRow("b", "B", false));
    List<RunAllOrderDialog.BaselineRow> baseline =
        List.of(
            new RunAllOrderDialog.BaselineRow("a", true),
            new RunAllOrderDialog.BaselineRow("b", false));
    assertFalse(RunAllOrderDialog.isDirty(rows, baseline));
  }

  @Test
  void reorderedIdsAreDirty() {
    List<RunAllOrderDialog.DraftRow> rows =
        List.of(
            new RunAllOrderDialog.DraftRow("b", "B", false),
            new RunAllOrderDialog.DraftRow("a", "A", true));
    List<RunAllOrderDialog.BaselineRow> baseline =
        List.of(
            new RunAllOrderDialog.BaselineRow("a", true),
            new RunAllOrderDialog.BaselineRow("b", false));
    assertTrue(RunAllOrderDialog.isDirty(rows, baseline));
  }

  @Test
  void selectionChangeIsDirty() {
    List<RunAllOrderDialog.DraftRow> rows =
        List.of(
            new RunAllOrderDialog.DraftRow("a", "A", false),
            new RunAllOrderDialog.DraftRow("b", "B", false));
    List<RunAllOrderDialog.BaselineRow> baseline =
        List.of(
            new RunAllOrderDialog.BaselineRow("a", true),
            new RunAllOrderDialog.BaselineRow("b", false));
    assertTrue(RunAllOrderDialog.isDirty(rows, baseline));
  }
}
