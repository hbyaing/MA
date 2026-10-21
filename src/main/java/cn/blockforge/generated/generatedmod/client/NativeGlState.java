package cn.blockforge.generated.generatedmod.client;
/** Tiny GL queries use scoped stack memory rather than allocating cleaner-backed direct buffers per draw. */
public final class NativeGlState {
    public static boolean[] colorMask(){
        com.mojang.blaze3d.systems.RenderSystem.assertOnRenderThread();
        try(var stack=org.lwjgl.system.MemoryStack.stackPush()){
            var b=stack.malloc(4);org.lwjgl.opengl.GL11.glGetBooleanv(org.lwjgl.opengl.GL11.GL_COLOR_WRITEMASK,b);
            return new boolean[]{b.get(0)!=0,b.get(1)!=0,b.get(2)!=0,b.get(3)!=0};
        }
    }
}
