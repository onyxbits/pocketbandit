package de.onyxbits.bureauengine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.assets.AssetManager;

import java.util.Random;

import de.onyxbits.bureauengine.audio.NullMusic;
import de.onyxbits.bureauengine.audio.MuteManager;
import de.onyxbits.bureauengine.screen.BureauScreen;

/**
 * A game that consists of several more or less independant screens between which
 * the player can navigate.
 */
public abstract class BureauGame extends Game {

  /**
   * Application wide <code>SpriteBatch</code>
   */
  public SpriteBatch spriteBatch;
  
  /**
   * Responsible for making assets available
   */
  public AssetManager assetManager;
  
  /**
   * Central control for muting sound/music.
   */
  public MuteManager muteManager;
  
  /**
   * Global preferences. This is a managed object that will get automatically flushed 
   * when the game is paused or destroyed. Note: Do not store anything in this object
   * and then try to retrieve it without doing a <code>flush()</code> in between! At
   * least on Android, this will fail and you will get the old value!
   */
  public static Preferences prefs;
  
  /**
   * General purpose Random Number Generator.
   */
  public static final Random rng = new Random(System.currentTimeMillis());

  
  /**
   * The game is booted in this order:<p>
   * <code>createPreferences()</code>
   * <code>bootGame()</code>
   * <code>create*Manager()</code>
   * <code>createFirstScreen()</code>
   * <p>
   * Afterwards the first screen is directly shown.
   */
  @Override
  public void create() {
    prefs = createPreferences();
    bootGame();
    spriteBatch = createSpriteBatch();
    assetManager = createAssetManager();
    muteManager = createMuteManager();
    
    BureauScreen tmp = createStartUpScreen();
    tmp.prepareAssets(true);
    tmp.readyScreen();
    setScreen(tmp);
    Texture.setAssetManager(assetManager);
  }
  
  /**
   * Subclasses must call <code>super.resume()</code>.
   */
  @Override
  public void resume() {
    if (assetManager!=null) {
      assetManager.finishLoading();
    }
    prefs = createPreferences();
  }
  
  /**
   * Subclasses must call <code>super.pause()</code>.
   */
  @Override
  public void pause() {
    if (prefs!=null) prefs.flush();
    super.pause();
  }
  
  /**
   * Subclasses must call </code>super.dispose()</code>
   */
  @Override
  public void dispose() {
    if (prefs!=null) prefs.flush();
    super.dispose();
    if (spriteBatch!=null) spriteBatch.dispose();
    if (assetManager!=null) assetManager.dispose();
  }
  
  /**
   * Called as the first method. Default implementation does nothing.
   */
  protected void bootGame() {}
  
  /**
   * Create the global preferences.
   * @return a <code>Preferences</code> object that is linked to a persistent
   * storage or null if not desired. Default implementation returns null.
   */
  protected Preferences createPreferences() {
    return null;
  }
  
  /**
   * Get the screen that is to show on game startup
   * @return a new instance of the starting screen
   */
  protected abstract BureauScreen createStartUpScreen();
  
  /**
   * Create the <code>muteManager</code>. Subclasses should override this method.
   * @return Default implementation returns a mute manager that is not configured to persist its settings.
   */
  protected MuteManager createMuteManager() {
    return new MuteManager();
  }
  
  /**
   * Create the <code>assetManager</code>. Subclasses should override this method.
   * @return Default implementation returns a manager with default loaders.
   */
  protected AssetManager createAssetManager() {
    return new AssetManager();
  }
  
  /**
   * Create the <code>spriteBatch</code>. Subclasses should override this method.
   * @return Default implementation returns a standard <code>SpriteBatch</code>
   */
  protected SpriteBatch createSpriteBatch() {
    return new SpriteBatch();
  }
  
}