package cn.blockforge.generated.generatedmod.boss;

import software.bernie.geckolib.animatable.GeoEntity;
import java.util.UUID;

public interface BossVisualEntity extends GeoEntity {
    BossVisualDefinition visualDefinition();
    UUID bossEventId();
    boolean isVisualTest();
    String previewAnimation();
    long previewStarted();
    long previewUntil();
    void previewAnimation(String name);
    void clientMotion(String name,long start,long until);
}
