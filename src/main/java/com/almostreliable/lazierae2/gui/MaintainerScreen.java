package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
import com.almostreliable.lazierae2.gui.control.MaintainerControl;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MaintainerScreen extends GenericScreen<MaintainerMenu> {

    private static final ResourceLocation TEXTURE = TextUtil.getRL("textures/gui/maintainer.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 211;
    public final MaintainerControl maintainerControl;

    @SuppressWarnings({"AssignmentToSuperclassField", "ThisEscapedInObjectConstruction"})
    public MaintainerScreen(
        MaintainerMenu menu, Inventory inventory, Component ignoredTitle
    ) {
        super(menu, inventory);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
        maintainerControl = new MaintainerControl(this, menu.getRequestSlots());
    }

    @Override
    protected void init() {
        super.init();
        addRenderables(maintainerControl.init());
    }

    @Override
    protected void renderLabels(PoseStack stack, int mX, int mY) {
        drawCenteredString(stack, font, title, TEXTURE_WIDTH / 2, -12, 16_777_215);
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mX, int mY) {
        // background texture
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (InputConstants.getKey("key.keyboard.tab").getValue() == keyCode) {
            // if tab is pressed, let the widget handle it
            return getFocused() != null && getFocused().keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
