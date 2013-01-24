package de.onyxbits.bureauengine.screen;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.Gdx;
import de.onyxbits.bureauengine.BureauGame;


/**
 * A {@link BureauScreen} that has a stage object.
 */
public abstract class StageScreen<T extends BureauGame> extends BureauScreen<T> {

  /**
   * The stage object (will be rendered automatically
   */
  protected Stage stage;
  
    /**
   * Instantiate a new screen. Note: instantiation must usually happen fast and on the
   * UI thread (e.g. when the player hits an "exit" button). Put all your real construction
   * work in the {@link #readyScreen} method.
   * @param game callback reference to the main game object
   */
  public StageScreen(T game) {
    super(game);
  }
  
  @Override
  public void show() {
    super.show();
    Gdx.input.setInputProcessor(stage);
  }
  
  public void hide() {
    super.hide();
    if (Gdx.input.getInputProcessor()==stage) {
      Gdx.input.setInputProcessor(null);
    }
  }
  
  @Override
  public void render(float delta) {
    super.render(delta);
    stage.act(delta);
    stage.draw();
  }
  
  @Override
  public void dispose() {
    super.dispose();
    if (stage!=null) stage.dispose();
  }
     
}
