# Rendering and GUI — NeoForge 26.1.2

26.1 is a **submission-based** render pipeline. The old immediate `MultiBufferSource` style is gone. This is the single largest change in the API and the easiest place to write code that compiles but renders nothing.

## The model

```
createRenderState()                 build immutable render state (no game object refs!)
extractRenderState(entity, state, tick)   copy data from entity to state
submit(state, poseStack, collector, camera)  queue geometry/feature submissions
FeatureRenderDispatcher#renderAllFeatures  executes the queue
```

`SubmitNodeCollector` (superinterface `OrderedSubmitNodeCollector`) submission methods:

`submitShadow` · `submitNameTag` · `submitText` · `submitFlame` · `submitLeash` · `submitModel` · `submitModelPart` · `submitMovingBlock` · `submitBlockModel` · `submitBreakingBlockModel` · `submitItem` · `submitCustomGeometry(RenderType, ...)` · `submitParticleGroup`, plus NeoForge's `submitMultiLayerBlockModel`.

`collector.order(int)` sets the pass order.

## Removed / renamed

- `BlockRenderDispatcher` — **removed**
- `ItemRenderer` — **removed**
- `RenderType` → `RenderTypes`
- Core shader JSON → `RenderPipeline`; depth → `DepthStencilState` (`DepthTestFunction` → `CompareOp`); blend/colour → `ColorTargetState`; `BlendOp` removed
- `TRIPWIRE_BLOCK` / `TRIPWIRE_TERRAIN` pipelines removed
- `SpriteGetter` → `MaterialBaker`; raw texture locations → `Material` / `Material$Baked`
- `BakedQuad` is no longer `int[]`-based; light/tint/overlay consolidated into `QuadInstance`; `putBulkData` → `putBlockBakedQuad` / `putBakedQuad`; `ModelBaker.PartCache` caches vectors
- `ItemModel` / `BlockModel` accept a `Transformation`; `$Unbaked#bake` takes the parent `Matrix4fc`
- `RenderHighlightEvent` removed → `ExtractBlockOutlineRenderStateEvent`
- Model JSON `neoforge_data` dropped `block_light`/`sky_light`; use `light_emission`

## `MutableQuad` (NeoForge helper)

Mutable `BakedQuad` representation for building/modifying quads:

- `setCubeFaceFromSpriteCoords(...)` — face positions from a 2D sprite coordinate system
- `setCubeFace(...)` — 3D cube face from the cube extent
- `bakeUvsFromPosition(...)` — vanilla-style UV baking, with optional transforms
- `recalculateWinding()` — reorder vertices to match vanilla AO expectations for axis-aligned quads
- `setSpriteAndMoveUv(...)` — swap sprite and remap atlas UVs automatically

## Entity renderers

```java
public class MyEntityRenderer extends EntityRenderer<MyEntity, MyRenderState> {
    @Override
    public MyRenderState createRenderState() { return new MyRenderState(); }

    @Override
    public void extractRenderState(MyEntity entity, MyRenderState state, float partialTick) { /* ... */ }

    @Override
    public void submit(MyRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        collector.submitModel(poseStack, myModel, state);
    }

    // Only LivingEntityRenderer requires this. EntityRenderer no longer has getTextureLocation.
    @Override
    public Identifier getTextureLocation(MyRenderState state) { return TEXTURE; }
}
```

Registration and layers:

```java
@SubscribeEvent  // mod event bus
static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(MY_ENTITY_TYPE.get(), MyEntityRenderer::new);
}

@SubscribeEvent
static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.add(MyEntityModel.MY_LAYER, MyEntityModel::createBodyLayer);
}
```

Other events: `EntityRenderersEvent.AddLayers`, `RenderLayerParent` (renamed to `RenderLayer` in 26.1), `BlockEntityRendererProvider.Context`.

## Block entity renderers

`BlockEntityRenderer` is now `implements`ed, with no abstract base:

```java
public class MyBER implements BlockEntityRenderer<MyBlockEntity, MyBlockEntityRenderState> {
    @Override
    public MyBlockEntityRenderState createRenderState() { return new MyBlockEntityRenderState(); }

    @Override
    public void extractRenderState(MyBlockEntity be, MyBlockEntityRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay overlay) { /* ... */ }

    @Override
    public void submit(MyBlockEntityRenderState state, SubmitNodeCollector collector, PoseStack poseStack,
                       Vec3 cameraPos) { /* ... */ }

    // register via:
    // event.registerBlockEntityRenderer(MY_BLOCK_ENTITY.get(), MyBER::new);
}
```

Render states extend `BlockEntityRenderState`.

## NeoForge render-state modifiers

- `ContextKey<T>`
- `RegisterRenderStateModifiersEvent` — `registerEntityModifier`, `registerAvatarEntityModifier`
- `AvatarRenderStateModifier`
- On a state: `setRenderData` / `getRenderDataOrThrow`

This is the clean, non-mixin way to add data to vanilla render states.

## Animations

NeoForge JSON animations live at `assets/<ns>/neoforge/animations/entity/<path>.json`, loaded via `Model.getAnimation(Identifier)` → `AnimationHolder` → `.bake(root)`.

## GUI / screens

- `GuiGraphics` → `GuiGraphicsExtractor`
- `Screen#render` → `Screen#extractRenderState`
- `Screen#renderBackground` → `Screen#extractBackground`
- `AbstractContainerScreen#renderLabels` → `AbstractContainerScreen#extractLabels`
- `AbstractWidget#renderWidget` → `AbstractWidget#extractWidgetRenderState`
- Method names drop `draw*`/`render*`/`submit*` prefixes and `*RenderState` suffixes: `renderOutline` → `outline`, `submitEntityRenderState` → `entity`, `hline` → `horizontalLine`, `*String*` → `*Text*`
- `imageWidth`/`imageHeight` are `final` constructor params (defaults 176×166): `super(menu, inv, title, 256, 256)`
- Do **not** override `render` in `AbstractContainerScreen` subclasses — the chain now calls `renderTooltip` at the end
- `ClickType` → `ContainerInput`
- Colours need mandatory alpha: `0xffffff` → `0xffffffff`
- `blitSprite` uses `RenderPipelines.GUI_TEXTURE`; `blit` takes a `Function<Identifier, RenderType>` as its first argument

> The docs give naming patterns and a handful of examples, not a full `GuiGraphicsExtractor` method table. Enumerate real method names from the decompiled sources before writing a screen.

## Practical rule for a QoL mod

Prefer, in order:
1. `RegisterRenderStateModifiersEvent` for read-only extra data on vanilla states.
2. A vanilla-facing `Screen` subclass with `extractRenderState` + `extractBackground` overrides.
3. Mixins only when there is no event or extension point at all — and keep them minimal, since the render pipeline refactor makes mixin targets especially fragile.
