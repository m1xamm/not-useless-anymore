package io.github.m1xamm.nua.client;

import com.google.common.base.Suppliers;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoArmorRenderer;
import io.github.m1xamm.nua.Nua;
import io.github.m1xamm.nua.item.NuaItems;
import io.github.m1xamm.nua.item.StormHelmetItem;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = Nua.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Nua.MOD_ID, value = Dist.CLIENT)
public class NuaClient {

    public NuaClient(IEventBus modBus) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        NuaItems.STORM_HELMET.get().setGeoRenderProvider(new GeoRenderProvider() {

            private final com.google.common.base.Supplier<GeoArmorRenderer<StormHelmetItem, HumanoidRenderState>> renderer =
                    Suppliers.memoize(() -> new GeoArmorRenderer<>(NuaItems.STORM_HELMET.get()));

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                return this.renderer.get();
            }
        });
    }
}
