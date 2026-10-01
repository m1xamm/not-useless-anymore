package io.github.m1xamm.nua.item;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

/**
 * A copper helmet with a lightning rod model on top. The renderer itself is client side and is
 * attached in the client setup, so only the holder lives here.
 */
public class StormHelmetItem extends Item implements GeoItem {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private final AtomicReference<GeoRenderProvider> geoRenderProvider = new AtomicReference<>();

    public StormHelmetItem(Properties properties) {
        super(properties.humanoidArmor(ArmorMaterials.COPPER, ArmorType.HELMET));
    }

    public void setGeoRenderProvider(GeoRenderProvider provider) {
        this.geoRenderProvider.set(provider);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.geoRenderProvider.get());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
