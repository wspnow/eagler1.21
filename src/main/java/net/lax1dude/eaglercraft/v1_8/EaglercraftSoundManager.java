package net.lax1dude.eaglercraft.v1_8;

import net.minecraft.client.Options;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class EaglercraftSoundManager {

    private final Options settings;
    private final SoundManager handler;

    public EaglercraftSoundManager(Options settings, SoundManager handler) {
        this.settings = settings;
        this.handler = handler;
    }

    public void unloadSoundSystem() {
    }

    public void reloadSoundSystem() {
    }

    public void setSoundSourceVolume(SoundSource category, float volume) {
    }

    public void stopAllSounds() {
    }

    public void pauseAllSounds() {
    }

    public void resumeAllSounds() {
    }

    public void updateAllSounds() {
    }

    public boolean isSoundPlaying(SoundInstance sound) {
        return false;
    }

    public void stopSound(SoundInstance sound) {
    }

    public void playSound(SoundInstance sound) {
    }

    public void playDelayedSound(SoundInstance sound, int delay) {
    }

    public void setListener(Player player, float partialTicks) {
    }
}
