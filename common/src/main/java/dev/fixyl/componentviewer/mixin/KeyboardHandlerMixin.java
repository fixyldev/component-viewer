package dev.fixyl.componentviewer.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fixyl.componentviewer.ComponentViewer;
import dev.fixyl.componentviewer.control.keyboard.Keyboard.Action;

@Mixin(value = KeyboardHandler.class)
public final class KeyboardHandlerMixin {

    private KeyboardHandlerMixin() {}

    @Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At(value = "HEAD"))
    private void keyPress(long windowHandle, int action, KeyEvent keyEvent, CallbackInfo callback) {
        if (windowHandle == 0L || windowHandle != Minecraft.getInstance().getWindow().handle()) {
            return;
        }

        ComponentViewer.dispatchEventSafely(dispatcher ->
            dispatcher.invokeKeyInputEvent(keyEvent, Action.fromInt(action))
        );
    }
}
