package net.MechGaming.EndlessSands.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class EndlessSandsMixinPlugin implements IMixinConfigPlugin {
    private static final String NO_CUBES_MOD_ID = "nocubesreloadedbase";
    private static final String NO_CUBES_MIXIN = "net.MechGaming.EndlessSands.mixin.NoCubesCursedSandLayerMixin";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (NO_CUBES_MIXIN.equals(mixinClassName)) {
            return LoadingModList.get().getModFileById(NO_CUBES_MOD_ID) != null;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}