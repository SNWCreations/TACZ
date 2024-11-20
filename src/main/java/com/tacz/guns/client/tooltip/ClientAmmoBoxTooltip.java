package com.tacz.guns.client.tooltip;

import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import com.tacz.guns.item.GunTooltipPart;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class ClientAmmoBoxTooltip implements ClientTooltipComponent {
    private final ItemStack ammo;
    private final Component count;
    private final Component ammoName;

    public ClientAmmoBoxTooltip(AmmoBoxTooltip tooltip) {
        this.ammo = tooltip.getAmmo();
        ItemStack ammoBox = tooltip.getAmmoBox();
        if (ammoBox.getItem() instanceof IAmmoBox box && box.isCreative(ammoBox)) {
            this.count = Component.literal("∞");
        } else {
            this.count = Component.translatable("tooltip.tacz.ammo_box.count", tooltip.getCount());
        }
        this.ammoName = this.ammo.getHoverName();
    }

    @Override
    public int getHeight() {
        if (GunTooltipPart.hideFlagsPresent(ammo)) {
            return 0;
        }
        return 28;
    }

    @Override
    public int getWidth(Font font) {
        if (GunTooltipPart.hideFlagsPresent(ammo)) {
            return 0;
        }
        return Math.max(font.width(ammoName), font.width(count)) + 22;
    }

    @Override
    public void renderText(Font font, int pX, int pY, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        if (GunTooltipPart.hideFlagsPresent(ammo)) {
            return;
        }
        font.drawInBatch(ammoName, pX + 20, pY + 4, 0xffaa00, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        font.drawInBatch(count, pX + 20, pY + 15, 0x666666, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        if (GunTooltipPart.hideFlagsPresent(ammo)) {
            return;
        }
        pGuiGraphics.renderItem(ammo, pX, pY + 5);
    }
}
