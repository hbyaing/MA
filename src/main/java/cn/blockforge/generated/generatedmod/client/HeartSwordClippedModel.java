package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import cn.blockforge.generated.generatedmod.client.heartsword.HeartSwordModelGeometry;

/** Clips the original OBJ faces at the growth front; never stretches blade/UVs. */
public final class HeartSwordClippedModel extends BakedModelWrapper<BakedModel> {
    private final float limit;
    private final boolean empty;
    private final float reveal;
    private final boolean summon;
    private List<BakedModel> regularPasses, fabulousPasses;
    HeartSwordClippedModel(BakedModel original,float reveal,boolean summon) {
        super(original);
        this.reveal=reveal;this.summon=summon;
        limit=HeartSwordModelGeometry.revealY(reveal);
        empty=reveal<=0;
    }
    public float reveal() { return reveal; }
    public boolean summoning() { return summon; }
    @Override public BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left) {
        originalModel.applyTransform(context,pose,left);return this;
    }
    @Override public List<BakedModel> getRenderPasses(ItemStack stack,boolean fabulous) {
        var cached=fabulous?fabulousPasses:regularPasses;
        if(cached!=null)return cached;
        var passes=originalModel.getRenderPasses(stack,fabulous);
        var result=passes.size()==1&&passes.get(0)==originalModel?List.<BakedModel>of(this)
                :passes.stream().map(pass->(BakedModel)new HeartSwordClippedModel(pass,reveal,summon)).toList();
        if(fabulous)fabulousPasses=result;else regularPasses=result;
        return result;
    }
    @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random) {
        return clip(originalModel.getQuads(state,side,random));
    }
    @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType type) {
        return clip(originalModel.getQuads(state,side,random,data,type));
    }
    private List<BakedQuad> clip(List<BakedQuad> source) {
        List<BakedQuad> result=new ArrayList<>();
        if(empty)return result;
        for(BakedQuad quad:source) {
            int[] raw=quad.getVertices();int stride=raw.length/4;
            float minY=Float.MAX_VALUE,maxY=-Float.MAX_VALUE;
            for(int v=0;v<4;v++) {
                minY=Math.min(minY,Float.intBitsToFloat(raw[v*stride+1]));
                maxY=Math.max(maxY,Float.intBitsToFloat(raw[v*stride+1]));
            }
            if(minY>limit)continue;
            if(maxY<=limit) { result.add(quad); continue; }
            List<int[]> polygon=new ArrayList<>(5);
            for(int i=0;i<4;i++) {
                int[] a=java.util.Arrays.copyOfRange(raw,i*stride,(i+1)*stride);
                int next=(i+1)%4;
                int[] b=java.util.Arrays.copyOfRange(raw,next*stride,(next+1)*stride);
                float ay=Float.intBitsToFloat(a[1]),by=Float.intBitsToFloat(b[1]);
                boolean insideA=ay<=limit,insideB=by<=limit;
                if(insideA)polygon.add(a);
                if(insideA!=insideB) {
                    float t=(limit-ay)/(by-ay);int[] cut=a.clone();
                    for(int field:new int[]{0,1,2,4,5}) {
                        float start=Float.intBitsToFloat(a[field]),end=Float.intBitsToFloat(b[field]);
                        cut[field]=Float.floatToRawIntBits(start+(end-start)*t);
                    }
                    polygon.add(cut);
                }
            }
            if(polygon.size()<3)continue;
            // Triangle fan also handles clipped quads with five vertices.
            for(int i=1;i<polygon.size()-1;i++) {
                int[] vertices=new int[raw.length];
                int[][] triangle={polygon.get(0),polygon.get(i),polygon.get(i+1),polygon.get(i+1)};
                for(int v=0;v<4;v++)System.arraycopy(triangle[v],0,vertices,v*stride,stride);
                result.add(new BakedQuad(vertices,quad.getTintIndex(),quad.getDirection(),quad.getSprite(),quad.isShade()));
            }
        }
        return result;
    }
}
