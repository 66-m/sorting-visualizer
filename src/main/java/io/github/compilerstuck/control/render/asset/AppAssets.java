package io.github.compilerstuck.control.render.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import io.github.compilerstuck.control.config.AppConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Owns HUD fonts (and later shared textures). Sync load on desktop; single dispose owner for font
 * natives.
 */
public final class AppAssets implements Disposable {
  public static final String FONT_PATH = "fonts/LiberationSans-Regular.ttf";
  private static final int[] FONT_SIZES = {16, 20, 28};

  private final FreeTypeFontGenerator fontGenerator;
  private final Map<Integer, BitmapFont> fonts = new HashMap<>();

  public AppAssets() {
    fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
  }

  /** Generates the standard Overlay-flipped font sizes. */
  public void loadDefaults() {
    for (int size : FONT_SIZES) {
      if (!fonts.containsKey(size)) {
        fonts.put(size, createFont(size));
      }
    }
  }

  /**
   * Returns a FreeType font at the exact requested pixel size (rounded, clamped to {@code
   * 1..AppConfig.MAX_TEXT_SIZE}). Generated fonts are cached by size.
   */
  public BitmapFont font(float sizePx) {
    int size = Math.max(1, Math.min(AppConfig.MAX_TEXT_SIZE, Math.round(sizePx)));
    BitmapFont font = fonts.get(size);
    if (font == null) {
      font = createFont(size);
      fonts.put(size, font);
    }
    return font;
  }

  private BitmapFont createFont(int sizePx) {
    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = sizePx;
    parameter.flip = true;
    parameter.minFilter = Texture.TextureFilter.Linear;
    parameter.magFilter = Texture.TextureFilter.Linear;
    BitmapFont font = fontGenerator.generateFont(parameter);
    font.setUseIntegerPositions(true);
    return font;
  }

  @Override
  public void dispose() {
    for (BitmapFont f : fonts.values()) {
      f.dispose();
    }
    fonts.clear();
    fontGenerator.dispose();
  }
}
