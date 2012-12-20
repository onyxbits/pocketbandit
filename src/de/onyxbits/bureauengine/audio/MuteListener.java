package de.onyxbits.bureauengine.audio;

import java.util.EventListener;

/**
 * A MuteListener gets informed about the player's wishes to mute
 * sound and/or music.
 */
public interface MuteListener extends EventListener {

  /**
   * Invoked when the user (un-)mutes sound
   * @param mute true if sound is to be muted
   */
  public void muteSound(boolean mute);
  
  /**
   * Invoked when the user (un-)mutes music
   * @param mute true if music is to be muted
   */
  public void muteMusic(boolean mute);
}