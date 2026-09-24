precision highp float;
precision highp int;

uniform sampler2D u_source;
uniform sampler2D u_index;
uniform int u_length;
uniform int u_mode; // 0 = columns, 1 = rows, 2 = grid
uniform int u_cols;
uniform int u_rows;
uniform int u_indexWidth;
uniform float u_highlightStrength;
uniform int u_gridLines;
uniform vec2 u_rectPx; // on-screen frame size in pixels

in vec2 v_uv;
out vec4 fragColor;

void main() {
  vec2 uv = clamp(v_uv, 0.0, 0.99999);
  int piece;
  vec2 local;
  if (u_mode == 0) {
    float p = uv.x * float(u_length);
    piece = int(floor(p));
    local = vec2(fract(p), uv.y);
  } else if (u_mode == 1) {
    float p = uv.y * float(u_length);
    piece = int(floor(p));
    local = vec2(uv.x, fract(p));
  } else {
    vec2 cell = uv * vec2(float(u_cols), float(u_rows));
    ivec2 c = ivec2(floor(cell));
    piece = c.y * u_cols + c.x;
    local = fract(cell);
  }
  if (piece >= u_length) {
    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    return;
  }
  vec4 idx = texelFetch(u_index, ivec2(piece % u_indexWidth, piece / u_indexWidth), 0);
  int src = int(idx.r * 255.0 + 0.5) * 65536 + int(idx.g * 255.0 + 0.5) * 256
      + int(idx.b * 255.0 + 0.5);
  src = clamp(src, 0, u_length - 1);

  // Source piece rectangle in UV; sampling is clamped half a texel inside it so bilinear
  // filtering never bleeds in the neighbouring piece.
  vec2 lo;
  vec2 hi;
  if (u_mode == 0) {
    lo = vec2(float(src) / float(u_length), 0.0);
    hi = vec2(float(src + 1) / float(u_length), 1.0);
  } else if (u_mode == 1) {
    lo = vec2(0.0, float(src) / float(u_length));
    hi = vec2(1.0, float(src + 1) / float(u_length));
  } else {
    vec2 grid = vec2(float(u_cols), float(u_rows));
    vec2 srcCell = vec2(float(src % u_cols), float(src / u_cols));
    lo = srcCell / grid;
    hi = (srcCell + 1.0) / grid;
  }
  vec2 srcUv = mix(lo, hi, local);
  vec2 halfTexel = 0.5 / vec2(textureSize(u_source, 0));
  vec2 inner = min(halfTexel, (hi - lo) * 0.5);
  srcUv = clamp(srcUv, lo + inner, hi - inner);
  vec4 color = texture(u_source, srcUv);
  color.a = 1.0;
  if (idx.a < 0.5) {
    color = mix(color, vec4(1.0), clamp(u_highlightStrength, 0.0, 1.0));
  }
  if (u_mode == 2 && u_gridLines == 1) {
    vec2 tilePx = u_rectPx / vec2(float(u_cols), float(u_rows));
    vec2 edge = min(local, 1.0 - local) * tilePx;
    if (min(edge.x, edge.y) < 0.75) {
      color.rgb *= 0.15;
    }
  }
  fragColor = color;
}
