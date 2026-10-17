package com.example.cloud;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class CloudClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("cloud", "main"));
    private static KeyMapping toggleKey;
    private static KeyMapping menuKey;

    private static boolean wasOpen = false;
    private static boolean done = false;
    private static int timer = 0;

    @Override
    public void onInitializeClient() {
        CloudConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cloud.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cloud.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_INSERT, CATEGORY));

        // /cloud - вкл/выкл, /cloud delay <0-20> - задержка в тиках
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("cloud")
                .executes(ctx -> {
                    CloudConfig.enabled = !CloudConfig.enabled;
                    CloudConfig.save();
                    ctx.getSource().sendFeedback(Component.literal("Cloud: " + (CloudConfig.enabled ? "ВКЛ" : "ВЫКЛ")));
                    return 1;
                })
                .then(ClientCommandManager.literal("delay")
                    .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, 20))
                        .executes(ctx -> {
                            CloudConfig.delay = IntegerArgumentType.getInteger(ctx, "ticks");
                            CloudConfig.save();
                            ctx.getSource().sendFeedback(Component.literal(
                                    "Cloud: задержка " + CloudConfig.delay + " тик."));
                            return 1;
                        })))));

        ClientTickEvents.END_CLIENT_TICK.register(CloudClient::tick);
    }

    private static void tick(Minecraft mc) {
        while (toggleKey.consumeClick()) {
            CloudConfig.enabled = !CloudConfig.enabled;
            CloudConfig.save();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.literal("Cloud: " + (CloudConfig.enabled ? "ВКЛ" : "ВЫКЛ")), true);
            }
        }
        while (menuKey.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new CloudScreen(null));
        }

        if (!CloudConfig.enabled || mc.player == null || mc.gameMode == null) {
            wasOpen = false;
            return;
        }

        if (mc.screen instanceof InventoryScreen) {
            if (!wasOpen) {            // инвентарь только что открыли
                wasOpen = true;
                timer = 0;
                done = false;
            }
            if (!done) {
                if (timer >= CloudConfig.delay) {
                    swapTotemToOffhand(mc);
                    done = true;
                } else {
                    timer++;
                }
            }
        } else {
            wasOpen = false;
        }
    }

    private static void swapTotemToOffhand(Minecraft mc) {
        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return; // уже в левой руке

        AbstractContainerMenu menu = mc.player.inventoryMenu;
        // слоты инвентаря игрока: 9..35 - сумка, 36..44 - хотбар
        for (int i = 9; i <= 44; i++) {
            if (menu.getSlot(i).getItem().is(Items.TOTEM_OF_UNDYING)) {
                // SWAP с кнопкой 40 = обмен с левой рукой
                mc.gameMode.handleInventoryMouseClick(menu.containerId, i, 40, ClickType.SWAP, mc.player);
                return;
            }
        }
    }
}
