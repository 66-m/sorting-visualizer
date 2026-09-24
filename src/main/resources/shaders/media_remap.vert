precision highp float;

// Unit quad corner in [0,1]²; placed into u_rect (NDC x0, y0, x1, y1).
in vec2 a_position;

uniform vec4 u_rect;

out vec2 v_uv;

void main() {
  // v_uv: (0,0) = top-left of the frame.
  v_uv = vec2(a_position.x, 1.0 - a_position.y);
  vec2 ndc = mix(u_rect.xy, u_rect.zw, a_position);
  gl_Position = vec4(ndc, 0.0, 1.0);
}
