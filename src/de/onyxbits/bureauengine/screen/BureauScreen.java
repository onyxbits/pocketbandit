package de.onyxbits.bureauengine.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;

import de.onyxbits.bureauengine.BureauGame;
import de.onyxbits.bureauengine.audio.NullMusic;
import de.onyxbits.bureauengine.audio.MuteListener;

/**
 * A screen of the game. <code>BureauScreen</code>S are heavyweight objects that usually
 * have a large amount of assets (music, textures, sfx) bound to them. Their purpose is
 * to break the game down into seperate working sets of "art units", so large games
 * can be written without the need to hold all assets in memory at all time.
 */
public abstract class BureauScreen<T extends BureauGame> implements Screen, MuteListener {
  
  /**
   * Reference to the game object.
   */
  protected T game;
  
  /**
   * Music playing on this screen. May be null, will not be disposed of automatically if
   * not loaded via an <code>AssetManager</code>.
   */
  protected Music music;
  
  /**
   * Instantiate a new screen. Note: instantiation must usually happen fast and on the
   * UI thread (e.g. when the player hits an "exit" button). Put all your real construction
   * work in the {@link #readyScreen} method.
   * @param game callback reference to the main game object
   */
  public BureauScreen(T game) {
    this.game=game;
  }
  
  /**
   * Register all assets declared by <code>getAssets()</code> with the <code>AssetManager</code>. 
   * This method must be called before <code>readyScreen()</code> may be called. Note: this is a
   * two step process because asset loading (at least as far as textures are concerned) must be
   * done on the UI thread which will cause a noticable pause in game play. Some games may want
   * to handle that pause differently than others.
   * @param finishLoading true to also call <code>AssetManager.finishLoading()</code>.
   */
  public void prepareAssets(boolean finishLoading) {
    AssetDescriptor ad[] = getAssets();
    for (AssetDescriptor tmp: ad) {
      game.assetManager.load(tmp);
    }
    if (finishLoading) game.assetManager.finishLoading();
  }
  
  /**
   * Declare assets that are to be automatically loaded/unloaded.
   * @return The assets, this <code>Screen</code> depends on.
   */
  protected AssetDescriptor[] getAssets() {
    return new AssetDescriptor[0];
  }
  
  @Override
  public void dispose() {
    AssetDescriptor ad[] = getAssets();
    for (AssetDescriptor tmp: ad) { 
      game.assetManager.unload(tmp.fileName);
    }
  }
  
  /**
   * Showing the screen will register the stage as an inputprocessor, register
   * the screen as a <code>MuteListener</code> and start playing music.
   */
  @Override
  public void show() {
    game.muteManager.addMuteListener(this);
    if (music!=null && !game.muteManager.isMusicMuted()) {
      music.play();
    }
  }
  
  @Override
  public void hide() {
    game.muteManager.removeMuteListener(this);
  }
  
  @Override
  public void resume() {
    game.assetManager.finishLoading();
  }
  
  @Override
  public void pause() {
  }
  
  @Override
  public void resize(int w, int h) {}
  
  /**
   * Subclasses should override this method if they wish to do any drawing below the 
   * stage. Default implementation just clears the screen to a solid color.
   * @param delta time diff
   */
  public void renderBackground(float delta) {
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
  }
  
  /**
   * Default implementation pauses/resumes music
   */
  @Override
  public void muteMusic(boolean mute) {
    if (music==null) return;
    if (mute) {
      music.pause();
    }
    else {
      music.play();
    }
  }
  
  /**
   * Default implementation does nothing
   */
  @Override
  public void muteSound(boolean mute) {}
  
  
  @Override
  public void render(float delta) {
    renderBackground(delta);
  }
  
  /**
   * Called before the screen is shown. This is the method to actually construct the screen
   * in. 
   */
  public abstract void readyScreen();
  
}