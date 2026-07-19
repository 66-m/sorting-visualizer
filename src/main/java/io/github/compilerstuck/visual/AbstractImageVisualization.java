package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.control.render.asset.ImageHandle;
import io.github.compilerstuck.control.render.asset.ImageRepository;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

/**
 * Shared Overlay image strip remap (Horizontal / Vertical). GDX-free: loads via {@link
 * ImageRepository}; remaps only when visual revision / layout / handle change.
 */
abstract class AbstractImageVisualization extends Visualization
    implements ImageSourceVisualization {

  private ImageRepository imageRepository;
  private ImageHandle image;
  private String imagePath = "";

  private int[] stripIndices;
  private boolean[] stripHighlight;
  private int cachedLength = -1;
  private long cachedRevision = Long.MIN_VALUE;
  private int cachedScreenW = -1;
  private int cachedScreenH = -1;
  private int cachedHandleGen = -1;
  private int drawRevision;
  private boolean hasDrawRevision;

  AbstractImageVisualization(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    setImage(blankHandle(screenWidth, screenHeight));
  }

  /** {@code true} = remap horizontal bands (Y); {@code false} = vertical bands (X). */
  protected abstract boolean horizontalMode();

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  @Override
  public void bindRepository(ImageRepository repository) {
    this.imageRepository = repository;
  }

  @Override
  public void setImage(ImageHandle handle) {
    if (handle == null) {
      return;
    }
    this.image = handle;
    if (handle.path() != null && !handle.path().isBlank()) {
      this.imagePath = handle.path();
    }
    invalidateCache();
  }

  @Override
  public boolean setImagePath(String path) {
    if (path == null || path.isBlank()) {
      return false;
    }
    if (imageRepository == null) {
      setImage(blankHandle(rs.getWidth(), rs.getHeight()));
      imagePath = path;
      return true;
    }
    ImageHandle loaded = imageRepository.load(path, rs.getWidth(), rs.getHeight());
    if (loaded == null) {
      return false;
    }
    imagePath = path;
    setImage(loaded);
    return true;
  }

  @Override
  public void update(float delta) {
    ensureImageSizedForWindow();
    if (image == null) {
      return;
    }

    int length = arrayController.getLength();
    if (length <= 0) {
      return;
    }

    boolean dirty = isDirty(length);
    if (dirty) {
      rebuildStrips(length);
      cachedRevision = arrayController.getVisualRevision();
      cachedScreenW = screenWidth;
      cachedScreenH = screenHeight;
      cachedHandleGen = image.generation();
      cachedLength = length;
      drawRevision++;
      hasDrawRevision = true;
    }

    if (!hasDrawRevision) {
      return;
    }

    rs.drawImageRemap(image, stripIndices, stripHighlight, length, horizontalMode(), drawRevision);
  }

  private boolean isDirty(int length) {
    return !hasDrawRevision
        || cachedRevision != arrayController.getVisualRevision()
        || cachedScreenW != screenWidth
        || cachedScreenH != screenHeight
        || cachedHandleGen != image.generation()
        || cachedLength != length
        || stripIndices == null
        || stripIndices.length < length;
  }

  private void rebuildStrips(int length) {
    if (stripIndices == null || stripIndices.length < length) {
      stripIndices = new int[length];
      stripHighlight = new boolean[length];
    }
    for (int i = 0; i < length; i++) {
      stripIndices[i] = arrayController.get(i);
      boolean highlight = arrayController.getMarker(i) == Marker.SET;
      stripHighlight[i] = highlight;
      if (highlight) {
        sound.playSound(i);
      }
    }
  }

  private void ensureImageSizedForWindow() {
    int w = Math.max(1, rs.getWidth());
    int h = Math.max(1, rs.getHeight());
    if (image != null && image.width() == w && image.height() == h) {
      return;
    }
    if (imageRepository != null && imagePath != null && !imagePath.isBlank()) {
      ImageHandle reloaded = imageRepository.load(imagePath, w, h);
      if (reloaded != null) {
        setImage(reloaded);
        return;
      }
    }
    if (imageRepository != null) {
      setImage(imageRepository.blank(w, h));
    } else {
      setImage(blankHandle(w, h));
    }
  }

  private void invalidateCache() {
    hasDrawRevision = false;
    cachedRevision = Long.MIN_VALUE;
  }

  private static ImageHandle blankHandle(int width, int height) {
    int w = Math.max(1, width);
    int h = Math.max(1, height);
    return new ImageHandle("", w, h, new int[w * h]);
  }
}
