package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.logging.Logger;

/**
 * Hardware-instanced draw of unit boxes / quads / spheres. One {@link Mesh#render} per primitive
 * type. Requires {@link Gdx#gl30}.
 */
public final class InstanceRenderer3D implements Disposable {
  private static final Logger LOGGER = Logger.getLogger(InstanceRenderer3D.class.getName());

  /**
   * Start near a typical preview size; {@link #ensureCapacity} doubles up to large N so cold start
   * does not allocate for {@link
   * io.github.compilerstuck.control.config.SettingsDefaults#ARRAY_SIZE_MAX}.
   */
  private static final int INITIAL_MAX_INSTANCES = 4096;

  private static final String VERT_PATH = "shaders/instance_lit.vert";
  private static final String FRAG_PATH = "shaders/instance_lit.frag";

  public enum Kind {
    BOX,
    QUAD,
    SPHERE
  }

  private final InstanceTransform transform = new InstanceTransform();
  private final ShaderProgram shader;
  private Mesh boxMesh;
  private Mesh quadMesh;
  private Mesh sphereMesh;
  private int maxInstances;
  private float[] instanceFloats;

  private final float[] ambient = {0.4f, 0.4f, 0.4f};
  private final float[] lightDir = {-0.4f, -0.8f, -0.3f};
  private final float[] lightColor = {0.85f, 0.85f, 0.85f};

  public InstanceRenderer3D() {
    if (Gdx.gl30 == null) {
      throw new GdxRuntimeException("InstanceRenderer3D requires GL30");
    }
    boolean prevPedantic = ShaderProgram.pedantic;
    ShaderProgram.pedantic = false;
    String prevVert = ShaderProgram.prependVertexCode;
    String prevFrag = ShaderProgram.prependFragmentCode;
    ShaderProgram.prependVertexCode = "#version 300 es\n";
    ShaderProgram.prependFragmentCode = "#version 300 es\n";
    try {
      shader = new ShaderProgram(Gdx.files.internal(VERT_PATH), Gdx.files.internal(FRAG_PATH));
    } finally {
      ShaderProgram.prependVertexCode = prevVert;
      ShaderProgram.prependFragmentCode = prevFrag;
      ShaderProgram.pedantic = prevPedantic;
    }
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("instance_lit shader: " + shader.getLog());
    }
    maxInstances = INITIAL_MAX_INSTANCES;
    instanceFloats = new float[maxInstances * InstanceTransform.FLOATS_PER_INSTANCE];
    boxMesh = buildBox();
    quadMesh = buildQuad();
    sphereMesh = buildSphere();
    enableInstancing(boxMesh);
    enableInstancing(quadMesh);
    enableInstancing(sphereMesh);
    LOGGER.info("InstanceRenderer3D ready (maxInstances=" + maxInstances + ")");
  }

  public InstanceTransform transform() {
    return transform;
  }

  public void setSceneSize(float width, float height) {
    transform.setSceneSize(width, height);
  }

  /**
   * Uploads instance data and draws once. Caller must have set depth/blend; must not nest inside
   * ModelBatch begin/end.
   *
   * @param order optional instance index order (e.g. back-to-front); {@code null} uses natural
   *     order
   * @return true if a draw was issued
   */
  public boolean draw(Kind kind, InstanceData data, Matrix4 projView, int[] order) {
    if (data == null || data.count <= 0) {
      return false;
    }
    ensureCapacity(data.count);
    int floats =
        order != null
            ? transform.packOrdered(data, order, instanceFloats)
            : transform.pack(data, instanceFloats);
    Mesh mesh = meshFor(kind);
    mesh.setInstanceData(instanceFloats, 0, floats);

    shader.bind();
    shader.setUniformMatrix("u_projView", projView);
    shader.setUniform3fv("u_ambient", ambient, 0, 3);
    shader.setUniform3fv("u_lightDir", lightDir, 0, 3);
    shader.setUniform3fv("u_lightColor", lightColor, 0, 3);
    mesh.render(shader, GL20.GL_TRIANGLES);
    return true;
  }

  /** Draws in natural instance order. */
  public boolean draw(Kind kind, InstanceData data, Matrix4 projView) {
    return draw(kind, data, projView, null);
  }

  private Mesh meshFor(Kind kind) {
    return switch (kind) {
      case BOX -> boxMesh;
      case QUAD -> quadMesh;
      case SPHERE -> sphereMesh;
    };
  }

  private void ensureCapacity(int count) {
    if (count <= maxInstances) {
      return;
    }
    int next = maxInstances;
    while (next < count) {
      next *= 2;
    }
    LOGGER.info("Growing instance buffer " + maxInstances + " → " + next);
    boxMesh.disableInstancedRendering();
    quadMesh.disableInstancedRendering();
    sphereMesh.disableInstancedRendering();
    maxInstances = next;
    instanceFloats = new float[maxInstances * InstanceTransform.FLOATS_PER_INSTANCE];
    enableInstancing(boxMesh);
    enableInstancing(quadMesh);
    enableInstancing(sphereMesh);
  }

  private void enableInstancing(Mesh mesh) {
    mesh.enableInstancedRendering(
        false,
        maxInstances,
        new VertexAttribute(Usage.Generic, 4, "i_world_0"),
        new VertexAttribute(Usage.Generic, 4, "i_world_1"),
        new VertexAttribute(Usage.Generic, 4, "i_world_2"),
        new VertexAttribute(Usage.Generic, 4, "i_world_3"),
        new VertexAttribute(Usage.ColorUnpacked, 4, "i_color"));
  }

  private static Mesh buildBox() {
    MeshBuilder mb = new MeshBuilder();
    mb.begin(
        new VertexAttributes(
            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE)),
        GL20.GL_TRIANGLES);
    BoxShapeBuilder.build(mb, 1f, 1f, 1f);
    return mb.end();
  }

  private static Mesh buildQuad() {
    // Unit quad in XY (z=0), matching ModelBuilder.createRect used previously.
    MeshBuilder mb = new MeshBuilder();
    mb.begin(
        new VertexAttributes(
            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE)),
        GL20.GL_TRIANGLES);
    mb.rect(-0.5f, -0.5f, 0f, 0.5f, -0.5f, 0f, 0.5f, 0.5f, 0f, -0.5f, 0.5f, 0f, 0f, 0f, 1f);
    return mb.end();
  }

  private static Mesh buildSphere() {
    MeshBuilder mb = new MeshBuilder();
    mb.begin(
        new VertexAttributes(
            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE)),
        GL20.GL_TRIANGLES);
    SphereShapeBuilder.build(mb, 1f, 1f, 1f, 12, 12);
    return mb.end();
  }

  @Override
  public void dispose() {
    if (boxMesh != null) {
      boxMesh.dispose();
      boxMesh = null;
    }
    if (quadMesh != null) {
      quadMesh.dispose();
      quadMesh = null;
    }
    if (sphereMesh != null) {
      sphereMesh.dispose();
      sphereMesh = null;
    }
    if (shader != null) {
      shader.dispose();
    }
  }
}
