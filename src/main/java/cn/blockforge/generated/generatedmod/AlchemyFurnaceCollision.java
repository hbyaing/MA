package cn.blockforge.generated.generatedmod;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Cached coarse structural boxes. Ornament detail is intentionally not physical geometry. */
public final class AlchemyFurnaceCollision {
    private static final Map<String,VoxelShape> CACHE=new HashMap<>();
    static {
        try (var stream=AlchemyFurnaceCollision.class.getResourceAsStream("/data/wuxingxiantu/alchemy/furnace_shapes.json")) {
            if (stream==null) throw new IllegalStateException("Missing furnace shapes");
            JsonObject root=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            for (String kind:new String[]{"collision","selection"}) for (var entry:root.getAsJsonObject(kind).entrySet()) {
                String[] cell=entry.getKey().split(",");
                int x=Integer.parseInt(cell[0]),y=Integer.parseInt(cell[1]),z=Integer.parseInt(cell[2]);
                for (int turn=0;turn<4;turn++) {
                    int rx=x,rz=z; for (int t=0;t<turn;t++) {int old=rx;rx=rz;rz=-old;}
                    VoxelShape shape=Shapes.empty();
                    for (var box:entry.getValue().getAsJsonArray()) {
                        var b=box.getAsJsonArray();double x0=b.get(0).getAsDouble(),z0=b.get(2).getAsDouble(),x1=b.get(3).getAsDouble(),z1=b.get(5).getAsDouble();
                        for (int t=0;t<turn;t++) {double old0=x0,old1=x1;x0=z0;x1=z1;z0=1-old1;z1=1-old0;}
                        shape=Shapes.or(shape,Shapes.box(x0,b.get(1).getAsDouble(),z0,x1,b.get(4).getAsDouble(),z1));
                    }
                    CACHE.put(kind+":"+turn+":"+rx+","+y+","+rz,shape.optimize());
                }
            }
        } catch (Exception e) {throw new ExceptionInInitializerError(e);}
    }
    public static VoxelShape at(BlockGetter level,BlockPos pos,boolean selection) {
        BlockPos core=level.getBlockEntity(pos) instanceof AlchemyFurnacePartEntity part ? part.core : pos;
        var state=level.getBlockState(core);
        int turn=Math.floorMod(Math.round(AlchemyFurnaceVisualLayout.rotation(AlchemyFurnaceStructure.inwardOf(state))/90),4);
        return CACHE.getOrDefault((selection?"selection":"collision")+":"+turn+":"+(pos.getX()-core.getX())+","+(pos.getY()-core.getY())+","+(pos.getZ()-core.getZ()),Shapes.empty());
    }
    private AlchemyFurnaceCollision() {}
}
