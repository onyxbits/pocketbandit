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


/**
 * Shown on startup. The user may start a new game here, see the credits, change settings. etc.
 */
public class MenuScreen extends BureauScreen implements EventListener {
  
  private Button startGame;
  private Button showCredits;
  private Button settings;
  private TextureRegion background;
  private TextureAtlas localAtlas;
  private TextureAtlas globalAtlas;
  private int offset=-64;
  private int scale=0;
  private FadeOverScreen fadeOverScreen;
  
  private static final AssetDescriptor[] ASSETS = {
    new AssetDescriptor<Music>("music/Pinball Spring.mp3",Music.class), 
    new AssetDescriptor<TextureAtlas>("textures/menuscreen.atlas",TextureAtlas.class),
  };
  
  public MenuScreen(BureauGame game) {
    super(game);
  }
  
  protected AssetDescriptor[] getAssets() {
    return ASSETS;
  }
  
  public void readyScreen() {
    this.stage = new Stage(320, 480, true,game.spriteBatch);
    fadeOverScreen = new FadeOverScreen();
    
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
      Variation v = Variation.loadDefaultVariation();
      Player p = new Player(v,v.seedCapital);   
      fadeOverScreen.configure(game,this,new GambleScreen(game,p,v),1);
      game.setScreen(fadeOverScreen);
    }
    
    if (isOver && actor==showCredits && input.getType().equals(InputEvent.Type.touchUp)) {
      buildCreditsDialog().show(stage);
    }
    
    if (isOver && actor==settings && input.getType().equals(InputEvent.Type.touchUp)) {
      Variation v = Variation.loadDefaultVariation();
      fadeOverScreen.configure(game,this,new GambleScreen(game,null,v),1);
      game.setScreen(fadeOverScreen);
    }
    
    return true;
  }
  
  private Dialog buildCreditsDialog() {
    Dialog ret = new Dialog("", ((SlotMachine)game).skin);
    Table content = ret.getContentTable();
    Drawable up, down;
    up = new TextureRegionDrawable(localAtlas.findRegion("btn_external"));
    ImageButton linkButton;
    LinkHandler linkHandler = new LinkHandler();
    
    content.setSkin(((SlotMachine)game).skin);
    content.align(Align.left);
    content.add("Code").left();
    content.row();
    content.add();
    content.add("Patrick Ahlbrecht").left();
    content.add(linkHandler.register(new ImageButton(up),"http://www.onyxbits.de"));
    content.row();
    content.add();
    content.add("LibGDX Project").left();
    content.add(linkHandler.register(new ImageButton(up),"http://libgdx.badlogicgames.com/"));
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
    content.add(linkHandler.register(new ImageButton(up),"http://incompetech.com/"));
    content.row();
    content.add("Sfx").left();
    content.row();
    content.add();
    content.add("bradwesson").left();
    content.add(linkHandler.register(new ImageButton(up),"http://www.freesound.org/people/bradwesson/"));
    content.row();
    content.add();
    content.add("q-k").left();
    content.add(linkHandler.register(new ImageButton(up),"http://www.freesound.org/people/q-k/"));
    content.row();
    content.add();
    content.add("creek23").left();
    content.add(linkHandler.register(new ImageButton(up),"http://www.freesound.org/people/creek23/"));
    content.row();
    content.add();
    content.add("ehproductions").left();
    content.add(linkHandler.register(new ImageButton(up),"http://www.freesound.org/people/ehproductions/"));
    content.row();
    content.add();
    content.add("Mike Koenig").left();
    content.add(linkHandler.register(new ImageButton(up),"http://soundbible.com/"));
    
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
  
}