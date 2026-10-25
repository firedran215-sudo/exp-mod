package com.expmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;

public class ExpModClient implements ClientModInitializer {
    public static final String MOD_ID = "expmod";

    /** Czy pokazywać małe HUD z expem w rogu ekranu. */
    public static boolean hudEnabled = true;

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        // Klawisz "=" (można zmienić w Opcje -> Sterowanie)
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.expmod.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_EQUAL,
                "category.expmod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new ExpScreen());
                }
            }
        });

        // Prosty HUD z poziomem expa
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity p = mc.player;
            if (!hudEnabled || p == null || mc.options.hudHidden) return;
            String text = "EXP: lvl " + p.experienceLevel + " (" + (int) (p.experienceProgress * 100) + "%)";
            context.drawTextWithShadow(mc.textRenderer, text, 6, 6, 0xFF55FF55);
        });
    }
}
