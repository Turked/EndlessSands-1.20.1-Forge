package net.MechGaming.EndlessSands.item.custom;

import net.MechGaming.EndlessSands.client.render.VultureAdvancementIconRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class VultureAdvancementIconItem extends Item {
    public VultureAdvancementIconItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new VultureAdvancementIconRenderer();
                }
                return this.renderer;
            }
        });
    }
}
