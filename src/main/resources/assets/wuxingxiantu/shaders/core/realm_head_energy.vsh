#version 150

in vec3 Position;
in vec2 UV0;
out vec2 texCoord;

// CPU submits only the projected bounds of one energy head, not a whole-screen
// volume per actor. This same quad is replayed into the selected emission FBO.
void main() {
    gl_Position=vec4(Position,1.0);
    texCoord=UV0;
}
