precision highp float;

uniform sampler2D u_texture;
uniform int u_colorMode; // 0 = piece, 1 = vertex, 2 = texture
uniform vec3 u_lightDir;
uniform vec3 u_ambient;
uniform vec3 u_lightColor;

in vec3 v_normal;
in vec2 v_uv;
in vec4 v_vertexColor;
in vec4 v_pieceColor;

out vec4 fragColor;

void main() {
  vec3 base;
  if (v_pieceColor.a < -0.5) {
    base = v_pieceColor.rgb; // solid-color piece
  } else if (u_colorMode == 2) {
    // OBJ convention: v = 0 is the bottom row; textures are uploaded top row first.
    base = texture(u_texture, vec2(v_uv.x, 1.0 - v_uv.y)).rgb;
  } else if (u_colorMode == 1) {
    base = v_vertexColor.rgb;
  } else {
    base = v_pieceColor.rgb;
  }
  base = mix(base, vec3(1.0), clamp(v_pieceColor.a, 0.0, 1.0));
  vec3 n = normalize(v_normal);
  // Two-sided: shards and slices are open shells, so back faces must light too.
  float diffuse = abs(dot(n, normalize(-u_lightDir)));
  vec3 lit = base * (u_ambient + u_lightColor * diffuse);
  fragColor = vec4(lit, 1.0);
}
