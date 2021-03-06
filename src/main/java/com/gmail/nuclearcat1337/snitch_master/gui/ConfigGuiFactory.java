package com.gmail.nuclearcat1337.snitch_master.gui;

import com.gmail.nuclearcat1337.snitch_master.gui.screens.SettingsGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class ConfigGuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraft) {

	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new SettingsGui(parentScreen);
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
}
