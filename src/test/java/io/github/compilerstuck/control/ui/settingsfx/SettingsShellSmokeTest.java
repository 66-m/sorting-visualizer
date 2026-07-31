package io.github.compilerstuck.control.ui.settingsfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import atlantafx.base.theme.PrimerLight;
import io.github.compilerstuck.control.ui.settingsfx.vm.AlgorithmViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.AppContextTestFixture;
import io.github.compilerstuck.control.ui.settingsfx.vm.AppearanceViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.ArraySizeViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.DebugViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.DisplayViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.SoundViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.SpeedViewModel;
import io.github.compilerstuck.control.ui.settingsfx.vm.VisualizationViewModel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Headless smoke: SettingsShell with real section nodes under TestFX + Monocle (G18). No Processing
 * / NEWT bootstrap.
 */
class SettingsShellSmokeTest extends ApplicationTest {

  @Override
  public void start(Stage stage) {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    ShellResult shell = buildWiredShell();
    Scene scene = new Scene(shell.root(), 720, 560);
    var css = SettingsFxController.class.getResource("/css/settings-app.css");
    if (css != null) {
      scene.getStylesheets().add(css.toExternalForm());
    }
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void shellShowsTitleRunButtonAndSections() {
    Label title = lookup("#" + SettingsShell.TITLE_ID).queryAs(Label.class);
    assertNotNull(title);
    assertEquals(SettingsStrings.TITLE, title.getText());
    assertTrue(title.isVisible());

    Button run = lookup("#" + SettingsShell.RUN_BUTTON_ID).queryAs(Button.class);
    assertNotNull(run);
    assertEquals(SettingsStrings.RUN, run.getText());
    assertTrue(run.isVisible());

    Button cancel = lookup("#" + SettingsShell.CANCEL_BUTTON_ID).queryAs(Button.class);
    assertNotNull(cancel);
    assertEquals(SettingsStrings.CANCEL, cancel.getText());

    assertNotNull(lookup("#" + SoundSection.ROOT_ID).query());
    assertNotNull(lookup("#" + SpeedSection.ROOT_ID).query());
    assertNotNull(lookup("#" + DisplaySection.ROOT_ID).query());
    assertNotNull(lookup("#" + DebugSection.ROOT_ID).query());
    assertNotNull(lookup("#" + ArraySizeSection.ROOT_ID).query());
    assertNotNull(lookup("#" + AppearanceSection.ROOT_ID).query());
    assertNotNull(lookup("#" + VisualizationSection.ROOT_ID).query());
    assertNotNull(lookup("#" + SortingSection.ROOT_ID).query());
  }

  @Test
  void shellBuildIsCallableOffStageForUnitInspection() throws Exception {
    AtomicReference<ShellResult> root = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          root.set(SettingsShell.build());
          latch.countDown();
        });
    assertTrue(latch.await(10, TimeUnit.SECONDS));
    assertNotNull(root.get());
    assertNotNull(root.get().root().lookup("#" + SettingsShell.TITLE_ID));
  }

  @Test
  void wiredShellPreferredHeightFitsCollapsedOnePager() {
    var root = lookup(".root").queryParent();
    // Scene in start() is 720×560; form pref height should sit near that band, not the old 780.
    double pref = root.prefHeight(960);
    assertTrue(pref >= 420 && pref <= 700, "unexpected prefHeight=" + pref);
  }

  private static ShellResult buildWiredShell() {
    AppContextTestFixture fx = new AppContextTestFixture();
    VisualizationViewModel vizVm = new VisualizationViewModel(fx.app);
    ArraySizeViewModel arrayVm = new ArraySizeViewModel(fx.app, vizVm::currentConstraints);
    vizVm.setSizeDisplaySync(arrayVm::syncDisplayedSize);
    return SettingsShell.build(
        new SectionNodes(
            ArraySizeSection.build(arrayVm),
            SortingSection.build(new AlgorithmViewModel(fx.app)),
            SpeedSection.build(new SpeedViewModel(fx.app), new DisplayViewModel(fx.app)),
            VisualizationSection.build(vizVm),
            AppearanceSection.build(new AppearanceViewModel(fx.app)),
            OptionsSection.build(
                new DisplayViewModel(fx.app),
                new SoundViewModel(fx.app),
                new DebugViewModel(fx.app))));
  }
}
