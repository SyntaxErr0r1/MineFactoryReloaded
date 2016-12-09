package powercrystals.minefactoryreloaded.block.decor;

import powercrystals.minefactoryreloaded.block.BlockFactory;

public class BlockFactoryDecoration extends BlockFactory
{
	public static String[] _names = new String [] { null, "prc" };
	
	public BlockFactoryDecoration() {
		
		super(0.5f);
		setUnlocalizedName("mfr.machineblock");
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		icons = new IIcon[_names.length];
		bottomIcon = ir.registerIcon("minefactoryreloaded:machines/tile.mfr.machine.0.bottom");
		blockIcon = ir.registerIcon("minefactoryreloaded:machines/tile.mfr.machine.0.active.side");
		topIcon = ir.registerIcon("minefactoryreloaded:machines/tile.mfr.machine.0.top");
		for (int i = _names.length; i --> 1; )
			icons[i] = ir.registerIcon("minefactoryreloaded:tile.mfr.machineblock." + _names[i]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if (meta > 0)
			return icons[Math.min(Math.max(meta, 1), 15)];

		switch (side)
		{
		case 0:
			return bottomIcon;
		case 1:
			return topIcon;
		default:
			return blockIcon;
		}
	}
*/
}
