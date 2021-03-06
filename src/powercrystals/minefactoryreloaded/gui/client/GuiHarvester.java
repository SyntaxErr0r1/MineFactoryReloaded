package powercrystals.minefactoryreloaded.gui.client;

import net.minecraft.client.gui.GuiButton;

import powercrystals.minefactoryreloaded.gui.container.ContainerHarvester;
import powercrystals.minefactoryreloaded.net.MFRPacket;
import powercrystals.minefactoryreloaded.net.Packets;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityHarvester;

public class GuiHarvester extends GuiUpgradeable {

	private TileEntityHarvester _harvester;

	private GuiButton _settingSilkTouch;
	private GuiButton _settingSmallShrooms;

	private static final String _silkTouchText = "Shear Leaves: ";
	private static final String _smallShroomsText = "Small Shrooms: ";

	public GuiHarvester(ContainerHarvester container, TileEntityHarvester te) {

		super(container, te);
		_harvester = te;
	}

	@Override
	public void initGui() {

		super.initGui();

		int xOffset = (this.width - this.xSize) / 2;
		int yOffset = (this.height - this.ySize) / 2;

		_settingSilkTouch = new GuiButton(1, xOffset + 7, yOffset + 14, 110, 20, _silkTouchText);
		_settingSmallShrooms = new GuiButton(2, xOffset + 7, yOffset + 34, 110, 20, _smallShroomsText);
		//new GuiButton(3, xOffset + 7, yOffset + 54, 110, 20, );

		buttonList.add(_settingSilkTouch);
		buttonList.add(_settingSmallShrooms);
	}

	@Override
	protected void updateElementInformation() {

		_settingSilkTouch.displayString = _silkTouchText + getSettingText("silkTouch");
		_settingSmallShrooms.displayString = _smallShroomsText + getSettingText("harvestSmallMushrooms");
	}

	@Override
	protected void actionPerformed(GuiButton button) {

		if (button.id == 1) {
			MFRPacket.sendHarvesterButtonToServer(_tileEntity, "silkTouch", getNewSettingValue("silkTouch"));
		} else if (button.id == 2) {
			MFRPacket.sendHarvesterButtonToServer(_tileEntity, "harvestSmallMushrooms", getNewSettingValue("harvestSmallMushrooms"));
		} else if (button.id == 3) {
			//PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(MineFactoryReloadedCore.modNetworkChannel, Packets.HarvesterButton,
			//		new Object[] { _harvester.xCoord, _harvester.yCoord, _harvester.zCoord, "", getNewSettingValue("") }));
		}
	}

	private String getSettingText(String setting) {

		return _harvester.getSettings().get(setting) == Boolean.TRUE ? "Yes" : "No";
	}

	private Boolean getNewSettingValue(String setting) {

		return _harvester.getSettings().get(setting) == Boolean.TRUE ? false : true;
	}

}
