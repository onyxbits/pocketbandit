package de.onyxbits.pocketbandit;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.*;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import de.onyxbits.bureauengine.*;
import de.onyxbits.bureauengine.screen.*;

/**
 * Represents the actual game screen. This class also doubles as the config screen.
 */
public class GambleScreen extends BureauScreen implements EventListener {

  /**
   * 3x3 symbols packed into a single array. First reel goes from 0 to 2, second from 3 to 5 and
   * third from 6 to 8. Symbols within a reel are potentially unordered.
   */
  private Symbol[] reelSymbols = new Symbol[9];
  
  /**
   * For toggling the number of coins the player may bet in each game.
   */
  private ImageButton[] bet = new ImageButton[3];
  
  /**
   * Amount of coins on hand
   */
  private Label credits;
  
  /**
   * The lever used to spin the reels
   */
  private Image knob;
  
  /**
   * How often the lever has been pulled.
   */
  private Label turns;
  
  /**
   * Displays a message to the player
   */
  private Label feedbackMessage;
  
  /**
   * Displays a symbol to the player
   */
  private Image feedbackSymbol;
  
  /**
   * Groups feedbackMessage and feedbackSymbol
   */
  private Group feedbackGroup;
  
  /**
   * Switch between game view and options view
   */
  private ImageButton viewSwitch;
  
  /**
   * Mute music
   */
  private ImageButton musicStatus;
  
  /**
   * Mute sound
   */
  private ImageButton soundStatus;
  
  /**
   * Cash out and exit to the menu screen
   */
  private ImageButton exit;
  
  /**
   * Config mode: select previous game variation
   */
  private ImageButton previousVariation;
  
  /**
   * Config mode: select next game variation
   */
  private ImageButton nextVariation;
  
  /**
   * Displays the name of the variation
   */
  private Table deviceName;
  
  /**
   * Rules
   */
  private Variation variation;
  
  /**
   * Game state
   */
  private Player player;
  
  /**
   * Number of <code>Symbol</code>S in motion.
   */
  private int spinning;

  /**
   * Symbols on the reel
   */
  private Drawable[] symbols;
  
  /**
   * Symbols on the paytable
   */
  private Drawable[] smallSymbols;
  
  /**
   * Container for the paytable
   */
  private ScrollPane scrollTable;

  /**
   * Contains the actual automaton UI
   */
  private Group deviceGroup = new Group();
  
  /**
   * Contains the name, paytable and menu buttons
   */
  private Group infoGroup = new Group();
  
  /**
   * For <code>playSoundEffect()</code>
   */
  protected static final int TRIGGERSOUND=0;
  
  /**
   * For <code>playSoundEffect()</code>
   */
  protected static final int WINSOUND=1;
  
  /**
   * For <code>playSoundEffect()</code>
   */
  protected static final int EJECTCOINSOUND=2;
  
  /**
   * For <code>playSoundEffect()</code>
   */
  protected static final int REELSTOPSOUND=3;
  
  private Sound triggerSound;
  private Sound winSound;
  private Sound ejectCoinSound;
  private Sound reelStopSound;
  
  private static final AssetDescriptor[] ASSETS = {
    new AssetDescriptor<TextureAtlas>("textures/gamblescreen.atlas",TextureAtlas.class),
    new AssetDescriptor<Music>("music/Theme for Harold var 3.mp3",Music.class),
    new AssetDescriptor<Sound>("sfx/Pellet Gun Pump-SoundBible.com-517750307.mp3",Sound.class),
    new AssetDescriptor<Sound>("sfx/135936__bradwesson__collectcoin.ogg",Sound.class),
    new AssetDescriptor<Sound>("sfx/56246__q-k__latch-04.ogg",Sound.class),
  };
  
  /**
   * Construct a new Gamble/Options screen
   * @param game reference to the game object
   * @param player game state. May be null. If null, the gamble screen allows for selecting
   * a variation.
   * @param variation the game variant
   */
  public GambleScreen(BureauGame game, Player player, Variation variation) {
    super(game);
    this.player = player;
    this.variation=variation;
  }
  
  @Override
  protected AssetDescriptor[] getAssets() {
    return ASSETS;
  }
  
  public void readyScreen() {
    this.stage = new Stage(320, 480, true,game.spriteBatch);
    deviceGroup.setTransform(false);
    
    triggerSound=game.assetManager.get("sfx/Pellet Gun Pump-SoundBible.com-517750307.mp3",Sound.class);
    winSound=game.assetManager.get("sfx/135936__bradwesson__collectcoin.ogg",Sound.class);
    reelStopSound=game.assetManager.get("sfx/56246__q-k__latch-04.ogg",Sound.class);
    music = game.assetManager.get("music/Theme for Harold var 3.mp3",Music.class);
    music.setLooping(true);

    //FIXME: Do these two need to be dispose()d off?
    TextureAtlas localAtlas =game.assetManager.get("textures/gamblescreen.atlas",TextureAtlas.class);
    TextureAtlas globalAtlas =game.assetManager.get("textures/global.atlas",TextureAtlas.class);
    
    Drawable up,down,checked; // Reusables for making buttons.
    symbols=new Drawable[variation.symbolNames.length];
    smallSymbols=new Drawable[variation.symbolNames.length];
    Drawable backgroundImage = new NinePatchDrawable(new NinePatch(globalAtlas.findRegion("roundbox_grey"),8,8,8,8));
    
    // Note: Ideally this would be done in renderBackground() without the use of actors. Unfortunately,
    // something about the stage being larger than the physical screen seems to mess with the camera
    TextureRegion background = localAtlas.findRegion("spr_background");
    for (int x=-12;x<Gdx.graphics.getWidth();x+=background.getRegionWidth()) {
      for (int y=-12;y<Gdx.graphics.getHeight();y+=background.getRegionHeight()) {
        Image img = new Image(background);
        img.setPosition(x,y);
        stage.addActor(img);
      }
    }
    
    for(int i=0;i<symbols.length;i++) {
      symbols[i]=new TextureRegionDrawable(new TextureRegion(localAtlas.findRegion(variation.symbolNames[i])));
      smallSymbols[i]=new TextureRegionDrawable(new TextureRegion(localAtlas.findRegion(variation.symbolNames[i])));
      smallSymbols[i].setMinWidth(symbols[i].getMinWidth()/2);
      smallSymbols[i].setMinHeight(symbols[i].getMinHeight()/2);
    }

    Image frontPanel = new Image(new TextureRegionDrawable(localAtlas.findRegion("spr_frontpanel")));
    frontPanel.setPosition(19,61);
    deviceGroup.addActor(frontPanel);
   
    knob = new Image(localAtlas.findRegion("spr_knob"));
    knob.setPosition(235,219);
    KnobHandler handler = new KnobHandler(this,true,240,69);
    knob.addListener(handler);
    handler.restKnob(knob);
    deviceGroup.addActor(knob);
    
    ClippingGroup reelGroup = new ClippingGroup(new Rectangle(0,56,Gdx.graphics.getWidth(),87));
    int[] initialFaces = variation.getInitialFaces();
    int pos = 0;
    for (int i=0;i<reelSymbols.length;i++) {
      reelSymbols[i] = new Symbol(variation,symbols,initialFaces[i],i/3,this);
      reelGroup.addActor(reelSymbols[i]);
      if (i>0 && i%3==0) {
        pos+=75;
      }
      reelSymbols[i].setPosition(pos, (i%3)*reelSymbols[0].getHeight());
    }
    reelGroup.setPosition(53,284);
    deviceGroup.addActor(reelGroup);
    
    Group coinGroup = new Group();
    for (int i=0;i<bet.length;i++) {
      up = new TextureRegionDrawable(localAtlas.findRegion("btn_bet_up"));
      down = new TextureRegionDrawable(localAtlas.findRegion("btn_bet_down"));
      checked = new TextureRegionDrawable(localAtlas.findRegion("btn_bet_checked"));
      bet[i] = new ImageButton(up,down,checked);
      bet[i].setPosition(0,3*bet[i].getHeight()-i*bet[i].getHeight());
      bet[i].addListener(this);
      coinGroup.addActor(bet[i]);
    }
    bet[0].setChecked(true);
    
    coinGroup.setPosition(10,40);
    deviceGroup.addActor(coinGroup);
    
    Table statusBar = new Table(((SlotMachine)game).skin);
    statusBar.setBackground(backgroundImage);
    statusBar.setBounds(10,10,300,48);
 
    if (player!=null) {
      Image knobCount = new Image(new TextureRegionDrawable(localAtlas.findRegion("spr_turns")));
      statusBar.add(knobCount).left();
     
      turns = new Label("x 0",((SlotMachine)game).skin);
      statusBar.add(turns).width(30).right().padLeft(5).padRight(20);
    
      Image coinCount = new Image(new TextureRegionDrawable(localAtlas.findRegion("spr_cash")));
      statusBar.add(coinCount);
    
      credits = new Label("x "+player.credit,((SlotMachine)game).skin);
      statusBar.add(credits).width(30).right().padLeft(5).padRight(20);
    
      up = new TextureRegionDrawable(localAtlas.findRegion("btn_view_up"));
      down = new TextureRegionDrawable(localAtlas.findRegion("btn_view_down"));
      checked = new TextureRegionDrawable(localAtlas.findRegion("btn_view_checked"));
      
      viewSwitch = new ImageButton(up,down,checked);
      viewSwitch.addListener(this);
      statusBar.add(viewSwitch).right();
    }
    else {
      // Setup screen
      up = new TextureRegionDrawable(localAtlas.findRegion("btn_left_up"));
      down = new TextureRegionDrawable(localAtlas.findRegion("btn_left_down"));
      
      previousVariation = new ImageButton(up,down);
      previousVariation.addListener(this);
      
      up = new TextureRegionDrawable(localAtlas.findRegion("btn_right_up"));
      down = new TextureRegionDrawable(localAtlas.findRegion("btn_right_down"));
      nextVariation = new ImageButton(up,down);
      nextVariation.addListener(this);
      
      statusBar.add(previousVariation).padRight(30);
      statusBar.add("Select game").padRight(30);
      statusBar.add(nextVariation);
    }
    
    stage.addActor(statusBar);
    
    feedbackMessage = new Label("",((SlotMachine)game).skin);
    feedbackSymbol = new Image(new TextureRegionDrawable(localAtlas.findRegion("spr_feedbackcoins")));
    feedbackGroup = new Group();
    feedbackGroup.addActor(feedbackMessage);
    feedbackGroup.addActor(feedbackSymbol);
    feedbackMessage.setPosition(10+feedbackSymbol.getWidth(),feedbackSymbol.getHeight()/2-feedbackMessage.getHeight()/2);
    feedbackGroup.getColor().a=0;
    deviceGroup.addActor(feedbackGroup);
    
    deviceName = new Table(((SlotMachine)game).skin);
    deviceName.setBackground(backgroundImage);
    deviceName.add(variation.machineName);
    deviceName.setBounds(10,422,300,48);
    infoGroup.addActor(deviceName);
    
    Table buttons = new Table();
    buttons.setBounds(262,150,48,200);
    buttons.setBackground(backgroundImage);
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_musicmuted_up"));
    down = new TextureRegionDrawable(localAtlas.findRegion("btn_musicmuted_down"));
    checked = new TextureRegionDrawable(localAtlas.findRegion("btn_musicmuted_checked")); 
    musicStatus = new ImageButton(up,down,checked);
    musicStatus.setChecked(game.muteManager.isMusicMuted());
    musicStatus.addListener(this);   
    
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_soundmuted_up"));
    down = new TextureRegionDrawable(localAtlas.findRegion("btn_soundmuted_down"));
    checked = new TextureRegionDrawable(localAtlas.findRegion("btn_soundmuted_checked"));
    soundStatus = new ImageButton(up,down,checked);
    soundStatus.addListener(this);   
    
    up = new TextureRegionDrawable(globalAtlas.findRegion("btn_close_up"));
    down = new TextureRegionDrawable(globalAtlas.findRegion("btn_close_down"));
    exit = new ImageButton(up,down);
    exit.addListener(this);   
    buttons.add(musicStatus).padBottom(20).row();
    buttons.add(soundStatus).padBottom(20).row();
    buttons.add(exit);

    infoGroup.addActor(buttons); 
    
    scrollTable = new ScrollPane(null,((SlotMachine)game).skin);
    setVariant(variation);
    
    //scrollTable.setSize(244,360);
    scrollTable.setOverscroll(true,true);
    //scrollTable.setPosition(18,60);
    scrollTable.setBounds(10,68,320-30-48,334);
    
    if (player==null) {
      // Use as setup screen
      infoGroup.setPosition(0,0);
      deviceGroup.setPosition(stage.getWidth(),0);
    }
    else {
      infoGroup.setPosition(stage.getWidth(),0);
    }
    infoGroup.addActor(scrollTable);
    stage.addActor(deviceGroup);
    stage.addActor(infoGroup);
  }
  
  /**
   * setup the info on the inforscreen
   * @param variant the game to visualize
   * @return a pay table.
   */
  private void setVariant(Variation variant) {
    Table paytable = new Table(((SlotMachine)game).skin);
    for (int x=0;x<variant.paytable.length;x++) {
      for (int y=0;y<variant.paytable[x].length-1;y++) {
        if (variant.paytable[x][y]==-1) {
          // Wild symbol == empty space
          paytable.add().pad(2,2,2,2);
        }
        else {
          Actor actor = new Image(smallSymbols[variant.paytable[x][y]]);
          paytable.add(actor).pad(2,2,8,2);
        }
      }
      paytable.add();
      paytable.add(" = "+variant.paytable[x][3]+" coins").padLeft(10).padRight(10);
      paytable.row();
    }
    paytable.pack();
    this.variation= variant;
    scrollTable.setWidget(paytable);
    deviceName.clear();
    deviceName.add(variant.machineName);
  }
  
  @Override
  public void dispose() {
    super.dispose();
    if (triggerSound!=null) triggerSound.dispose();
    if (ejectCoinSound!=null) ejectCoinSound.dispose();
    if (winSound!=null) winSound.dispose();
    if (reelStopSound!=null) reelStopSound.dispose();
  }
  
  @Override
  public void renderBackground(float delta) {
    Gdx.gl.glClearColor(0.72f, 0.74f, 0.71f, 1);
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
  }
  
  /**
   * Sum up the coins that are currently visible and checked.
   * @return amount of coins bet.
   */
  public int getBet() {
    int ret=0;
    for (int i=0;i<bet.length;i++) {
      if (bet[i].isChecked() && bet[i].isVisible()) ret++;
    }
    return ret;
  }
  
  @Override
  public boolean handle(Event event) {
    if (! (event instanceof InputEvent)) return false;
    
    InputEvent input = (InputEvent) event;
    Actor actor = input.getListenerActor();
    
    Vector2 coords = Vector2.tmp.set(input.getStageX(), input.getStageY());
    event.getListenerActor().stageToLocalCoordinates(coords);
    boolean isOver = actor.hit(coords.x,coords.y,true)!=null;
    
    
    if (isOver && actor==viewSwitch && input.getType().equals(InputEvent.Type.touchUp)) {
      if (viewSwitch.isChecked()) {
        // Note: moveBy takes time in which the user may trigger another moveBy, potentially
        // scrolling the viewport into empty space. As a workaround, make sure, that groups 
        // are where they are suppose to be before doing a relative movement.
        deviceGroup.setX(0);
        infoGroup.setX(stage.getWidth());
        deviceGroup.addAction(Actions.moveBy(-stage.getWidth(),0,0.1f));
        infoGroup.addAction(Actions.moveBy(-stage.getWidth(),0,0.1f));
      }
      else {
        deviceGroup.setX(-stage.getWidth());
        infoGroup.setX(0);
        deviceGroup.addAction(Actions.moveBy(stage.getWidth(),0,0.1f));
        infoGroup.addAction(Actions.moveBy(stage.getWidth(),0,0.1f));
      }
    } 
    
    if (isOver && actor==exit && input.getType().equals(InputEvent.Type.touchUp)) {
      SlotMachine.fadeOverScreen.configure(game,this,new MenuScreen(game),1);
      game.setScreen(SlotMachine.fadeOverScreen);
    }
    
    if (isOver && actor==musicStatus && input.getType().equals(InputEvent.Type.touchUp)) {
      game.muteManager.setMusicMuted(musicStatus.isChecked());
    }
    
    if (isOver && actor==soundStatus && input.getType().equals(InputEvent.Type.touchUp)) {
      game.muteManager.setSoundMuted(soundStatus.isChecked());
    }
    
    if (isOver && actor==nextVariation && input.getType().equals(InputEvent.Type.touchUp)) {
      setVariant(Variation.loadNextVariation());
    }
    
    if (isOver && actor==previousVariation && input.getType().equals(InputEvent.Type.touchUp)) {
      setVariant(Variation.loadNextVariation());
    }
    
    return true;
  }
  
  /**
   * Mark a <code>Symbol</code> as either in motion or at rest.
   * @param symbol the <code>Symbol</code> in question.
   * @param moving true if spinning, false at rest
   */
  protected synchronized void inMotion(Symbol symbol, boolean moving) {
    if (moving) spinning++;
    else spinning--;
    
    // Provide audible feedback for a stopping reel
    if (symbol.isOnPayline() && !moving) {
      player.payline[symbol.getReel()]=symbol.getFace();
      playSoundEffect(REELSTOPSOUND);
    }
    
    // All reels stopped -> evaluate
    if (spinning==0) {
      // A round may be played without betting, so simulate a bet to find out for sure if the player
      // won or lost.
      if (variation.getPayout(1,player.payline)>0) {
        int win = variation.getPayout(player.bet,player.payline);
        // No bet -> no bling
        if (win>0) {
          feedbackMessage.setText("+ "+win);
          // NOTE: Actions from static import
          float centerPos = stage.getWidth()/2-(feedbackMessage.getWidth()+10+feedbackSymbol.getWidth())/2;
          feedbackGroup.addAction(sequence(moveTo(centerPos,85),fadeIn(0.4f),moveBy(0,-50,1f),fadeOut(0.4f)));
          playSoundEffect(WINSOUND);
        }
        // But it still counts towards the statistics
        player.win(win);
      }
      else {
        // Player lost the round
        player.loose();
      }
      
      // Make sure, the player can not bet more coins than s/he has. NOTE: There is no explicit
      // Game Over check. The player just runs into a dead end eventually from which on s/he can
      // only play wagerless rounds. This is intended! The game design calls for free games as a 
      // means of skipping over (suspected) loosing rounds (strategy element).
      // Since this allows the player to play forever by never betting, a Game Over screen would 
      // be meaningless.
      for (int i=0;i<bet.length;i++) {
        bet[i].setVisible(player.credit>i);
      }
      
      player.round++;
      credits.setText("x "+player.credit);
      turns.setText("x "+player.round);

    }
  }
  
  /**
   * Query if the reels are still in motion
   * @return true if the reels are spinning
   */
  public synchronized boolean isSpinning() {
    return spinning!=0;
  }
  
  /**
   * Start a new round.
   * @param velocity how fast to spin the reels.
   */
  public synchronized void newRound(int velocity) {
    if (isSpinning()) return;
    player.gamble(getBet());
    credits.setText("x "+player.credit);
    for (int i=0;i<reelSymbols.length;i++) {
      reelSymbols[i].spin((1+i/3 )* 3 +velocity,velocity);
    }
  }
  
  /**
   * Trigger a sound effect
   * @param which which sound effect to play
   */
  protected void playSoundEffect(int which) {
    if (game.muteManager.isSoundMuted()) return;
    // FIXME: We should probably throttle the music volume a bit to make sfx more noticeable.
    // Problem with that: Sound.play() only triggers the sound effect and returns immediately.
    switch(which) {
      case TRIGGERSOUND: {
        triggerSound.play();
        break;
      }
      case WINSOUND: {
        winSound.play();
        break;
      }
      case EJECTCOINSOUND: {
        ejectCoinSound.play();
        break;
      }
      case REELSTOPSOUND: {
        reelStopSound.play();
        break;
      }
    }
  }
  
  /**
   * Try stopping the wheels. This may or may not succeed (skill element).
   * Wheels are stopped from left to right.
   */
  public synchronized void brakeWheels() {
    switch (spinning) {
      case 1:
      case 2:
      case 3: {
        reelSymbols[6].handbrake();
        reelSymbols[7].handbrake();
        reelSymbols[8].handbrake();
        break;
      }
      case 4:
      case 5:
      case 6: {
        reelSymbols[3].handbrake();
        reelSymbols[4].handbrake();
        reelSymbols[5].handbrake();
        break;
      }
      case 7:
      case 8:
      case 9: {
        reelSymbols[0].handbrake();
        reelSymbols[1].handbrake();
        reelSymbols[2].handbrake();
        break;
      }
      default: {
        return;
      }
    }
  }
}