package de.onyxbits.pocketbandit;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;

import de.onyxbits.bureauengine.*;
import de.onyxbits.bureauengine.audio.*;
import de.onyxbits.bureauengine.screen.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

/**
 * The platform independent entry point of the game
 */
public class SlotMachine extends BureauGame {
  
  /**
   * The name to use for <code>Gdx.app.getPreferences</code>
   */
  public static final String PREFSNAME = "PocketBandit";
  
  public Skin skin;
  
  protected MuteManager createMuteManager() {
    MuteManager ret = new MuteManager();
    ret.persistWith(Gdx.app.getPreferences(PREFSNAME),"MuteManager.musicMuted","MuteManager.soundMuted");
    return ret;
  }
  
  protected BureauScreen createStartUpScreen() {
    assetManager.load("textures/global.atlas",TextureAtlas.class);
    assetManager.finishLoading();
    TextureAtlas globalAtlas= assetManager.get("textures/global.atlas",TextureAtlas.class);
    BitmapFont defaultFont = new BitmapFont();
    skin = new Skin(globalAtlas);
    Window.WindowStyle windowStyle = new Window.WindowStyle();
    windowStyle.titleFont = defaultFont;
    windowStyle.background= new NinePatchDrawable(new NinePatch(globalAtlas.findRegion("roundbox_grey"),8,8,8,8));
    skin.add("default",windowStyle);
    
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font= defaultFont;
    labelStyle.fontColor = Color.BLACK;
    skin.add("default",labelStyle);
    
    ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
    scrollPaneStyle.background = new NinePatchDrawable(new NinePatch(globalAtlas.findRegion("roundbox_grey"),8,8,8,8));
    skin.add("default",scrollPaneStyle);
    
    // NOTE: -Dvariant=<file> to fastboot to a GambleScreen - for debugging
    String variant=System.getProperty("variant",null);
    
    // NOTE: -Dsequence=1,2,3,4,.. to preload a non random sequence - for debugging
    int[] symbolSequence = null;
    String[] tmpSeq= System.getProperty("sequence","").split(",");
    if (!tmpSeq[0].equals("") && !tmpSeq[0].equals("+")) {
      symbolSequence= new int[tmpSeq.length];
      for(int i=0;i<tmpSeq.length;i++) {
        symbolSequence[i]=Integer.parseInt(tmpSeq[i]);
      }
    }
    // NOTE: -Dsequence=+,1000,0 to put 1000 times the 0 into the sequence - for debugging
    if (!tmpSeq[0].equals("") && tmpSeq[0].equals("+")) {
      symbolSequence= new int[Integer.parseInt(tmpSeq[1])];
      for(int i=0;i<tmpSeq.length;i++) {
        symbolSequence[i]=Integer.parseInt(tmpSeq[2]);
      }
    }
    
    if (variant==null) {
      return new MenuScreen(this);
    }
    else {
      Variation v =  Variation.loadVariation(Gdx.files.internal(variant));
      Player p = new Player(v);
      return new GambleScreen(this,p,v);
    }
  }
  
  @Override
  public void dispose() {
    skin.dispose();
    super.dispose();
  }
}
