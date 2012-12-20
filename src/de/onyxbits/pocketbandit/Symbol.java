package de.onyxbits.pocketbandit;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * A symbol on the reel
 */
class Symbol extends Image {

  /**
   * For rolling new symbols
   */
  private Variation variation;
  
  /**
   * Game state
   */
  private Player player;
  
  /**
   * The reel (0-2) this <code>Symbol</code> sits on
   */
  private int reel;
  
  /**
   * Symbol faces
   */
  private Drawable[] symbols;
  
  /**
   * index into <code>symbols</code> (the one that is suppose to show).
   */
  private int face;
  
  /**
   * How many stops have still to pass before stopping to spin
   */
  private int remainingStops;
  
  /**
   * Total number of stops in the current round
   */
  private int totalStops;
  
  /**
   * Cached height of the symbol face. All symbols in the machine must have the same size.
   */
  private int symbolHeight;
  
  /**
   * How fast to scroll symbols by. Condition: symbolHeight % velocity==0
   */
  private int velocity;
  
  /**
   * Call reference (to notify when a symbols stops)
   */
  private GambleScreen gambleScreen;
  
  /**
   * Emergency break signal. When set, try to stop ASAP
   */
  private boolean doBreak;
  
  /**
   * Only use this constructor!
   * @param variation game rules
   * @param symbols <code>Drawable</code> version of <code>Variation.SYMBOLNAMES</code>. Symbol heights
   * must be a multiple of 2.
   * @param inital index into <code>symbols</code>: The initial face to show.
   * @param reel the reel on which this <code>Symbol</code> sits.
   * @param gambleScreen callback to notify about reels starting/stopping.
   */
  public Symbol(Variation variation, Drawable[] symbols, int initial, int reel, GambleScreen gambleScreen) {
    super(symbols[initial]);
    this.player=player;
    this.reel=reel;
    this.variation=variation;
    this.symbols=symbols;
    this.gambleScreen=gambleScreen;
    face=initial;
    symbolHeight=(int)symbols[initial].getMinHeight();
  }
  
  public void act(float delta) {
    if (remainingStops<=0) {
      return;
    }
    
    int posY= (int)getY();
    
    if (posY==0) {
      posY=(int)(3*symbolHeight);
      face = variation.pick(reel);
      setDrawable(symbols[face]);
    }
    posY-=velocity;
    setY(posY);
    
    if (posY%(symbolHeight)==0) {
      if (doBreak) {
        remainingStops=0;
      }
      else {
        remainingStops--;
      }
    }
    if (remainingStops==0) {
      gambleScreen.inMotion(this,false);
    }
  }
  
  /**
   * Bring the symbol to a halt ASAP. Note: For safety reasons, this
   * method does nothing if the this <code>Symbol</code> is currently
   * in a stop position (braking in a stop position would jam the reels).
   * @return true if the symbol was decelerated successfully and will stop
   * ASAP.
   */
  public boolean handbrake() {
    if (getY() % symbolHeight == 0) return false;
    doBreak=true;
    return true;
  }
  
  /**
   * Set this <code>Actor</code> in motion
   * @param stops after how many stops to come to rest. Must be at leat 1.
   */
  protected void spin(int stops, int velocity) {
    totalStops=stops;
    remainingStops=stops;
    doBreak=false;
    if (stops>0) {
      gambleScreen.inMotion(this,true);
    }
    this.velocity=velocity;
  }
  
  /**
   * Query the reel
   * @return the reel, this <code>Symobl</code> is sitting on.
   */
  protected int getReel() {
    return reel;
  }
  
  /**
   * Check if the this <code>Actor</code> is on the payline
   * @return true if the reels are in the resting position and this <code>Actor</code> is
   * in the middle
   */
  protected boolean isOnPayline() {
    return getY()==getHeight();
  }
  
  /**
   * Query the face of this symbol
   * @return index into <code>Variation.SYMBOLNAMES</code>
   */
  protected int getFace() {
    return face;
  }
}