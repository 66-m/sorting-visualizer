package io.github.compilerstuck.control.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstanceTransformTest {

  private InstanceTransform transform;
  private InstanceData data;

  @BeforeEach
  void setUp() {
    transform = new InstanceTransform();
    transform.setSceneSize(800f, 600f);
    data = new InstanceData();
    data.ensureCapacity(2);
    data.count = 1;
  }

  @Test
  void worldPacksPositionAsAuthored() {
    data.set(0, 12f, -34f, 56f, 1f, 1f, 1f, 0f, 0f, 0f, 0xFF112233);
    float[] out = new float[InstanceTransform.FLOATS_PER_INSTANCE];
    transform.packOne(data, 0, out, 0);
    assertEquals(12f, out[12], 1e-4f);
    assertEquals(-34f, out[13], 1e-4f);
    assertEquals(56f, out[14], 1e-4f);
  }

  @Test
  void packMatchesBuildMatrixAndColor() {
    data.set(0, 100f, 50f, -20f, 2f, 3f, 4f, 0.1f, 0.2f, 0.3f, 0x80FF00AA);
    data.count = 1;

    Matrix4 expected = new Matrix4();
    transform.buildMatrix(data, 0, expected);

    float[] packed = new float[InstanceTransform.FLOATS_PER_INSTANCE];
    transform.packOne(data, 0, packed, 0);

    float[] m = expected.val;
    for (int i = 0; i < 16; i++) {
      assertEquals(m[i], packed[i], 1e-5f, "matrix float " + i);
    }

    assertEquals(1f, packed[16], 1e-5f);
    assertEquals(0f, packed[17], 1e-5f);
    assertEquals(170f / 255f, packed[18], 1e-5f);
    assertEquals(128f / 255f, packed[19], 1e-5f);
  }

  @Test
  void unpackArgbHonorsZeroAlphaAsTransparent() {
    data.set(0, 0f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 0f, 0x00112233);
    data.count = 1;
    float[] packed = new float[InstanceTransform.FLOATS_PER_INSTANCE];
    transform.packOne(data, 0, packed, 0);
    assertEquals(0x11 / 255f, packed[16], 1e-5f);
    assertEquals(0x22 / 255f, packed[17], 1e-5f);
    assertEquals(0x33 / 255f, packed[18], 1e-5f);
    assertEquals(0f, packed[19], 1e-5f);
  }

  @Test
  void unpackArgbHonorsFullOpacity() {
    data.set(0, 0f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 0f, 0xFFAABBCC);
    data.count = 1;
    float[] packed = new float[InstanceTransform.FLOATS_PER_INSTANCE];
    transform.packOne(data, 0, packed, 0);
    assertEquals(1f, packed[19], 1e-5f);
  }

  @Test
  void worldEulerAppliedWithoutNegation() {
    data.set(0, 0f, 0f, 0f, 1f, 1f, 1f, 0.5f, 0.25f, -0.1f, 0xFFFFFFFF);
    Matrix4 expected = new Matrix4();
    expected.idt();
    expected.translate(0f, 0f, 0f);
    expected.rotateRad(Vector3.X, 0.5f);
    expected.rotateRad(Vector3.Y, 0.25f);
    expected.rotateRad(Vector3.Z, -0.1f);
    expected.scale(1f, 1f, 1f);

    Matrix4 actual = new Matrix4();
    transform.buildMatrix(data, 0, actual);
    float[] e = expected.val;
    float[] a = actual.val;
    for (int i = 0; i < 16; i++) {
      assertEquals(e[i], a[i], 1e-5f, "matrix float " + i);
    }
  }

  @Test
  void packWritesFloatsPerInstanceTimesCount() {
    data.set(0, 0f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 0f, 0xFFFFFFFF);
    data.set(1, 10f, 20f, 30f, 1f, 1f, 1f, 0f, 0f, 0f, 0xFF000000);
    data.count = 2;
    float[] out = new float[2 * InstanceTransform.FLOATS_PER_INSTANCE];
    assertEquals(40, transform.pack(data, out));
  }

  @Test
  void transformLocalPointMatchesScaledTranslatedOrigin() {
    data.set(0, 10f, 20f, 30f, 2f, 2f, 2f, 0f, 0f, 0f, 0xFFFFFFFF);
    data.count = 1;
    float[] out = new float[3];
    transform.transformLocalPoint(data, 0, 0.5f, 0f, 0f, out, 0);
    assertEquals(11f, out[0], 1e-4f);
    assertEquals(20f, out[1], 1e-4f);
    assertEquals(30f, out[2], 1e-4f);
  }
}
