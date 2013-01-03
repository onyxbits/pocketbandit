package de.onyxbits.bureauengine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
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
 * the player can navigate
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
   * General purpose Random Number Generator.
   */
  public static final Random rng = new Random(System.currentTimeMillis());

  @Override
  public void dispose() {
    super.dispose();
    if (spriteBatch!=null) spriteBatch.dispose();
    if (assetManager!=null) assetManager.dispose();
    if (getScreen()!=null) getScreen().dispose();
  }
  
  @Override
  public void create() {
    spriteBatch = createSpriteBatch();
    assetManager = createAssetManager();
    muteManager = createMuteManager();
    
    BureauScreen tmp = createStartUpScreen();
    tmp.prepareAssets(true);
    tmp.readyScreen();
    setScreen(tmp);
    Texture.setAssetManager(assetManager);
  }
  
  @Override
  public void resume() {
    if (assetManager!=null) {
      assetManager.finishLoading();
    }
  }
  
  public void pause() {
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