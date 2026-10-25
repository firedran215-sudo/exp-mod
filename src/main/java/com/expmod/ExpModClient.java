package com.expmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ExpModClient implements ClientModInitializer {
    public static final String MOD_ID = "expmod";

    /** Klawisz, który mod "klika" po osiągnięciu poziomu: - (minus). */
    private static final int AUTO_KEY = GLFW.GLFW_KEY_MINUS;

    // Ustawienia (zapisywane do config/expmod.properties)
    public static boolean hudEnabled = true;
    public static boolean autoEnabled = false;
    public static int targetLevel = 32;      // poziom, przy którym mod ma zadziałać
    public static int clickCount = 4;        // ile razy kliknąć
    public static int intervalTenths = 5;    // odstęp w dziesiątych sekundy (5 = 0,5 s)

    private static int lastLevel = -1;
    private static int pressesLeft = 0;
    private static long nextPressAt = 0;
    private static boolean releasePending = false;
    private static KeyBinding openGuiKey;

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("expmod.properties");
    }

    public static void load() {
        try {
            Path path = configPath();
            if (!Files.exists(path)) return;
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            }
            hudEnabled = Boolean.parseBoolean(props.getProperty("hud", "true"));
            autoEnabled = Boolean.parseBoolean(props.getProperty("auto", "false"));
            targetLevel = Integer.parseInt(props.getProperty("level", "32"));
            clickCount = Integer.parseInt(props.getProperty("count", "4"));
            intervalTenths = Integer.parseInt(props.getProperty("interval", "5"));
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Properties props = new Properties();
            props.setProperty("hud", String.valueOf(hudEnabled));
            props.setProperty("auto", String.valueOf(autoEnabled));
            props.setProperty("level", String.valueOf(targetLevel));
            props.setProperty("count", String.valueOf(clickCount));
            props.setProperty("interval", String.valueOf(intervalTenths));
            try (OutputStream out = Files.newOutputStream(configPath())) {
                props.store(out, "Exp Mod");
            }
        } catch (Exception ignored) {
        }
    }

    /** Symuluje wciśnięcie / puszczenie klawisza tak, jakby zrobił to gracz. */
    private static void sendKey(MinecraftClient client, int action) {
        long window = client.getWindow().getHandle();
        int scancode = GLFW.glfwGetKeyScancode(AUTO_KEY);
        client.keyboard.onKey(window, AUTO_KEY, scancode, action, 0);
    }

    @Override
    public void onInitializeClient() {
        load();

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

            // Puszczenie klawisza wciśniętego w poprzednim ticku
            if (releasePending) {
                sendKey(client, GLFW.GLFW_RELEASE);
                releasePending = false;
            }

            ClientPlayerEntity p = client.player;
            if (p == null) {
                lastLevel = -1;
                pressesLeft = 0;
                return;
            }

            long now = System.currentTimeMillis();
            int lvl = p.experienceLevel;

            // Start serii: tylko w momencie PRZEKROCZENIA wybranego poziomu
            if (autoEnabled && lastLevel >= 0 && lastLevel < targetLevel && lvl >= targetLevel) {
                pressesLeft = clickCount;
                nextPressAt = now;
            }
            lastLevel = lvl;

            // Kolejne kliknięcia co ustawiony odstęp (tylko gdy nie ma otwartego ekranu)
            if (pressesLeft > 0 && now >= nextPressAt && client.currentScreen == null) {
                sendKey(client, GLFW.GLFW_PRESS);
                releasePending = true;
                pressesLeft--;
                nextPressAt = now + intervalTenths * 100L;
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity p = mc.player;
            if (!hudEnabled || p == null || mc.options.hudHidden) return;
            String text = "EXP: lvl " + p.experienceLevel + " (" + (int) (p.experienceProgress * 100) + "%)";
            context.drawTextWithShadow(mc.textRenderer, text, 6, 6, 0xFF55FF55);
        });
    }
}
