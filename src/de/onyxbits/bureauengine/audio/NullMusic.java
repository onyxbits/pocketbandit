package de.onyxbits.bureauengine.audio;

import com.badlogic.gdx.audio.Music;

/**
 * A <code>Music</code> implementation without any backend. Useful to avoid
 * nullpointerchecks when no real music is available.
 */
public class NullMusic implements Music {

  private boolean isLooping = false;
  private long since=-1;
  
  @Override
  public void dispose() {}
  
  @Override
  public float getPosition() {
    return System.currentTimeMillis()-since;
  } 
  
  @Override
  public boolean isLooping() {
    return isLooping;
  }
  
  @Override
  public boolean isPlaying() {
    return since>-1;
  }
  
  @Override
  public void play() {
    since=System.currentTimeMillis();
  }
  
  @Override
  public void pause() {
    since=-1;
  }
  
  @Override
  public void setLooping(boolean isLooping) {
    this.isLooping=isLooping;
  }
  
  @Override
  public void setVolume(float volume) {}
  
  @Override
  public void stop() {
    since=-1;
  }
  
}