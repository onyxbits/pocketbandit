package de.onyxbits.bureauengine.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Game;

/**
 * A <code>Screen</code> for smoothly transitioning between <code>Screen</code>S.
 */
public interface TransitionScreen extends Screen {

  /**
   * Set the transition up
   * @param game callback into the game
   * @param fromScreen the <code>Screen</code> to transition away from
   * @param toScreen the <code>Screen</code> to transit to.
   * @param timimg transition dependent timing values.
   */
  public void configure(Game game, BureauScreen fromScreen, BureauScreen toScreen, float... timing);
}
 