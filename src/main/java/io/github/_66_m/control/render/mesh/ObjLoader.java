package io.github._66_m.control.render.mesh;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Minimal Wavefront OBJ reader: {@code v}, {@code vt}, {@code vn}, {@code f} (triangles and
 * polygons, fan-triangulated; negative indices), {@code mtllib} / {@code usemtl} with {@code Kd}
 * colors and a {@code map_Kd} diffuse texture (first one found). Everything else is ignored.
 */
public final class ObjLoader {

  /** Refuse bigger meshes: they would not render interactively anyway. */
  public static final int MAX_TRIANGLES = 2_000_000;

  /** Textures are downscaled to at most this edge length. */
  static final int MAX_TEXTURE_EDGE = 4096;

  private ObjLoader() {}

  /** Loads and parses {@code path} (plus its MTL / texture when present). */
  public static MeshData load(Path path) throws IOException {
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return parse(reader, path.toAbsolutePath().getParent());
    }
  }

  /**
   * Parses OBJ text. {@code baseDir} resolves {@code mtllib} and textures; {@code null} skips
   * materials.
   */
  public static MeshData parse(Reader reader, Path baseDir) throws IOException {
    FloatList v = new FloatList();
    FloatList vt = new FloatList();
    FloatList vn = new FloatList();
    FloatList outPos = new FloatList();
    FloatList outNorm = new FloatList();
    FloatList outUv = new FloatList();
    IntList outColor = new IntList();
    boolean anyUv = false;
    boolean anyNormal = true;
    Map<String, Material> materials = new HashMap<>();
    Material material = null;
    TextureImage texture = null;
    boolean anyMaterialColor = false;

    BufferedReader in = reader instanceof BufferedReader br ? br : new BufferedReader(reader);
    String line;
    int[] vi = new int[64];
    int[] ti = new int[64];
    int[] ni = new int[64];
    while ((line = in.readLine()) != null) {
      line = line.strip();
      if (line.isEmpty() || line.charAt(0) == '#') {
        continue;
      }
      String[] parts = line.split("\\s+");
      switch (parts[0]) {
        case "v" -> {
          v.add(f(parts, 1));
          v.add(f(parts, 2));
          v.add(f(parts, 3));
        }
        case "vt" -> {
          vt.add(f(parts, 1));
          vt.add(parts.length > 2 ? f(parts, 2) : 0f);
        }
        case "vn" -> {
          vn.add(f(parts, 1));
          vn.add(f(parts, 2));
          vn.add(f(parts, 3));
        }
        case "mtllib" -> {
          if (baseDir != null && parts.length > 1) {
            String name = line.substring(line.indexOf(parts[1]));
            materials.putAll(readMtl(baseDir.resolve(name)));
          }
        }
        case "usemtl" -> {
          material = parts.length > 1 ? materials.get(parts[1]) : null;
          if (material != null) {
            anyMaterialColor = true;
            if (texture == null && material.texture != null) {
              texture = material.texture;
            }
          }
        }
        case "f" -> {
          int count = parts.length - 1;
          if (count < 3) {
            continue;
          }
          if (count > vi.length) {
            vi = new int[count];
            ti = new int[count];
            ni = new int[count];
          }
          for (int k = 0; k < count; k++) {
            String[] refs = parts[k + 1].split("/", -1);
            vi[k] = resolve(refs[0], v.size() / 3);
            ti[k] = refs.length > 1 && !refs[1].isEmpty() ? resolve(refs[1], vt.size() / 2) : -1;
            ni[k] = refs.length > 2 && !refs[2].isEmpty() ? resolve(refs[2], vn.size() / 3) : -1;
          }
          for (int k = 1; k + 1 < count; k++) {
            int[] corners = {0, k, k + 1};
            for (int c : corners) {
              int p = vi[c];
              if (p < 0 || p * 3 + 2 >= v.size()) {
                throw new IOException("Face references a missing vertex");
              }
              outPos.add(v.get(p * 3));
              outPos.add(v.get(p * 3 + 1));
              outPos.add(v.get(p * 3 + 2));
              int t = ti[c];
              if (t >= 0 && t * 2 + 1 < vt.size()) {
                outUv.add(vt.get(t * 2));
                outUv.add(vt.get(t * 2 + 1));
                anyUv = true;
              } else {
                outUv.add(0f);
                outUv.add(0f);
              }
              int n = ni[c];
              if (n >= 0 && n * 3 + 2 < vn.size()) {
                outNorm.add(vn.get(n * 3));
                outNorm.add(vn.get(n * 3 + 1));
                outNorm.add(vn.get(n * 3 + 2));
              } else {
                outNorm.add(0f);
                outNorm.add(0f);
                outNorm.add(0f);
                anyNormal = false;
              }
              outColor.add(material != null ? material.kd : 0xFFB0B0B0);
            }
            if (outPos.size() / 9 > MAX_TRIANGLES) {
              throw new IOException("Model has more than " + MAX_TRIANGLES + " triangles");
            }
          }
        }
        default -> {}
      }
    }
    if (outPos.size() == 0) {
      throw new IOException("Model contains no faces");
    }
    float[] positions = outPos.toArray();
    float[] normals = outNorm.toArray();
    if (!anyNormal) {
      MeshMath.faceNormals(positions, normals);
    }
    float[] uvs = anyUv && texture != null ? outUv.toArray() : null;
    int[] colors = anyMaterialColor ? outColor.toArray() : null;
    return new MeshData(positions, normals, uvs, colors, uvs != null ? texture : null);
  }

  private static int resolve(String ref, int count) {
    int idx = Integer.parseInt(ref);
    return idx < 0 ? count + idx : idx - 1;
  }

  private static float f(String[] parts, int i) throws IOException {
    if (i >= parts.length) {
      throw new IOException("Malformed line: " + String.join(" ", parts));
    }
    try {
      return Float.parseFloat(parts[i]);
    } catch (NumberFormatException e) {
      throw new IOException("Malformed number: " + parts[i], e);
    }
  }

  private static Map<String, Material> readMtl(Path mtl) {
    Map<String, Material> out = new HashMap<>();
    if (!Files.isReadable(mtl)) {
      return out;
    }
    try (BufferedReader in = Files.newBufferedReader(mtl, StandardCharsets.UTF_8)) {
      Material current = null;
      String line;
      while ((line = in.readLine()) != null) {
        line = line.strip();
        if (line.isEmpty() || line.charAt(0) == '#') {
          continue;
        }
        String[] parts = line.split("\\s+");
        switch (parts[0]) {
          case "newmtl" -> {
            current = new Material();
            if (parts.length > 1) {
              out.put(parts[1], current);
            }
          }
          case "Kd" -> {
            if (current != null && parts.length >= 4) {
              current.kd = rgb(f(parts, 1), f(parts, 2), f(parts, 3));
            }
          }
          case "map_Kd" -> {
            if (current != null && parts.length > 1) {
              // Options such as "-s 1 1 1" may precede the file name; it is the last token.
              current.texture = readTexture(mtl.getParent().resolve(parts[parts.length - 1]));
            }
          }
          default -> {}
        }
      }
    } catch (IOException e) {
      // A broken MTL only costs colors.
    }
    return out;
  }

  static TextureImage readTexture(Path file) {
    try {
      if (!Files.isReadable(file)) {
        return null;
      }
      BufferedImage img = ImageIO.read(file.toFile());
      if (img == null) {
        return null;
      }
      return fromImage(img, MAX_TEXTURE_EDGE);
    } catch (IOException | RuntimeException e) {
      return null;
    }
  }

  /** Converts (and downsamples when larger than {@code maxEdge}) an AWT image to ARGB. */
  public static TextureImage fromImage(BufferedImage img, int maxEdge) {
    int w = img.getWidth();
    int h = img.getHeight();
    int step = 1;
    while (w / step > maxEdge || h / step > maxEdge) {
      step++;
    }
    int ow = Math.max(1, w / step);
    int oh = Math.max(1, h / step);
    int[] argb = new int[ow * oh];
    for (int y = 0; y < oh; y++) {
      for (int x = 0; x < ow; x++) {
        argb[y * ow + x] = img.getRGB(x * step, y * step) | 0xFF000000;
      }
    }
    return new TextureImage(ow, oh, argb);
  }

  private static int rgb(float r, float g, float b) {
    int ri = Math.round(Math.max(0f, Math.min(1f, r)) * 255f);
    int gi = Math.round(Math.max(0f, Math.min(1f, g)) * 255f);
    int bi = Math.round(Math.max(0f, Math.min(1f, b)) * 255f);
    return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
  }

  private static final class Material {
    int kd = 0xFFB0B0B0;
    TextureImage texture;
  }

  /** Growable primitive float list. */
  static final class FloatList {
    private float[] data = new float[1024];
    private int size;

    void add(float x) {
      if (size == data.length) {
        data = java.util.Arrays.copyOf(data, data.length * 2);
      }
      data[size++] = x;
    }

    float get(int i) {
      return data[i];
    }

    int size() {
      return size;
    }

    float[] toArray() {
      return java.util.Arrays.copyOf(data, size);
    }
  }

  /** Growable primitive int list. */
  static final class IntList {
    private int[] data = new int[1024];
    private int size;

    void add(int x) {
      if (size == data.length) {
        data = java.util.Arrays.copyOf(data, data.length * 2);
      }
      data[size++] = x;
    }

    int[] toArray() {
      return java.util.Arrays.copyOf(data, size);
    }
  }
}
