package de.onyxbits.pocketbandit;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Camera;

/**
 * A <code>Group</code> in which drawing can be limited to a certain area.
 */
public class ClippingGroup extends Group {

  private Rectangle scissors;
  private  Rectangle clip;
  
  /**
   * Create a new group
   * @param clip the area in which drawing takes place.
   */
  public ClippingGroup(Rectangle clip) {
    super();
    this.clip=clip;
    scissors= new Rectangle();
  }
  
  @Override
  public void draw (SpriteBatch batch, float parentAlpha) {
    
    if (isTransform()) applyTransform(batch, computeTransform());
    ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), clip, scissors);
    ScissorStack.pushScissors(scissors);
    super.draw(batch,parentAlpha);
    batch.flush();
    ScissorStack.popScissors();
    if (isTransform()) resetTransform(batch);
    
  }
}