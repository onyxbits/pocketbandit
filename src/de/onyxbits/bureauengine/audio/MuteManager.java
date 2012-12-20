package de.onyxbits.bureauengine.audio;

import java.util.EventListener;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Preferences;


/**
 * Central class for allowing the user to individually mute music and/or
 * sound effects. Preference settings can automatically be synced with a
 * persistent storage.
 */
public class MuteManager {

  private boolean musicMuted;
  private boolean soundMuted;
  private Preferences preferences;
  private String keyMusic;
  private String keySound;
  private MuteListener[] listeners = new MuteListener[0];
  
  /**
   * Un/-mute sound.
   * @param muted true to mute
   */
  public void setSoundMuted(boolean mute) {
    soundMuted=mute;
    if (preferences!=null) {
      preferences.putBoolean(keySound,soundMuted);
      preferences.flush();
    }
    for (MuteListener m:listeners) m.muteSound(soundMuted);
  }
  
  /**
   * Un/-mute music
   * @param mute true to mute
   */
  public void setMusicMuted(boolean mute) {
    musicMuted=mute;
    if (preferences!=null) {
      preferences.putBoolean(keyMusic,musicMuted);
      preferences.flush();
    }
    for (MuteListener m:listeners) m.muteMusic(musicMuted);
  }
  
  /**
   * Query if music was muted
   * @return true if muted
   */
  public boolean isMusicMuted() {
    return musicMuted;
  }
  
  /**
   * Query if sound is muted
   * @return true if muted
   */
  public boolean isSoundMuted() {
    return soundMuted;
  }
  
  /**
   * Add a listener to be notified when the mute settings change
   * @param l listener to notify upon setting changes
   */
  public void addMuteListener(MuteListener l) {
    if (l==null) return;
    int len= listeners.length;
    for (int i=0;i<len;i++) {
      if(listeners[i]==l) return;
    }
    
    MuteListener[] tmp = new MuteListener[len+1];
    System.arraycopy(listeners,0,tmp,0,len);
    tmp[len]=l;
    listeners=tmp;
  }
  
  /**
   * Remove a listener from the notififaction list
   * @param l the listener to remove
   */
  public void removeMuteListener(MuteListener l) {
    if (l==null) return;
    int idx=-1;
    for (int i=0;i<listeners.length;i++) {
      if (listeners[i]==l) {
        idx=i;
        break;
      }
    }
    
    if (idx!=-1) {
      MuteListener[] tmp = new MuteListener[listeners.length-1];
      System.arraycopy(listeners, 0, tmp, 0, idx);
      if (idx < tmp.length) {
        System.arraycopy(listeners, idx+1, tmp, idx, tmp.length - idx);
      }
      listeners=tmp;
    }
  }
  
  /**
   * Connect the manager with a persistant storage for keeping settings in between runs.
   * Whenever a new storage is set, its settings are read and listeners are notified.
   * @param p the storage object. May be null to not sync with a storage.
   * @param keyMusic key name for storing the music setting.
   * @param keySound key name for storing the sound setting.
   */
  public void persistWith(Preferences p, String keyMusic, String keySound) {
    preferences=p;
    if (preferences!=null) {
      if (keyMusic==null || keySound == null) throw new NullPointerException("keys may not be null!");
      this.keyMusic=keyMusic;
      this.keySound=keySound;
      soundMuted = preferences.getBoolean(keySound,soundMuted);
      musicMuted = preferences.getBoolean(keyMusic,musicMuted);
    }
  }
}