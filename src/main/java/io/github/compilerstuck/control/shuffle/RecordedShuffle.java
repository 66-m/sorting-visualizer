package io.github.compilerstuck.control.shuffle;

import java.util.Arrays;
import java.util.Objects;

/**
 * Snapshot of a mute shuffle: pre/post permutations plus recorded swap pairs for a paced visual
 * replay that ends on the same post state.
 */
public final class RecordedShuffle {
  private final int[] pre;
  private final int[] post;

  /** Flat {@code i,j} pairs in mute-shuffle order. */
  private final int[] swapPairs;

  public RecordedShuffle(int[] pre, int[] post, int[] swapPairs) {
    this.pre = Objects.requireNonNull(pre, "pre").clone();
    this.post = Objects.requireNonNull(post, "post").clone();
    this.swapPairs = swapPairs != null ? swapPairs.clone() : new int[0];
    if (this.pre.length != this.post.length) {
      throw new IllegalArgumentException("pre/post length mismatch");
    }
    if ((this.swapPairs.length & 1) != 0) {
      throw new IllegalArgumentException("swapPairs must contain i,j pairs");
    }
  }

  public int[] pre() {
    return pre.clone();
  }

  public int[] post() {
    return post.clone();
  }

  public int[] swapPairs() {
    return swapPairs.clone();
  }

  public int length() {
    return pre.length;
  }

  public int swapCount() {
    return swapPairs.length / 2;
  }

  public int swapI(int index) {
    return swapPairs[index * 2];
  }

  public int swapJ(int index) {
    return swapPairs[index * 2 + 1];
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RecordedShuffle that)) {
      return false;
    }
    return Arrays.equals(pre, that.pre)
        && Arrays.equals(post, that.post)
        && Arrays.equals(swapPairs, that.swapPairs);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(pre);
    result = 31 * result + Arrays.hashCode(post);
    result = 31 * result + Arrays.hashCode(swapPairs);
    return result;
  }
}
