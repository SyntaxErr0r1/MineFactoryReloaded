package powercrystals.minefactoryreloaded;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.Point;
import powercrystals.minefactoryreloaded.core.IHarvestAreaContainer;
import powercrystals.minefactoryreloaded.entity.EntityPinkSlime;
import powercrystals.minefactoryreloaded.gui.container.ContainerAutoBrewer;
import powercrystals.minefactoryreloaded.gui.container.ContainerAutoDisenchanter;
import powercrystals.minefactoryreloaded.gui.container.ContainerAutoJukebox;
import powercrystals.minefactoryreloaded.gui.container.ContainerFisher;
import powercrystals.minefactoryreloaded.gui.slot.SlotAcceptLaserFocus;
import powercrystals.minefactoryreloaded.gui.slot.SlotAcceptReusableSafariNet;
import powercrystals.minefactoryreloaded.gui.slot.SlotAcceptUpgrade;
import powercrystals.minefactoryreloaded.render.ModelHelper;
import powercrystals.minefactoryreloaded.render.block.BlockTankRenderer;
import powercrystals.minefactoryreloaded.render.block.PlasticPipeRenderer;
import powercrystals.minefactoryreloaded.render.block.RedNetCableRenderer;
import powercrystals.minefactoryreloaded.render.entity.EntityPinkSlimeRenderer;
import powercrystals.minefactoryreloaded.render.model.MFRModelLoader;
import powercrystals.minefactoryreloaded.render.tileentity.RedNetLogicRenderer;
import powercrystals.minefactoryreloaded.setup.MFRFluids;
import powercrystals.minefactoryreloaded.setup.MFRThings;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static powercrystals.minefactoryreloaded.setup.MFRThings.*;

@SideOnly(Side.CLIENT)
public class MineFactoryReloadedClient implements IResourceManagerReloadListener {

	public static MineFactoryReloadedClient instance;

	private static final ResourceLocation targetingBlue =
			new ResourceLocation(MineFactoryReloadedCore.hudFolder + "lockon_blue.png");
	private static final ResourceLocation targetingRed =
			new ResourceLocation(MineFactoryReloadedCore.hudFolder + "lockon_red.png");
	private static final int _lockonMax = 30;
	private static final int _lockonLostMax = 60;
	private int _lockonTicks = 0;
	private int _lockonLostTicks = 0;
	private Entity _lastEntityOver = null;
	@SuppressWarnings("unused")
	private static boolean gl14 = false;

	public static HashMap<BlockPos, Integer> prcPages = new HashMap<>();

	public static Set<IHarvestAreaContainer> _areaTileEntities = new LinkedHashSet<>();


	public static void preInit() {
		
		//fluids
		ModelHelper.registerModel(MFRFluids.milkLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "milk"));
		ModelHelper.registerModel(
				MFRFluids.sludgeLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "sludge"));
		ModelHelper.registerModel(
				MFRFluids.sewageLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "sewage"));
		ModelHelper.registerModel(
				MFRFluids.essenceLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "mob_essence"));
		ModelHelper.registerModel(
				MFRFluids.biofuelLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "biofuel"));
		ModelHelper.registerModel(MFRFluids.meatLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "meat"));
		ModelHelper.registerModel(
				MFRFluids.pinkSlimeLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "pink_slime"));
		ModelHelper.registerModel(
				MFRFluids.chocolateMilkLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "chocolate_milk"));
		ModelHelper.registerModel(
				MFRFluids.mushroomSoupLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "mushroom_soup"));
		ModelHelper.registerModel(
				MFRFluids.steamFluid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "steam"));

		RenderingRegistry.registerEntityRenderingHandler(EntityPinkSlime.class,
				manager -> new EntityPinkSlimeRenderer(manager, new ModelSlime(16), 0.25F));
		
		ModelLoaderRegistry.registerLoader(MFRModelLoader.INSTANCE);
	}

	public static void init() {

		instance = new MineFactoryReloadedClient();
		
		MinecraftForge.EVENT_BUS.register(instance);
		gl14 = GLContext.getCapabilities().OpenGL14; //TODO what is this used for? doesn't seem to have anything referring to it

		IReloadableResourceManager manager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
		manager.registerReloadListener(instance);
	}

	@Override
	public void onResourceManagerReload(IResourceManager p_110549_1_) {

	}

	@SubscribeEvent
	public void onPreTextureStitch(TextureStitchEvent.Pre evt) {

		registerFluidSprites(evt.getMap(), MFRFluids.milkLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.sludgeLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.sewageLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.essenceLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.biofuelLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.meatLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.pinkSlimeLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.chocolateMilkLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.mushroomSoupLiquid.getFluid());
		registerFluidSprites(evt.getMap(), MFRFluids.steamFluid.getFluid());

		evt.getMap().registerSprite(BlockTankRenderer.BOTTOM_TEXTURE_LOCATION);

		SlotAcceptReusableSafariNet.background = evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":items/gui/reusablenet"));
		ContainerAutoDisenchanter.background = evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":items/gui/book")).getIconName();
		ContainerAutoJukebox.background = evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":items/gui/record")).getIconName();
		SlotAcceptLaserFocus.background = evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":items/gui/laserfocus"));
		ContainerAutoBrewer.ingredient = evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":items/gui/netherwart")).getIconName();
		ContainerAutoBrewer.bottle = evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":items/gui/bottle")).getIconName();
		ContainerFisher.background = evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":items/gui/fishingrod")).getIconName();
		SlotAcceptUpgrade.background = evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":items/gui/upgrade"));

		PlasticPipeRenderer.setSprite(evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":blocks/tile.mfr.cable.plastic")));
		RedNetCableRenderer.setSprite(evt.getMap().registerSprite(new ResourceLocation(MineFactoryReloadedCore.modId + ":blocks/tile.mfr.cable.redstone")));
	}

	@SubscribeEvent
	public void onPostTextureStitch(TextureStitchEvent.Post evt) {

		RedNetLogicRenderer.updateUVT(evt.getMap().getAtlasSprite(RedNetLogicRenderer.textureLocation.toString()));
		BlockTankRenderer.updateSprites(evt.getMap());
	}

	private void registerFluidSprites(TextureMap textureMap, Fluid fluid) {
		if (fluid != null) {
			textureMap.registerSprite(fluid.getStill());
			textureMap.registerSprite(fluid.getFlowing());
		}
	}

	@SubscribeEvent
	public void onPlayerChangedDimension(Unload world) {

		if (world.getWorld().provider == null ||
				Minecraft.getMinecraft().thePlayer == null ||
				Minecraft.getMinecraft().thePlayer.worldObj == null ||
				Minecraft.getMinecraft().thePlayer.worldObj.provider == null) {
			return;
		}
		if (world.getWorld().provider.getDimension() == Minecraft.getMinecraft().thePlayer.worldObj.provider.getDimension()) {
			_areaTileEntities.clear();
			prcPages.clear();
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void tickEnd(RenderTickEvent evt) {

		if (evt.phase != Phase.END)
			return;
		renderHUD(evt.renderTickTime);
	}

	@SubscribeEvent
	public void tickStart(PlayerTickEvent evt) {

		if (evt.side != Side.CLIENT | evt.phase != Phase.START)
			return;

		if (holdsRocketLauncher(evt.player)) {
			Entity e = rayTrace();
			if (_lastEntityOver != null && _lastEntityOver.isDead) {
				_lastEntityOver = null;
				_lockonTicks = 0;
			} else if ((e == null || e != _lastEntityOver) && _lockonLostTicks > 0) {
				_lockonLostTicks--;
			} else if (e == null && _lockonLostTicks == 0) {
				_lockonTicks = 0;
				_lastEntityOver = null;
			} else if (_lastEntityOver == null) {
				_lastEntityOver = e;
			} else if (_lockonTicks < _lockonMax) {
				_lockonTicks++;
				if (_lockonTicks >= _lockonMax) {
					_lockonLostTicks = _lockonLostMax;
				}
			} else if (e != null && e == _lastEntityOver) {
				_lockonLostTicks = _lockonLostMax;
			}
		}
	}

	private void renderHUD(float partialTicks) {

		Minecraft mc = Minecraft.getMinecraft();

		if (mc.gameSettings.hideGUI)
			return;

		if (!mc.isGamePaused() && mc.currentScreen == null && holdsRocketLauncher(mc.thePlayer)) {
			ScaledResolution sr = new ScaledResolution(mc);
			Point center = new Point(sr.getScaledWidth() / 2, sr.getScaledHeight() / 2);

			if (MineFactoryReloadedClient.instance.getLockedEntity() != Integer.MIN_VALUE) {
				mc.renderEngine.bindTexture(targetingBlue);
			} else {
				mc.renderEngine.bindTexture(targetingRed);
			}

			GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.translate(center.getX(), center.getY(), 0);
			GlStateManager.rotate(((mc.theWorld.getTotalWorldTime() & 511) * 4) % 360 + partialTicks, 0, 0, 1);

			float distance = MineFactoryReloadedClient.instance.getLockTimeRemaining();

			drawLockonPart(center, distance, 0);
			drawLockonPart(center, distance, 90);
			drawLockonPart(center, distance, 180);
			drawLockonPart(center, distance, 270);

			GlStateManager.popMatrix();
		}
	}

	private boolean holdsRocketLauncher(EntityPlayer player) {

		return player != null && ((player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == MFRThings.rocketLauncherItem) ||
				(player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() == MFRThings.rocketLauncherItem));
	}

	private void drawLockonPart(Point center, float distanceFromCenter, int rotation) {

		GlStateManager.pushMatrix();

		GlStateManager.rotate(rotation, 0, 0, 1);
		GlStateManager.translate(-8, -13, 0);
		GlStateManager.translate(0, -distanceFromCenter, 0);

		GlStateManager.glBegin(GL11.GL_QUADS);
		GlStateManager.glTexCoord2f(0, 0);
		GL11.glVertex2i(0, 0);
		GlStateManager.glTexCoord2f(0, 1);
		GL11.glVertex2i(0, 16);
		GlStateManager.glTexCoord2f(1, 1);
		GL11.glVertex2i(16, 16);
		GlStateManager.glTexCoord2f(1, 0);
		GL11.glVertex2i(16, 0);
		GlStateManager.glEnd();

		GlStateManager.popMatrix();
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	// first to render, so everything else is overlayed
	public void renderWorldLast(RenderWorldLastEvent e) {

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player.inventory.getCurrentItem() == null ||
				!player.inventory.getCurrentItem().getItem().equals(factoryHammerItem)) {
			return;
		}

		float playerOffsetX = -(float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * e.getPartialTicks());
		float playerOffsetY = -(float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * e.getPartialTicks());
		float playerOffsetZ = -(float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * e.getPartialTicks());

		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.enableAlpha();
		GlStateManager.disableFog();
		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);

		for (IHarvestAreaContainer c : _areaTileEntities) {
			if (((TileEntity) c).isInvalid())
				continue;

			float r = colorFromCoord(c.getHAM().getOriginX(), 0xF8525888);
			float g = colorFromCoord(c.getHAM().getOriginY(), 0x85BDBD8C);
			float b = colorFromCoord(c.getHAM().getOriginZ(), 0x997696BF);

			GlStateManager.pushMatrix();
			GlStateManager.color(r, g, b, 0.4F);
			GlStateManager.translate(playerOffsetX, playerOffsetY, playerOffsetZ);
			renderAABB(c.getHAM().getHarvestArea().toAxisAlignedBB());
			GlStateManager.popMatrix();
		}

		GlStateManager.enableTexture2D();
	}

	private float colorFromCoord(int c, long h) {

		h = (h * c) + 0xBA;
		h ^= (h >>> 20) ^ (h >>> 12);
		h ^= (h >>> 7) ^ (h >>> 4);
		return ((h & 255) / 319f) + 0.1f; // odd values bound to 0.1 <= x < 0.9
	}

	public static void addTileToAreaList(IHarvestAreaContainer tile) {

		_areaTileEntities.add(tile);
	}

	public static void removeTileFromAreaList(IHarvestAreaContainer tile) {

		_areaTileEntities.remove(tile);
	}

	public int getLockedEntity() {

		if (_lastEntityOver != null && _lockonTicks >= _lockonMax) {
			return _lastEntityOver.getEntityId();
		}

		return Integer.MIN_VALUE;
	}

	public int getLockTimeRemaining() {

		if (_lastEntityOver != null && _lockonTicks >= _lockonMax) {
			return _lockonLostMax - _lockonLostTicks;
		} else {
			return (_lockonMax - _lockonTicks) * 2;
		}
	}

	private Entity rayTrace() {

		if (Minecraft.getMinecraft().getRenderViewEntity() == null || Minecraft.getMinecraft().theWorld == null) {
			return null;
		}

		double range = 64;
		Vec3d playerPos = Minecraft.getMinecraft().getRenderViewEntity().getPositionEyes(1);

		Vec3d playerLook = Minecraft.getMinecraft().getRenderViewEntity().getLook(1);
		Vec3d playerLookRel = playerPos.addVector(playerLook.xCoord * range, playerLook.yCoord * range, playerLook.zCoord * range);
		List<?> list = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABBExcludingEntity(
			Minecraft.getMinecraft().getRenderViewEntity(),
			Minecraft.getMinecraft().getRenderViewEntity().getEntityBoundingBox().addCoord(playerLook.xCoord * range, playerLook.yCoord * range,
				playerLook.zCoord * range)
					.expand(1, 1, 1));

		double entityDistTotal = range;
		Entity pointedEntity = null;
		for (int i = 0; i < list.size(); ++i) {
			Entity entity = (Entity) list.get(i);

			if (entity.canBeCollidedWith()) {
				double entitySize = entity.getCollisionBorderSize();
				AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(entitySize, entitySize, entitySize);
				RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(playerPos, playerLookRel);

				if (axisalignedbb.isVecInside(playerPos)) {
					if (0.0D < entityDistTotal || entityDistTotal == 0.0D) {
						pointedEntity = entity;
						entityDistTotal = 0.0D;
					}
				} else if (movingobjectposition != null) {
					double entityDist = playerPos.distanceTo(movingobjectposition.hitVec);

					if (entityDist < entityDistTotal || entityDistTotal == 0.0D) {
						pointedEntity = entity;
						entityDistTotal = entityDist;
					}
				}
			}
		}

		if (pointedEntity != null) {
			return pointedEntity;
		}
		return null;
	}

	public static void renderAABB(AxisAlignedBB par0AxisAlignedBB) {

		double eps = 0.006;

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION);
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();

		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();

		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();

		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();

		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();

		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		tessellator.draw();
	}

}
