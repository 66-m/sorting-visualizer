precision mediump float;

uniform sampler2D u_source;
uniform sampler2D u_index;
uniform float u_length;
uniform int u_horizontal;

in vec2 v_uv;
out vec4 fragColor;

void main() {
  float len = max(u_length, 1.0);
  float strip;
  float along;
  if (u_horizontal == 1) {
    strip = floor(v_uv.y * len);
    along = v_uv.x;
  } else {
    strip = floor(v_uv.x * len);
    along = v_uv.y;
  }
  strip = clamp(strip, 0.0, len - 1.0);
  vec4 idxSample = texture(u_index, vec2((strip + 0.5) / len, 0.5));
  if (idxSample.a < 0.5) {
    fragColor = vec4(1.0, 1.0, 1.0, 1.0);
    return;
  }
  float srcStrip = idxSample.r * 255.0 * 256.0 + idxSample.g * 255.0;
  srcStrip = clamp(srcStrip, 0.0, len - 1.0);

  vec2 srcUv;
  if (u_horizontal == 1) {
    srcUv = vec2(along, (srcStrip + 0.5) / len);
  } else {
    srcUv = vec2((srcStrip + 0.5) / len, along);
  }
  fragColor = texture(u_source, srcUv);
}
