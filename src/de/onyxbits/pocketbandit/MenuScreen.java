package de.onyxbits.pocketbandit;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.math.*;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import de.onyxbits.bureauengine.screen.*;
import de.onyxbits.bureauengine.*;
import de.onyxbits.bureauengine.util.*;

/**
 * Shown on startup. The user may start a new game here, see the credits, change settings. etc.
 */
public class MenuScreen<T extends SlotMachine> extends BureauScreen<T> implements EventListener {
  
  private Button startGame;
  private Button showCredits;
  private Button settings;
  
  private Button trialYes;
  private Button trialNo;
  private Button trialLater;
  
  private TextureRegion background;
  private TextureAtlas localAtlas;
  private TextureAtlas globalAtlas;
  private int offset=-64;
  private int scale=0;
  
  
  //private SlotMachine slotGame; // Alias for BuereauScreen.game with proper type.
  
  private static final AssetDescriptor[] ASSETS = {
    new AssetDescriptor<Music>("music/Pinball Spring.mp3",Music.class), 
    new AssetDescriptor<TextureAtlas>("textures/menuscreen.atlas",TextureAtlas.class),
  };
  
  public MenuScreen(T game) {
    super(game);
  }
  
  protected AssetDescriptor[] getAssets() {
    return ASSETS;
  }
  
  public void readyScreen() {
    this.stage = new Stage(320, 480, true,game.spriteBatch);
    
    Table layout = new Table();
    layout.setBounds(0,0,stage.getWidth(),stage.getHeight());
    layout.pad(20);
    layout.debugCell();
    music = game.assetManager.get("music/Pinball Spring.mp3",Music.class);
    music.setLooping(true);
    localAtlas = game.assetManager.get("textures/menuscreen.atlas",TextureAtlas.class);
    globalAtlas = game.assetManager.get("textures/global.atlas",TextureAtlas.class);
    background = globalAtlas.findRegion("checkered");
    Image logo = new Image(localAtlas.findRegion("spr_logo"));
    Drawable up,down;
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_play_up"));
    down = new TextureRegionDrawable(localAtlas.findRegion("btn_play_down"));
    startGame = new ImageButton(up,down);
    
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_credits_up"));
    down = new TextureRegionDrawable(localAtlas.findRegion("btn_credits_down"));
    showCredits = new ImageButton(up,down);
    
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_settings_up"));
    down = new TextureRegionDrawable(localAtlas.findRegion("btn_settings_down"));
    settings = new ImageButton(up,down);

    layout.add(logo).top().row();
    layout.add().pad(20).row();
    layout.add(startGame).padBottom(10).row();
    layout.add(settings).padBottom(10).row();
    layout.add(showCredits).row();
    layout.add().pad(40); // Just to push it up a little
    
    layout.layout();
    
    stage.addActor(layout);
    if (game.trialPeriod!=null && game.trialPeriod.getState()==TrialPeriod.INPROGRESS && 
    game.trialPeriod.isOver()) {
      buildFeedbackDialog().show(stage);
    }
    showCredits.addListener(this);
    startGame.addListener(this);
    settings.addListener(this);
  }
  
  public boolean handle (Event event){
    if (! (event instanceof InputEvent)) return false;
    
    InputEvent input = (InputEvent) event;
    Actor actor = input.getListenerActor();
    
    Vector2 coords = Vector2.tmp.set(input.getStageX(), input.getStageY());
    event.getListenerActor().stageToLocalCoordinates(coords);
    boolean isOver = actor.hit(coords.x,coords.y,true)!=null;
    
    
    if (isOver && actor==startGame && input.getType().equals(InputEvent.Type.touchUp)) {
      Variation v = game.loader.getDefault();
      Player p = game.loader.getPlayer(v);   
      p.reVisit();
      SlotMachine.fadeOverScreen.fadeTo(new GambleScreen<SlotMachine>(game,p,v),0.5f);
    }
    
    if (isOver && actor==showCredits && input.getType().equals(InputEvent.Type.touchUp)) {
      buildCreditsDialog().show(stage);
    }
    
    if (isOver && actor==settings && input.getType().equals(InputEvent.Type.touchUp)) {
      Variation v = game.loader.getDefault();
      SlotMachine.fadeOverScreen.fadeTo(new GambleScreen<SlotMachine>(game,null,v),0.5f);
    }
    
    if (isOver && actor==trialLater && input.getType().equals(InputEvent.Type.touchUp)) {
      // We just wipe all the data from the trial -> The extension is granted by completely
      // restarting the trial.
      game.trialPeriod.reset();
    }
    
    if (isOver && (actor==trialNo || actor==trialYes) && input.getType().equals(InputEvent.Type.touchUp)) {
      // We don't really care how the user decided. It's sufficient to jot down that the 
      // decission is final.
      game.trialPeriod.setState(TrialPeriod.ENDED);
    }
    
    return true;
  }
  
  private Dialog buildFeedbackDialog() {
    Dialog ret = new Dialog("", ((SlotMachine)game).skin);
    // FIXME: word wrapping seems to be broken in Label s (either that or I can't figure out
    // how to do it properly). So for the time being: use \n
    ret.getContentTable().add("Hi,\nseems like you are enjoying this game.\nWould you like to help encourage\nfurther development? All you have to\ndo is to rate or review Pocket Bandit\non Google Play.");
    Drawable up, down;
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_sure_up"));
    down = new TextureRegionDrawable(localAtlas.findRegion("btn_sure_down"));
    trialYes = new ImageButton(up,down);
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_noway_up")); 
    down = new TextureRegionDrawable(localAtlas.findRegion("btn_noway_down"));
    trialNo = new ImageButton(up,down);
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_later_up"));
    down = new TextureRegionDrawable(localAtlas.findRegion("btn_later_down"));
    trialLater = new ImageButton(up,down);
    game.linkHandler.register(trialYes,"market://details?id=de.onyxbits.pocketbandit");
    trialYes.addListener(this);
    trialNo.addListener(this);
    trialLater.addListener(this);
    ret.getButtonTable().add(trialYes);
    ret.getButtonTable().add(trialNo);
    ret.getButtonTable().add(trialLater);
    return ret;
  }
  
  private Dialog buildCreditsDialog() {
    Dialog ret = new Dialog("", ((SlotMachine)game).skin);
    Table content = ret.getContentTable();
    Drawable up, down;
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_external"));
    ImageButton linkButton;
    
    
    content.setSkin(((SlotMachine)game).skin);
    content.align(Align.left);
    content.add("Code").left();
    content.row();
    content.add();
    content.add("Patrick Ahlbrecht").left();
    content.add(game.linkHandler.register(new ImageButton(up),"http://www.onyxbits.de/pocketbandit/"));
    content.row();
    content.add();
    content.add("LibGDX Project").left();
    content.add(game.linkHandler.register(new ImageButton(up),"http://libgdx.badlogicgames.com/"));
    content.row();
    content.add("Graphics").left();
    content.row();
    content.add();
    content.add("Patrick Ahlbrecht").left();
    content.row();
    content.add();
    content.add("Astrid Denisse").left();
    content.row();
    content.add("Music").left();
    content.row();
    content.add();
    content.add("Kevin MacLeod").left();
    content.add(game.linkHandler.register(new ImageButton(up),"http://incompetech.com/"));
    content.row();
    content.add("Sfx").left();
    content.row();
    content.add();
    content.add("bradwesson").left();
    content.add(game.linkHandler.register(new ImageButton(up),"http://www.freesound.org/people/bradwesson/"));
    content.row();
    content.add();
    content.add("q-k").left();
    content.add(game.linkHandler.register(new ImageButton(up),"http://www.freesound.org/people/q-k/"));
    content.row();
    content.add();
    content.add("Mike Koenig").left();
    content.add(game.linkHandler.register(new ImageButton(up),"http://soundbible.com/"));
    
    // Note: the Dialog class handles buttons internally and closes by default when one is pressed,
    // so we don't need to to anything except adding one.
    up = new TextureRegionDrawable(globalAtlas.findRegion("btn_close_up"));
    down = new TextureRegionDrawable(globalAtlas.findRegion("btn_close_down")); 
    ret.getButtonTable().add(new ImageButton(up,down));
    return ret;
  }
  
  public void renderBackground(float delta) {
    game.spriteBatch.begin();
    for (int x=offset;x<Gdx.graphics.getWidth();x+=64) {
      for (int y=offset;y<Gdx.graphics.getHeight();y+=64) {
        game.spriteBatch.draw(background,x,y);
      }
    }
    scale++;
    if (scale==2) {
      offset+=1;
      if (offset==0) {
        offset=-64;
      }
      scale=0;
    }
    game.spriteBatch.end();
  }
  
    
  @Override
  public void hide() {
  }
}