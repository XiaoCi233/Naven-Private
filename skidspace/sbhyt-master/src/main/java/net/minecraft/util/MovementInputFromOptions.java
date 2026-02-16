package net.minecraft.util;

import com.surface.Wrapper;
import com.surface.events.EventMovementInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class MovementInputFromOptions extends MovementInput
{
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn)
    {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState()
    {
        if (Minecraft.getMinecraft().currentScreen != null && !(Minecraft.getMinecraft().currentScreen instanceof GuiChat) && Wrapper.Instance.getModManager().getModFromName("Screen Move").getState()) {
            this.moveStrafe = 0.0F;
            this.moveForward = 0.0F;

            if (this.gameSettings.keyBindForward.pressed || Keyboard.isKeyDown(this.gameSettings.keyBindForward.getKeyCode())) {
                this.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(this.gameSettings.keyBindForward.getKeyCode());
                ++this.moveForward;
            }

            if (this.gameSettings.keyBindBack.pressed || Keyboard.isKeyDown(this.gameSettings.keyBindBack.getKeyCode())) {
                this.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown(this.gameSettings.keyBindBack.getKeyCode());
                --this.moveForward;
            }

            if (this.gameSettings.keyBindLeft.pressed || Keyboard.isKeyDown(this.gameSettings.keyBindLeft.getKeyCode())) {
                this.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown(this.gameSettings.keyBindLeft.getKeyCode());
                ++this.moveStrafe;
            }

            if (this.gameSettings.keyBindRight.pressed || Keyboard.isKeyDown(this.gameSettings.keyBindRight.getKeyCode())) {
                this.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown(this.gameSettings.keyBindRight.getKeyCode());
                --this.moveStrafe;
            }

            this.jump = this.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(this.gameSettings.keyBindJump.getKeyCode());
        } else {
            this.moveStrafe = 0.0F;
            this.moveForward = 0.0F;

            if (this.gameSettings.keyBindForward.isKeyDown()) {
                ++this.moveForward;
            }

            if (this.gameSettings.keyBindBack.isKeyDown()) {
                --this.moveForward;
            }

            if (this.gameSettings.keyBindLeft.isKeyDown()) {
                ++this.moveStrafe;
            }

            if (this.gameSettings.keyBindRight.isKeyDown()) {
                --this.moveStrafe;
            }

            this.jump = this.gameSettings.keyBindJump.isKeyDown();
            this.sneak = this.gameSettings.keyBindSneak.isKeyDown();
        }

        final EventMovementInput event = new EventMovementInput(moveForward, moveStrafe, jump, sneak, 0.3D);
        Wrapper.Instance.getEventManager().call(event);

        final double sneakMultiplier = event.getSneakSlowDownMultiplier();
        this.moveForward = event.getForward();
        this.moveStrafe = event.getStrafe();
        this.jump = event.isJump();
        this.sneak = event.isSneak();

        if (this.sneak)
        {
            this.moveStrafe = (float) ((double) this.moveStrafe * sneakMultiplier);
            this.moveForward = (float) ((double) this.moveForward * sneakMultiplier);
        }
    }
}
