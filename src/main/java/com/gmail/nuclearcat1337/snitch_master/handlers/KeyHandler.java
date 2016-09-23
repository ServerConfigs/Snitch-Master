package com.gmail.nuclearcat1337.snitch_master.handlers;

import com.gmail.nuclearcat1337.snitch_master.SnitchMaster;
import com.gmail.nuclearcat1337.snitch_master.gui.MainGUI;
import net.java.games.input.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

/**
 * Created by Mr_Little_Kitty on 6/30/2016.
 */
public class KeyHandler
{
    public KeyBinding snitchMasterMainGUI = new KeyBinding("Snitch Master Settings", Keyboard.KEY_V, "Snitch Master");
    public KeyBinding toggleAllRender = new KeyBinding("Toggle Render Snitch Lists", Keyboard.KEY_N, "Snitch Master");


    private SnitchMaster snitchMaster;

    public KeyHandler(SnitchMaster snitchMaster)
    {
        this.snitchMaster = snitchMaster;
        ClientRegistry.registerKeyBinding(snitchMasterMainGUI);
        ClientRegistry.registerKeyBinding(toggleAllRender);
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event)
    {
        if(snitchMasterMainGUI.isPressed())
        {
            Minecraft.getMinecraft().displayGuiScreen(new MainGUI(snitchMaster));
        }
        if(toggleAllRender.isPressed())
        {
            snitchMaster.getSnitchLists().toggleUniversalRender();
        }
    }
}
