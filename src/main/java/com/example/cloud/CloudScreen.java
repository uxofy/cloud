package com.example.cloud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CloudScreen extends Screen {
    private final Screen parent;
    private Button delayButton;

    public CloudScreen(Screen parent) {
        super(Component.literal("Cloud"));
        this.parent = parent;
    }

    private static String onOff(boolean v) { return v ? "ВКЛ" : "ВЫКЛ"; }

    private Component delayText() {
        return Component.literal("Задержка: " + CloudConfig.delay + " тик. (" + (CloudConfig.delay * 50) + " мс)");
    }

    @Override
    protected void init() {
        int w = 240, h = 20, x = this.width / 2 - w / 2, y = this.height / 4, gap = 26;

        addRenderableWidget(Button.builder(Component.literal("Мод: " + onOff(CloudConfig.enabled)), b -> {
            CloudConfig.enabled = !CloudConfig.enabled;
            b.setMessage(Component.literal("Мод: " + onOff(CloudConfig.enabled)));
        }).bounds(x, y, w, h).build());

        // [-] [Задержка] [+]
        addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            CloudConfig.delay = Math.max(0, CloudConfig.delay - 1);
            delayButton.setMessage(delayText());
        }).bounds(x, y + gap, 30, h).build());

        delayButton = Button.builder(delayText(), b -> {
            CloudConfig.delay = 2; // сброс на 2
            b.setMessage(delayText());
        }).bounds(x + 34, y + gap, w - 68, h).build();
        addRenderableWidget(delayButton);

        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            CloudConfig.delay = Math.min(20, CloudConfig.delay + 1);
            delayButton.setMessage(delayText());
        }).bounds(x + w - 30, y + gap, 30, h).build());

        addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(x, y + gap * 2 + 8, w, h).build());
    }

    private void centered(GuiGraphics g, String text, int cx, int y, int color) {
        g.drawString(this.font, text, cx - this.font.width(text) / 2, y, color, true);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        centered(g, "Cloud", this.width / 2, 20, 0xFFFFFFFF);
        centered(g, "Клавиши: Настройки > Управление > Cloud", this.width / 2, this.height - 30, 0xFFAAAAAA);
    }

    @Override
    public void onClose() {
        CloudConfig.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}
