precision highp float;
precision highp int;

in vec3 a_position;
in vec3 a_normal;
in vec2 a_texCoord0;
in vec4 a_color;
in float a_piece;

uniform mat4 u_projView;
uniform sampler2D u_pieces; // RGBA32F, 5 texels per piece
uniform int u_perRow;

out vec3 v_normal;
out vec2 v_uv;
out vec4 v_vertexColor;
out vec4 v_pieceColor;

void main() {
  int piece = int(a_piece + 0.5);
  int base = (piece % u_perRow) * 5;
  int row = piece / u_perRow;
  vec4 r0 = texelFetch(u_pieces, ivec2(base, row), 0);
  vec4 r1 = texelFetch(u_pieces, ivec2(base + 1, row), 0);
  vec4 r2 = texelFetch(u_pieces, ivec2(base + 2, row), 0);
  vec4 color = texelFetch(u_pieces, ivec2(base + 3, row), 0);
  vec4 uvx = texelFetch(u_pieces, ivec2(base + 4, row), 0);

  vec4 p = vec4(a_position, 1.0);
  vec3 world = vec3(dot(r0, p), dot(r1, p), dot(r2, p));
  v_normal = vec3(dot(r0.xyz, a_normal), dot(r1.xyz, a_normal), dot(r2.xyz, a_normal));
  v_uv = a_texCoord0 * uvx.xy + uvx.zw;
  v_vertexColor = a_color;
  v_pieceColor = color;
  gl_Position = u_projView * vec4(world, 1.0);
}
