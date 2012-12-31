package de.onyxbits.bureauengine.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;

import de.onyxbits.bureauengine.BureauGame;

/**
 * Implements a fading over effect between two screens (fade to black, fade from black). To make
 * proper use of this class:
 * <ul>
 *   <li> Make sure that music on fading in screen is not playing. It will be started from this class
 *   <li> Do not (de-)subscribe <code>MuteListener</code>S. This calss will handle that.
 *   <li> Make sure to <code>dispose()</code> when no longer needed.
 * </ul>
 */
public class FadeOverScreen implements Screen {

  private BureauScreen fromScreen;
  private BureauScreen toScreen;
  private float[] time;
  private BureauGame game;
  private int state;
  private int screenWidth,screenHeight;
  
  /**
   * timespan for fading in/out
   */
  private float fadeDuration;
  
  /**
   * Accumulator for the fading
   */
  private float fadeTime;
  
  /**
   * Progress status of the fading process
   */
  private float fadePercent;
  
  /**
   * Fading state
   */
  private static final int START=0;
  
  /**
   * Fading state
   */
  private static final int FADEOUT=1;
  
  /**
   * Fading state
   */
  private static final int MIDWAY=2;
  
  /**
   * Fading state
   */
  private static final int SKIP=3;

  /**
   * Fading state
   */
  private static final int FADEIN=4;
  
  /**
   * Overlay for producing the fade effect
   */
  private Texture blankTexture;
  
  /**
   * Set the transition up
   * @param game callback into the game
   * @param fromScreen the <code>Screen</code> to transition away from
   * @param toScreen the <code>Screen</code> to transit to.
   * @param time timing values.
   */
  public void configure(BureauGame game, BureauScreen fromScreen, BureauScreen toScreen, float... time) {
    this.fromScreen=fromScreen;
    this.toScreen=toScreen;
    this.game=game;
    this.time=time;
    state=START;
    screenWidth=Gdx.graphics.getWidth();
    screenHeight=Gdx.graphics.getWidth();
    fadeDuration=time[0];
    fadePercent=0;
    fadeTime=0;
  }
  
  @Override
  public void render(float delta) {
    switch(state) {
      case START: {
        Gdx.input.setInputProcessor(null);
        fromScreen.render(delta);
        state=FADEOUT;
        break;
      }
      case FADEOUT: {
        fromScreen.render(delta);
        if (fade(delta)) {
          state=MIDWAY;
        }
        break;
      }
      case MIDWAY: {
        // The overlay is fully opaque now -> we can get away with stalling the rendering thread
        // without the player noticing.
        toScreen.prepareAssets(true);
        fadePercent=0;
        fadeTime=0;
        fromScreen.getMusic().stop();
        game.muteManager.removeMuteListener(fromScreen);
        toScreen.readyScreen();
        toScreen.getMusic().setVolume(0);
        if (!game.muteManager.isMusicMuted()) toScreen.getMusic().play();
        game.muteManager.addMuteListener(toScreen);
        state=SKIP;
        break;
      }
      case SKIP: {
        // Skip the first frame on toScreen. We have to do this since textures can only be loaded
        // on the GL thread, potentially stalling it and resulting in a spike in delta time. Such
        // spikes mess the fading math. Easiest way to compensate is by throwing one frame away.
        state=FADEIN;
        break;
      }
      case FADEIN: {
        toScreen.render(delta);
        if (fade(delta)) {
          game.setScreen(toScreen);
          fromScreen.dispose();
          fromScreen=null;
          toScreen=null;
        }
        break;
      }
    }
  }
  
  /**
   * Fade this screen in or out.
   * @param delta detla from <code>render(delta)</code>
   * @return true when the screen is fully faded.
   */
  private boolean fade(float delta) {
    if (fadePercent>=1) return true;

    // Calculate progress by accumulating delta time.
    fadeTime += delta;
    if (fadeTime>=fadeDuration) fadePercent = 1;
    else {
      fadePercent = fadeTime / fadeDuration;
    }
    
    Color tmp = game.spriteBatch.getColor();
    // Apply the effect
    if (state==FADEOUT) {
      fromScreen.getMusic().setVolume(1f-fadePercent);
      game.spriteBatch.begin();
      game.spriteBatch.setColor(0,0,0,fadePercent);
      game.spriteBatch.draw(blankTexture,0,0,screenWidth,screenHeight);
      game.spriteBatch.end();
    }
    if (state==FADEIN) {
      toScreen.getMusic().setVolume(fadePercent);
      game.spriteBatch.begin();
      game.spriteBatch.setColor(0,0,0,1f-fadePercent);
      game.spriteBatch.draw(blankTexture,0,0,screenWidth,screenHeight);
      game.spriteBatch.end();
    }
    game.spriteBatch.setColor(tmp);
    return fadePercent>=1;
  }
  
  /**
   * (re-)Create the blank texture
   */
  private void dynamicTextures() {
    if (blankTexture!=null) blankTexture.dispose();
     Pixmap pix = new Pixmap(2, 2, Format.RGBA8888);
     pix.setColor(Color.BLACK);
     pix.fill();
     blankTexture=new Texture(pix);
     pix.dispose();
  }
  
  @Override
  public void show() {
    dynamicTextures();
  }
  
  @Override
  public void hide() {
  }
  
  @Override
  public void resume() {
    dynamicTextures();
  }
  
  @Override
  public void resize(int w, int h) {
    dynamicTextures();
    screenWidth=w;
    screenHeight=h;
  }
  
  @Override
  public void pause() {}
  
  @Override
  public void dispose() {
    if (blankTexture!=null) blankTexture.dispose();
  }
}
