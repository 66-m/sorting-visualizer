precision mediump float;

in vec4 v_color;
in vec2 v_uv;

uniform float u_circleMask;

out vec4 fragColor;

void main() {
  float alpha = v_color.a;
  if (u_circleMask > 0.5) {
    float d = length(v_uv);
    // Soft edge ~1px-ish in UV space for mild AA.
    float edge = smoothstep(1.0, 0.92, d);
    alpha *= edge;
    if (alpha < 0.004) {
      discard;
    }
  }
  fragColor = vec4(v_color.rgb, alpha);
}
