package io.github._66_m.control.render.mesh;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Computes a value for a key on a background thread and keeps serving the last finished value while
 * a newer key is being computed. Used for derived model data (samples, voxels, pieces) so the
 * render thread never blocks.
 *
 * @param <K> key type (must implement {@code equals})
 * @param <V> value type
 */
public final class AsyncValue<K, V> {

  private static final Logger LOGGER = Logger.getLogger(AsyncValue.class.getName());

  private final String threadName;
  private K requestedKey;
  private K readyKey;
  private V ready;
  private boolean computing;
  private K pendingKey;
  private Supplier<V> pendingSupplier;

  public AsyncValue(String threadName) {
    this.threadName = threadName;
  }

  /**
   * Returns the newest finished value (possibly for an older key, or {@code null} before the first
   * one) and schedules {@code supplier} when {@code key} differs from what was last requested.
   */
  public synchronized V get(K key, Supplier<V> supplier) {
    if (!Objects.equals(key, requestedKey)) {
      requestedKey = key;
      if (computing) {
        pendingKey = key;
        pendingSupplier = supplier;
      } else {
        start(key, supplier);
      }
    }
    return ready;
  }

  /** {@code true} when the value for the last requested key is available. */
  public synchronized boolean isCurrent() {
    return requestedKey != null && Objects.equals(requestedKey, readyKey);
  }

  /** Drops everything (e.g. after the model changed). */
  public synchronized void reset() {
    requestedKey = null;
    readyKey = null;
    ready = null;
    pendingKey = null;
    pendingSupplier = null;
  }

  /** Runs synchronously; for tests / headless paths. */
  public synchronized V getNow(K key, Supplier<V> supplier) {
    if (!Objects.equals(key, readyKey)) {
      ready = supplier.get();
      readyKey = key;
      requestedKey = key;
    }
    return ready;
  }

  private void start(K key, Supplier<V> supplier) {
    computing = true;
    Thread t =
        new Thread(
            () -> {
              V value = null;
              try {
                value = supplier.get();
              } catch (RuntimeException | OutOfMemoryError e) {
                LOGGER.log(Level.WARNING, "Background model computation failed", e);
              }
              finish(key, value);
            },
            threadName);
    t.setDaemon(true);
    t.start();
  }

  private synchronized void finish(K key, V value) {
    computing = false;
    if (value != null) {
      ready = value;
      readyKey = key;
    }
    if (pendingSupplier != null) {
      K k = pendingKey;
      Supplier<V> s = pendingSupplier;
      pendingKey = null;
      pendingSupplier = null;
      start(k, s);
    }
  }
}
