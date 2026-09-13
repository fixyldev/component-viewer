package dev.fixyl.componentviewer.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.InteractionResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fixyl.componentviewer.ComponentViewer;
import dev.fixyl.componentviewer.control.keyboard.Keyboard.Action;

@Mixin(value = MouseHandler.class, priority = Integer.MIN_VALUE)
public final class MouseHandlerMixin {

    private MouseHandlerMixin() {}

    @Inject(method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", at = @At(value = "HEAD"))
    private void onButton(long windowHandle, MouseButtonInfo mouseButtonInfo, int action, CallbackInfo callback) {
        if (windowHandle == 0L || windowHandle != Minecraft.getInstance().getWindow().handle()) {
            return;
        }

        ComponentViewer.dispatchEventSafely(dispatcher ->
            dispatcher.invokeButtonInputEvent(mouseButtonInfo, Action.fromInt(action))
        );
    }

    @Inject(method = "onScroll(JDD)V", at = @At(value = "HEAD"), cancellable = true)
    private void onScroll(long windowHandle, double xOffset, double yOffset, CallbackInfo callback) {
        Minecraft minecraftClient = Minecraft.getInstance();

        if (windowHandle != minecraftClient.getWindow().handle()) {
            return;
        }

        InteractionResult result = ComponentViewer.dispatchEventWithResultSafely(
            dispatcher -> dispatcher.invokeMouseScrollEvent(xOffset, yOffset)
        ).orElse(InteractionResult.PASS);

        if (result == InteractionResult.SUCCESS) {
            minecraftClient.getFramerateLimitTracker().onInputReceived();
            callback.cancel();
        }
    }
}
