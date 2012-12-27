package de.onyxbits.bureauengine.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
 * <p>
 * <code>BureauScreen</code>S are not meant to be recycled. Once they are hidden, they
 * must not be shown again.
 */
public abstract class BureauScreen<T extends BureauGame> implements Screen, MuteListener {
  
  /**
   * <code>Stage</code> of this screen. Will be rendered automatically.
   */
  protected Stage stage;
  
  /**
   * Reference to the game object.
   */
  protected T game;
  
  /**
   * Music playing on this screen
   */
  protected Music music;
  
  /**
   * Create a new <code>Screen</code>. Subclasses should override this constructor and
   * register all required assets with the <code>AssetManager</code> here.
   * @param game callback reference to the main game object
   */
  public BureauScreen(T game) {
    this.game=game;
    AssetDescriptor ad[] = getAssets();
    for (AssetDescriptor tmp: ad) {
      game.assetManager.load(tmp);
    }
  }
  
  /**
   * Subclasses may override this for autoloading/disposing of assets. This method is
   * optional, subclasses may handle their assets differently if they wish.
   * @return a list of assets that should be loaded into the <code>AssetManager</code> by the
   * constructor and disposed of again in the <code>dispose()</code> method.
   */
  protected AssetDescriptor[] getAssets() {
    return new AssetDescriptor[0];
  }
  
  @Override
  public void dispose() {
    if (stage!=null) stage.dispose();
    AssetDescriptor ad[] = getAssets();
    for (AssetDescriptor tmp: ad) { 
      game.assetManager.unload(tmp.fileName);
    }
  }
  
  @Override
  public void show() {
    Gdx.input.setInputProcessor(stage);
  }
  
  @Override
  public void hide() {
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
    if (mute) {
      getMusic().pause();
    }
    else {
      getMusic().play();
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
    stage.act(delta);
    stage.draw();
  }
  
  /**
   * Access to the (currently playing) music of the screen. Note: music may be controlled externally
   * (e.g. while in screen transitions).
   * @return the screen's music. Default implementation returns <code>NullMusic</code>
   */
  public Music getMusic() {
    if (music==null) music = new NullMusic(); // Create this lazyly!
    return music;
  }
  
  /**
   * Initialize this screen. Subclasses should override this method to register eventlisteners
   * and to do any heavy duty construction work (e.g. building the stage). The default implementation
   * just creates a stage that fills the entire screen.
   */
  public void readyScreen() {
    this.stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false,game.spriteBatch);
  }
  
}