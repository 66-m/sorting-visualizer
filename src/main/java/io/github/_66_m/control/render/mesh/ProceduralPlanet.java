package io.github._66_m.control.render.mesh;

/**
 * Built-in equirectangular planet texture (oceans, continents, deserts, ice caps) generated from 3D
 * value noise, used by the Globe until the user picks an image (e.g. NASA Blue Marble).
 */
public final class ProceduralPlanet {

  private static volatile TextureImage planet;

  private ProceduralPlanet() {}

  /** The default 1024×512 planet (generated once). */
  public static TextureImage texture() {
    TextureImage t = planet;
    if (t == null) {
      synchronized (ProceduralPlanet.class) {
        t = planet;
        if (t == null) {
          t = generate(1024, 512, 7L);
          planet = t;
        }
      }
    }
    return t;
  }

  static TextureImage generate(int w, int h, long seed) {
    int[] argb = new int[w * h];
    for (int y = 0; y < h; y++) {
      double lat = Math.PI / 2 - (y + 0.5) * Math.PI / h;
      double cl = Math.cos(lat);
      double sl = Math.sin(lat);
      for (int x = 0; x < w; x++) {
        double lon = (x + 0.5) * 2 * Math.PI / w - Math.PI;
        double px = cl * Math.sin(lon);
        double pz = cl * Math.cos(lon);
        double elevation = fbm(px * 1.6, sl * 1.6, pz * 1.6, seed, 6) - 0.52;
        double moisture = fbm(px * 2.3 + 17, sl * 2.3, pz * 2.3 - 9, seed + 1, 4);
        argb[y * w + x] = color(elevation, moisture, Math.abs(Math.toDegrees(lat)));
      }
    }
    return new TextureImage(w, h, argb);
  }

  private static int color(double e, double moisture, double absLatDeg) {
    boolean ice = absLatDeg > 72 || (absLatDeg > 60 && e > 0.05) || e > 0.26;
    if (ice) {
      return rgb(235, 240, 245);
    }
    if (e < 0) {
      double depth = Math.min(1, -e / 0.3);
      return mix(rgb(40, 110, 170), rgb(8, 32, 84), depth);
    }
    if (e < 0.015) {
      return rgb(214, 196, 150); // beach
    }
    boolean desert = moisture < 0.47 && absLatDeg < 38;
    int low = desert ? rgb(200, 170, 110) : rgb(62, 128, 58);
    int high = desert ? rgb(160, 120, 80) : rgb(110, 100, 70);
    return mix(low, high, Math.min(1, e / 0.2));
  }

  private static double fbm(double x, double y, double z, long seed, int octaves) {
    double sum = 0;
    double amp = 0.5;
    double freq = 1;
    double norm = 0;
    for (int o = 0; o < octaves; o++) {
      sum += amp * valueNoise(x * freq, y * freq, z * freq, seed + o * 31L);
      norm += amp;
      amp *= 0.5;
      freq *= 2.0;
    }
    return sum / norm;
  }

  private static double valueNoise(double x, double y, double z, long seed) {
    int x0 = (int) Math.floor(x);
    int y0 = (int) Math.floor(y);
    int z0 = (int) Math.floor(z);
    double fx = smooth(x - x0);
    double fy = smooth(y - y0);
    double fz = smooth(z - z0);
    double c000 = hash(x0, y0, z0, seed);
    double c100 = hash(x0 + 1, y0, z0, seed);
    double c010 = hash(x0, y0 + 1, z0, seed);
    double c110 = hash(x0 + 1, y0 + 1, z0, seed);
    double c001 = hash(x0, y0, z0 + 1, seed);
    double c101 = hash(x0 + 1, y0, z0 + 1, seed);
    double c011 = hash(x0, y0 + 1, z0 + 1, seed);
    double c111 = hash(x0 + 1, y0 + 1, z0 + 1, seed);
    double x00 = c000 + (c100 - c000) * fx;
    double x10 = c010 + (c110 - c010) * fx;
    double x01 = c001 + (c101 - c001) * fx;
    double x11 = c011 + (c111 - c011) * fx;
    double y0v = x00 + (x10 - x00) * fy;
    double y1v = x01 + (x11 - x01) * fy;
    return y0v + (y1v - y0v) * fz;
  }

  private static double smooth(double t) {
    return t * t * (3 - 2 * t);
  }

  private static double hash(int x, int y, int z, long seed) {
    long h = seed * 0x9E3779B97F4A7C15L;
    h ^= x * 0xBF58476D1CE4E5B9L;
    h ^= y * 0x94D049BB133111EBL;
    h ^= z * 0xD6E8FEB86659FD93L;
    h ^= h >>> 31;
    h *= 0x9E3779B97F4A7C15L;
    h ^= h >>> 29;
    return (h >>> 11) * 0x1.0p-53;
  }

  private static int rgb(int r, int g, int b) {
    return 0xFF000000 | (r << 16) | (g << 8) | b;
  }

  private static int mix(int a, int b, double t) {
    int r = (int) Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
    int g = (int) Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
    int bl = (int) Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
    return rgb(r, g, bl);
  }
}
