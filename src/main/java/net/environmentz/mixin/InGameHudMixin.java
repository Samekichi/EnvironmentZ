package net.environmentz.mixin;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.Environment;
import net.environmentz.effect.ColdEffect;
import net.environmentz.init.ConfigInit;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
  @Shadow
  @Final
  @Mutable
  private final MinecraftClient client;

  private static final Identifier FREEZING_ICON = new Identifier("environmentz:textures/misc/coldness.png");
  private float smoothRendering;

  public InGameHudMixin(MinecraftClient client) {
    this.client = client;
  }

  @Inject(method = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/util/math/MatrixStack;)V"))
  private void renderFreezingIcon(MatrixStack matrixStack, float f, CallbackInfo info) {
    PlayerEntity playerEntity = client.player;
    if (!playerEntity.isCreative()) {
      if (playerEntity.world.getBiome(playerEntity.getBlockPos()).getTemperature() <= 0.0F
          && !ColdEffect.hasWarmClothing(playerEntity) && !ColdEffect.isWarmBlockNearBy(playerEntity)) {
        if (smoothRendering < 0.995F) {
          smoothRendering = smoothRendering + 0.0025F;
        }
        this.renderFreezingIconOverlay(matrixStack, smoothRendering);
      } else if (smoothRendering > 0.0F) {
        this.renderFreezingIconOverlay(matrixStack, smoothRendering);
        smoothRendering = smoothRendering - 0.01F;
      }

    }
  }

  private void renderFreezingIconOverlay(MatrixStack matrixStack, float smooth) {
    int scaledWidth = this.client.getWindow().getScaledWidth();
    int scaledHeight = this.client.getWindow().getScaledHeight();
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, smooth);
    this.client.getTextureManager().bindTexture(FREEZING_ICON);
    DrawableHelper.drawTexture(matrixStack, (scaledWidth / 2) - ConfigInit.CONFIG.freeze_icon_x,
        scaledHeight - ConfigInit.CONFIG.freeze_icon_y, 0.0F, 0.0F, 13, 13, 13, 13);
  }

}