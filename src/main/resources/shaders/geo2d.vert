precision mediump float;

in vec2 a_position;
in vec4 a_color;
in vec2 a_uv;

uniform mat4 u_projView;

out vec4 v_color;
out vec2 v_uv;

void main() {
  v_color = a_color;
  v_uv = a_uv;
  gl_Position = u_projView * vec4(a_position, 0.0, 1.0);
}
