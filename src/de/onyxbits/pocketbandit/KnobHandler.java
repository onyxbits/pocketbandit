package de.onyxbits.pocketbandit;

import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.TimeUtils;


/**
 * Handles the lever/ starts a new round
 */
class KnobHandler extends DragListener {

  /**
   * Callback reference
   */
  private GambleScreen gambleScreen;
  
  /**
   * Reference to the game state
   */
  private Player player;
  
  /**
   * Reference to the reels to spin
   */
  private Symbol[] reelSymbols;
  
  /**
   * May not move above this point
   */
  private float topLimitStop;
  
  /**
   * May not move below this point
   */
  private float bottomLimitStop;
  
  /**
   * Resting position to return to after release
   */
  private float restPoint;
  
  /**
   * Trigger point for a new round
   */
  private float triggerPoint;
  
  /**
   * Emergency break for the current round
   */
  private float brakePoint;
  
  /**
   * Offset between the knob's bottom line and the touch position
   */
  private float offset;
  
  /**
   * Has the knob been pulled below the triggerpoint since the last drag start
   */
  private boolean triggered=false;
  
  /**
   * Has the player hit the brakes?
   */
  private boolean stopped=false;
  
  /**
   * When dragging started
   */
  private long startTime;
  
  /**
   * How long it took the player to pull the knob to the trigger point
   */
  private int timeDiff;
  
  /**
   * Whether or not reels may be stopped
   */
  private boolean allowBrakes;
  
  /**
   * Construct a new handler.
   * @param gambleScreen callback reference
   * @param allowBrakes whether or nor reesl can be stopped.
   * @param player game state
   * @param topLimitStop max vertical coordinate of the knob
   * @param bottomLimitStop min vertical coordinate of the knob
   */
  public KnobHandler(GambleScreen gambleScreen, boolean allowBrakes, float topLimitStop, float bottomLimitStop) {
    this.gambleScreen=gambleScreen;
    this.player=player;
    this.topLimitStop=topLimitStop;
    this.bottomLimitStop=bottomLimitStop;
    this.allowBrakes=allowBrakes;
    float length=topLimitStop-bottomLimitStop;
    restPoint=bottomLimitStop+length*0.88f;
    triggerPoint=bottomLimitStop+length*0.20f;
    brakePoint=bottomLimitStop+length*0.90f;
  }
  
  /**
   * Move a knob to the resting position
   * @param knob knob to move
   */
  public void restKnob(Actor knob) {
    knob.setY(restPoint);
  }
  
  public void drag (InputEvent event, float x, float y, int pointer) {
    Actor actor = event.getListenerActor();
    float tmp = actor.getY()+y-offset;
    if (!triggered && tmp>bottomLimitStop && tmp<topLimitStop) {
      // When not triggered, the knob may be moved freely within the bounds of the slot
      actor.setY(tmp);
    }
    if (triggered && tmp>bottomLimitStop && tmp<triggerPoint) {
      // When the triggerpoint has been reached once, the knob may no longer slide above it
      actor.setY(tmp);
    }
    if (actor.getY()<triggerPoint && !triggered && !gambleScreen.isSpinning()) {
      // Give feedback
      triggered=true;
      gambleScreen.playSoundEffect(GambleScreen.TRIGGERSOUND);
      timeDiff=(int) (TimeUtils.millis()-startTime);
    }
    
    if (allowBrakes && actor.getY()>brakePoint && !stopped && gambleScreen.isSpinning()) {
      stopped=true;
      gambleScreen.playSoundEffect(GambleScreen.TRIGGERSOUND);
      gambleScreen.brakeWheels();
    }
  }
  
  public void dragStart (InputEvent event, float x, float y, int pointer) {
    offset=y;
    startTime=TimeUtils.millis();
  }
  
  public void dragStop (InputEvent event, float x, float y, int pointer) {
    Actor actor = event.getListenerActor();
    actor.addAction(Actions.moveTo(actor.getX(),restPoint,0.5f));
    if (triggered) {
    
      int velocity = 2;
      if (timeDiff<500) velocity=4;
      if (timeDiff<200) velocity=8;
      
      gambleScreen.newRound(velocity);
      
      triggered=false;
    }
    
    if (stopped) {
      stopped=false;
    }
  }
  
}
