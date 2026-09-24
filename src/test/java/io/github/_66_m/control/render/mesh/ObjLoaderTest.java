package io.github._66_m.control.render.mesh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ObjLoaderTest {

  private static final String QUAD =
      """
      # unit quad
      v 0 0 0
      v 1 0 0
      v 1 1 0
      v 0 1 0
      f 1 2 3 4
      """;

  @Test
  void quadIsFanTriangulatedWithComputedNormals() throws IOException {
    MeshData m = ObjLoader.parse(new StringReader(QUAD), null);
    assertEquals(2, m.triangleCount());
    assertEquals(1f, m.normals[2], 1e-6f); // +Z face normal
    assertNull(m.uvs);
    assertNull(m.colors);
    assertFalse(m.hasColor());
  }

  @Test
  void negativeIndicesAndSlashFormsResolve() throws IOException {
    String obj =
        """
        v 0 0 0
        v 1 0 0
        v 0 1 0
        vt 0 0
        vn 0 0 1
        f -3/1/1 -2//1 -1/1
        """;
    MeshData m = ObjLoader.parse(new StringReader(obj), null);
    assertEquals(1, m.triangleCount());
    assertEquals(1f, m.positions[3], 1e-6f);
    assertEquals(1f, m.positions[7], 1e-6f);
  }

  @Test
  void emptyOrBrokenFilesFail() {
    assertThrows(IOException.class, () -> ObjLoader.parse(new StringReader("v 0 0 0\n"), null));
    assertThrows(
        IOException.class, () -> ObjLoader.parse(new StringReader("v 0 0 0\nf 1 2 3\n"), null));
  }

  @Test
  void materialDiffuseColorsBecomeVertexColors(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("m.mtl"), "newmtl red\nKd 1 0 0\n");
    Path obj = dir.resolve("m.obj");
    Files.writeString(obj, "mtllib m.mtl\nusemtl red\n" + QUAD);
    MeshData m = ObjLoader.load(obj);
    assertTrue(m.hasColor());
    assertEquals(0xFFFF0000, m.colors[0]);
  }

  @Test
  void normalizeCentersAndFitsTheUnitBall() throws IOException {
    MeshData m = MeshMath.normalize(ObjLoader.parse(new StringReader(QUAD), null), false);
    float maxR = 0f;
    float sumX = 0f;
    for (int i = 0; i < m.positions.length; i += 3) {
      float x = m.positions[i];
      float y = m.positions[i + 1];
      float z = m.positions[i + 2];
      maxR = Math.max(maxR, (float) Math.sqrt(x * x + y * y + z * z));
      sumX += x;
    }
    assertEquals(1f, maxR, 1e-5f);
    assertEquals(0f, sumX, 1e-4f);
  }

  @Test
  void zUpRotatesZOntoY() throws IOException {
    String obj = "v 0 0 0\nv 1 0 0\nv 0 0 5\nf 1 2 3\n";
    MeshData m = MeshMath.normalize(ObjLoader.parse(new StringReader(obj), null), true);
    float maxY = -1f;
    for (int i = 1; i < m.positions.length; i += 3) {
      maxY = Math.max(maxY, m.positions[i]);
    }
    assertTrue(maxY > 0.9f, "tallest extent is now vertical");
  }
}
