package io.github.compilerstuck.control.render.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

/**
 * Owns HUD fonts (and later shared textures). Sync load on desktop; single dispose owner for font
 * natives.
 */
public final class AppAssets implements Disposable {
  public static final String FONT_PATH = "fonts/LiberationSans-Regular.ttf";
  public static final int[] FONT_SIZES = {16, 20, 28};

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

  /** Nearest cached size (same behavior as legacy {@code GdxRenderSystem.fontFor}). */
  public BitmapFont font(float sizePx) {
    int nearest = FONT_SIZES[0];
    float best = Math.abs(sizePx - nearest);
    for (int s : FONT_SIZES) {
      float d = Math.abs(sizePx - s);
      if (d < best) {
        best = d;
        nearest = s;
      }
    }
    BitmapFont font = fonts.get(nearest);
    if (font == null) {
      font = createFont(nearest);
      fonts.put(nearest, font);
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
