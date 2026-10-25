package com.expmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class ExpScreen extends Screen {
    private ButtonWidget hudButton;

    public ExpScreen() {
        super(Text.literal("Exp Mod"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        hudButton = ButtonWidget.builder(hudLabel(), btn -> {
            ExpModClient.hudEnabled = !ExpModClient.hudEnabled;
            btn.setMessage(hudLabel());
        }).dimensions(cx - 100, cy + 30, 200, 20).build();
        this.addDrawableChild(hudButton);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Zamknij"), btn -> this.close())
                .dimensions(cx - 100, cy + 55, 200, 20).build());
    }

    private Text hudLabel() {
        return Text.literal("HUD z expem: " + (ExpModClient.hudEnabled ? "WŁĄCZONE" : "WYŁĄCZONE"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        int cx = this.width / 2;
        int cy = this.height / 2;

        context.drawCenteredTextWithShadow(this.textRenderer, "EXP MOD", cx, cy - 70, 0xFFFFFF55);

        if (p == null) return;

        int next = p.getNextLevelExperience();
        int have = (int) (p.experienceProgress * next);

        context.drawCenteredTextWithShadow(this.textRenderer, "Poziom: " + p.experienceLevel, cx, cy - 50, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Suma punktów expa: " + p.totalExperience, cx, cy - 38, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Do następnego poziomu: " + have + " / " + next, cx, cy - 26, 0xFFAAAAAA);

        // Pasek postępu
        int barW = 200, barH = 10;
        int x = cx - barW / 2;
        int y = cy - 8;
        context.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, 0xFF000000);
        context.fill(x, y, x + barW, y + barH, 0xFF333333);
        context.fill(x, y, x + (int) (barW * p.experienceProgress), y + barH, 0xFF55FF55);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
