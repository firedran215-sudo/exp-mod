package com.expmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

public class ExpScreen extends Screen {

    public ExpScreen() {
        super(Text.literal("Exp Mod"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int w = 200;
        int x = cx - w / 2;

        this.addDrawableChild(ButtonWidget.builder(autoLabel(), btn -> {
            ExpModClient.autoEnabled = !ExpModClient.autoEnabled;
            btn.setMessage(autoLabel());
        }).dimensions(x, cy - 25, w, 20).build());

        this.addDrawableChild(new IntSlider(x, cy - 2, w, 20, 1, 100, ExpModClient.targetLevel,
                v -> "Poziom: " + v, v -> ExpModClient.targetLevel = v));

        this.addDrawableChild(new IntSlider(x, cy + 21, w, 20, 1, 10, ExpModClient.clickCount,
                v -> "Ilość kliknięć (-): " + v, v -> ExpModClient.clickCount = v));

        this.addDrawableChild(new IntSlider(x, cy + 44, w, 20, 1, 20, ExpModClient.intervalTenths,
                v -> "Odstęp: " + (v / 10.0) + " s", v -> ExpModClient.intervalTenths = v));

        this.addDrawableChild(ButtonWidget.builder(hudLabel(), btn -> {
            ExpModClient.hudEnabled = !ExpModClient.hudEnabled;
            btn.setMessage(hudLabel());
        }).dimensions(x, cy + 67, w, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Zamknij"), btn -> this.close())
                .dimensions(x, cy + 90, w, 20).build());
    }

    private Text autoLabel() {
        return Text.literal("Auto klik: " + (ExpModClient.autoEnabled ? "WŁĄCZONE" : "WYŁĄCZONE"));
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

        context.drawCenteredTextWithShadow(this.textRenderer, "EXP MOD", cx, cy - 110, 0xFFFFFF55);

        if (p == null) return;

        int next = p.getNextLevelExperience();
        int have = (int) (p.experienceProgress * next);

        context.drawCenteredTextWithShadow(this.textRenderer, "Poziom: " + p.experienceLevel, cx, cy - 90, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Suma punktów expa: " + p.totalExperience, cx, cy - 78, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Do następnego poziomu: " + have + " / " + next, cx, cy - 66, 0xFFAAAAAA);

        int barW = 200, barH = 10;
        int x = cx - barW / 2;
        int y = cy - 48;
        context.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, 0xFF000000);
        context.fill(x, y, x + barW, y + barH, 0xFF333333);
        context.fill(x, y, x + (int) (barW * p.experienceProgress), y + barH, 0xFF55FF55);
    }

    @Override
    public void removed() {
        ExpModClient.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Suwak wartości całkowitych od min do max. */
    private static class IntSlider extends SliderWidget {
        private final int min;
        private final int max;
        private final IntFunction<String> format;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int width, int height, int min, int max, int current,
                  IntFunction<String> format, IntConsumer onChange) {
            super(x, y, width, height, Text.empty(), (double) (current - min) / (max - min));
            this.min = min;
            this.max = max;
            this.format = format;
            this.onChange = onChange;
            updateMessage();
        }

        private int val() {
            return min + (int) Math.round(this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(format.apply(val())));
        }

        @Override
        protected void applyValue() {
            onChange.accept(val());
        }
    }
}
