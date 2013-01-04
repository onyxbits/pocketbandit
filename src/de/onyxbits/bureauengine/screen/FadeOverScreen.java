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
 * Implements a fading over effect between two screens (fade to black, fade from black). Note:
 * music is faded as well, meaning, this class will manipulate the <code>Music</code> object
 * on the target screen, potentially setting it to null or even replacing it.
 */
public class FadeOverScreen implements Screen {

  private Screen fromScreen;
  private BureauScreen toScreen;
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
   * Fade over to another screen
   * @param toScreen target screen. 
   * @param time time in seconds for how long the fading in/out takes each.
   */
  public void fadeTo(BureauScreen toScreen, float time) {
    this.toScreen=toScreen;
    this.game=toScreen.game;
    this.fromScreen=game.getScreen(); // If this is null, someone is using the engine in a funky way.
    state=FADEOUT;
    screenWidth=Gdx.graphics.getWidth();
    screenHeight=Gdx.graphics.getWidth();
    fadeDuration=time;
    fadePercent=0;
    fadeTime=0;
    Gdx.input.setInputProcessor(null);
    game.setScreen(this);
  }
  
  @Override
  public void render(float delta) {
    switch(state) {
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
        fadePercent=0;
        fadeTime=0;
        if ((fromScreen instanceof BureauScreen) && ((BureauScreen)fromScreen).music!=null) {
          ((BureauScreen)fromScreen).music.stop();
        }
        toScreen.prepareAssets(true); 
        toScreen.readyScreen();
        if (toScreen.music!=null) {
          toScreen.music.setVolume(0);
          if (!game.muteManager.isMusicMuted()) toScreen.music.play();
        }
        fromScreen.dispose();
        fromScreen=null;
        System.gc();
        state=SKIP;
        break;
      }
      case SKIP: {
        // Skip the first frame on toScreen. We have to do this since textures can only be loaded
        // on the GL thread, potentially stalling it and resulting in a spike in delta time. Such
        // spikes mess with the fading math. Easiest way to compensate is by throwing one frame away.
        // Note: an alternative approach would be to call AssetManager.update() on every odd frame while
        // fading out and skip the actual rendering for those frames. However, tests showed that this
        // produces very choppy fading.
        state=FADEIN;
        break;
      }
      case FADEIN: {
        toScreen.render(delta);
        if (fade(delta)) {
          // We have to do a little dance with the music here. Game.setScreen() will call
          // BureauScreen.show() and that will call Music.play(). Ideally, Music.play() should
          // do nothing if it is already playing, but not all backends seem to implement it that
          // way. So, in order to prevent an audio glitch, take it away temporarily.
          // FIXME: Wouldn't it be better to use a NullMusic object here? Someone implementing a
          // screen may not be aware of this little hack and run into a nullpointerexcpetion. On
          // the other hand: its another object to lug around and may cause even weirder and harder
          // to debug side effects.
          Music tmp = toScreen.music;
          toScreen.music=null;
          game.setScreen(toScreen);
          toScreen.music=tmp;
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
      if ((fromScreen instanceof BureauScreen) && ((BureauScreen)fromScreen).music!=null) {
        ((BureauScreen)fromScreen).music.setVolume(1f-fadePercent);
      }
      game.spriteBatch.begin();
      game.spriteBatch.setColor(0,0,0,fadePercent);
      game.spriteBatch.draw(blankTexture,0,0,screenWidth,screenHeight);
      game.spriteBatch.end();
    }
    if (state==FADEIN) {
      if (toScreen.music!=null) {
        toScreen.music.setVolume(fadePercent);
      }
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
