package io.github._66_m.control.render.mesh;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The mesh behind a model visualization: the built-in rocket until an OBJ file finished loading in
 * the background. Parsed files are cached process-wide, so switching between model visualizations
 * does not re-read them.
 */
public final class ModelSource {

  private static final Logger LOGGER = Logger.getLogger(ModelSource.class.getName());
  private static final Map<String, MeshData> RAW_CACHE = new ConcurrentHashMap<>();

  private volatile String path = "";
  private volatile boolean zUp;
  private volatile MeshData mesh;
  private volatile String status = "";
  private volatile boolean loading;
  private volatile long generation = 1;
  private int loadGeneration;

  /** Current mesh (the default rocket when nothing is loaded). */
  public MeshData mesh() {
    MeshData m = mesh;
    return m != null ? m : DefaultModel.rocket();
  }

  /** Increments whenever {@link #mesh()} changes. */
  public long generation() {
    return generation;
  }

  public String path() {
    return path;
  }

  /** Loading / error message; empty when fine. */
  public String status() {
    return status;
  }

  public boolean isLoading() {
    return loading;
  }

  /** Starts loading {@code newPath} (empty = default model) with the given up axis. */
  public synchronized void load(String newPath, boolean newZUp) {
    String p = newPath == null ? "" : newPath;
    if (p.equals(path) && newZUp == zUp && !loading && (mesh != null || p.isEmpty())) {
      return;
    }
    path = p;
    zUp = newZUp;
    int gen = ++loadGeneration;
    if (p.isEmpty()) {
      publish(gen, null, "");
      return;
    }
    loading = true;
    status = "Loading model…";
    Thread t =
        new Thread(
            () -> {
              try {
                MeshData raw = RAW_CACHE.get(p);
                if (raw == null) {
                  raw = ObjLoader.load(Path.of(p));
                  RAW_CACHE.clear(); // Keep only the latest file to bound memory.
                  RAW_CACHE.put(p, raw);
                }
                publish(gen, MeshMath.normalize(raw, newZUp), "");
              } catch (IOException | RuntimeException | OutOfMemoryError e) {
                LOGGER.log(Level.WARNING, "Cannot load model " + p, e);
                publish(gen, null, "Cannot load model: " + e.getMessage());
              }
            },
            "model-load");
    t.setDaemon(true);
    t.start();
  }

  /** Re-applies the up axis for the current file. */
  public void setZUp(boolean newZUp) {
    if (newZUp != zUp) {
      load(path, newZUp);
    }
  }

  private synchronized void publish(int gen, MeshData m, String message) {
    if (gen != loadGeneration) {
      return;
    }
    mesh = m;
    status = message;
    loading = false;
    generation++;
  }
}
