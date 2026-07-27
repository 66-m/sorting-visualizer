precision mediump float;

in vec3 a_position;
in vec3 a_normal;

in vec4 i_world_0;
in vec4 i_world_1;
in vec4 i_world_2;
in vec4 i_world_3;
in vec4 i_color;

uniform mat4 u_projView;
uniform vec3 u_ambient;
uniform vec3 u_lightDir;
uniform vec3 u_lightColor;

out vec4 v_color;

void main() {
  mat4 world = mat4(i_world_0, i_world_1, i_world_2, i_world_3);
  vec4 worldPos = world * vec4(a_position, 1.0);
  gl_Position = u_projView * worldPos;

  mat3 worldN = mat3(world);
  vec3 n = normalize(worldN * a_normal);
  float ndl = max(dot(n, normalize(-u_lightDir)), 0.0);
  vec3 lit = u_ambient + u_lightColor * ndl;
  v_color = vec4(clamp(lit, 0.0, 1.0) * i_color.rgb, i_color.a);
}
