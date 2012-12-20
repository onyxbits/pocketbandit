package de.onyxbits.bureauengine.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import de.onyxbits.bureauengine.BureauGame;

/**
 * A startup screen that can be shown while assets are being loaded and cached. Note: the
 * engine is designed to keep resource usage at a minimum by splitting the game into independant
 * compartments (<code>BureauScreen</code>S) that get their assets loaded when needed.
 * However, it may be desireable to preload and cache some assets with this class for faster
 * screen transitions.
 */
public abstract class SplashScreen extends BureauScreen {

  public SplashScreen(BureauGame game) {
    super(game);
    stage.addActor(getSplashImage());
  }
  
  /**
   * Subclasses must provide an image to be shown on the screen. This method is called
   * after textures have been loaded.
   * @return The <code>Image</code> that is to be shown on this <code>SplashScreen</code>
   */
  public abstract Image getSplashImage();
  
  /**
   * All <code>SplashScreen</code>S are associated with a background task and stay
   * on screen until that task finsishes
   * @return a <code>Runnable</code> that is executed in the background and waited for to complete.
   */
  public abstract Runnable getBackgroundTask();
  
  /**
   * Automatically switch over to another screen after the background task has
   * finished.
   * @return The screen to show after the background task has finished
   */
  public abstract BureauScreen getNextScreen();
   
}