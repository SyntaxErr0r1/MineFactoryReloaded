package powercrystals.minefactoryreloaded.item.tool;

import cofh.api.block.IBlockConfigGui;
import cofh.api.block.IBlockInfo;
import cofh.api.tileentity.ITileInfo;
import cofh.lib.util.helpers.ServerHelper;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetInfo;
import powercrystals.minefactoryreloaded.item.base.ItemMulti;
import powercrystals.minefactoryreloaded.render.ModelHelper;

public class ItemRedNetMeter extends ItemMulti {

	public static String[] _colorNames = new String[] { "White", "Orange", "Magenta", "LightBlue",
					"Yellow", "Lime", "Pink", "Gray", "LightGray", "Cyan", "Purple",
					"Blue", "Brown", "Green", "Red", "Black" };

	public ItemRedNetMeter() {
		setNames(null, "info", "debug");
		setUnlocalizedName("mfr.rednet.meter");
		setMaxStackSize(1);
		setRegistryName(MineFactoryReloadedCore.modId, "rednet_meter");
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
		if (stack.getItemDamage() != 2)
			return false;
		player.swingArm(hand);
		if (player.worldObj.isRemote)
			return true;
		player.addChatMessage(new TextComponentString("ID: " + EntityList.getEntityString(entity)));
		return true;
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world,
			BlockPos pos, EnumFacing hitSide, float hitX, float hitY, float hitZ, EnumHand hand) {
		boolean r = doItemThing(stack, player, world, pos, hitSide, hitX, hitY, hitZ);
		if (r) // HACK: forge is fucking stupid with this method
			ServerHelper.sendItemUsePacket(world, pos, hitSide, hand, hitX, hitY, hitZ);
		return r ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
	}

	public boolean doItemThing(ItemStack stack, EntityPlayer player, World world,
			BlockPos pos, EnumFacing hitSide, float hitX, float hitY, float hitZ) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		switch (stack.getItemDamage()) {
		case 2:
			ArrayList<ITextComponent> info = new ArrayList<ITextComponent>();
			if (block instanceof IBlockInfo) {
				if (ServerHelper.isClientWorld(world)) {
					info.add(new TextComponentString("-Client-"));
				} else {
					info.add(new TextComponentString("-Server-"));
				}
				((IBlockInfo) (block)).getBlockInfo(info, world, pos, hitSide, player, true);
				for (int i = 0; i < info.size(); i++) {
					player.addChatMessage(info.get(i));
				}
				return true;
			} else {
				TileEntity theTile = world.getTileEntity(pos);
				if (theTile instanceof ITileInfo) {
					if (ServerHelper.isServerWorld(world)) {
						((ITileInfo) theTile).getTileInfo(info, hitSide, player, player.isSneaking());
						for (int i = 0; i < info.size(); i++) {
							player.addChatMessage(info.get(i));
						}
					}
					return true;
				}
			}
			return false;
		case 1:
			if (ServerHelper.isClientWorld(world)) {
				if (block instanceof IBlockConfigGui || block instanceof IBlockInfo)
					return true;
				TileEntity theTile = world.getTileEntity(pos);
				return theTile instanceof ITileInfo;
			}
			info = new ArrayList<>();
			if (player.isSneaking() && block instanceof IBlockConfigGui) {
				if (((IBlockConfigGui)block).openConfigGui(world, pos, hitSide, player))
					return true;
			}
			if (block instanceof IBlockInfo) {
				((IBlockInfo) (block)).getBlockInfo(info, world, pos, hitSide, player, false);
				for (int i = 0; i < info.size(); i++) {
					player.addChatMessage(info.get(i));
				}
				return true;
			} else {
				TileEntity theTile = world.getTileEntity(pos);
				if (theTile instanceof ITileInfo) {
					if (ServerHelper.isServerWorld(world)) {
						((ITileInfo) theTile).getTileInfo(info, hitSide, player, false);
						for (int i = 0; i < info.size(); i++) {
							player.addChatMessage(info.get(i));
						}
					}
					return true;
				}
			}
			return false;
		case 0:
			if (ServerHelper.isClientWorld(world)) {
				return block instanceof IRedNetInfo || block.equals(Blocks.REDSTONE_WIRE);
			}
			info = new ArrayList<>();
			if (block.equals(Blocks.REDSTONE_WIRE)) {
				player.addChatMessage(new TextComponentTranslation("chat.info.mfr.rednet.meter.dustprefix")
						.appendText(": " + state.getValue(BlockRedstoneWire.POWER)));
			}
			else if (block instanceof IRedNetInfo) {
				((IRedNetInfo) (block)).getRedNetInfo(world, pos, hitSide, player, info);
				for (int i = 0; i < info.size(); i++) {
					player.addChatMessage(info.get(i));
				}
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {

		ModelHelper.registerModel(this, "tool", "variant=rednet_meter");
		ModelHelper.registerModel(this, 1, "tool", "variant=rednet_meter_info");
		ModelHelper.registerModel(this, 2, "tool", "variant=rednet_meter_debug");
	}
}
