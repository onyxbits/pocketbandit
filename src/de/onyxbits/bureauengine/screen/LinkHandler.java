package de.onyxbits.bureauengine.screen;

import java.util.HashMap;
import android.content.Intent;
import android.app.Activity;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;

/**
 * An event listener that can be bound to buttons to open websites in a webbrowser
 * when they are pressed.
 */
public class LinkHandler extends ClickListener {

  /**
   * Note: there is a <code>java.net.URI</code> and a <code>android.net.Uri</code>
   * class which are both needed, so we can only store addresses as strings
   */
  private HashMap<Actor,String> linkMap;
  
  public LinkHandler() {
    linkMap = new HashMap<Actor,String>();
  }
  
  /**
   * Register this handler on a button as an eventlistener and assign an uri to open
   * when the <code>Actor</code> is clicked.
   * @param actor the <code>Actor</code> in question.
   * @param uri the url to open when the <code>actor</code> is clicked or null to deregister
   * @return the actor object that was passed in.
   */
  public Actor register(Actor actor, String uri) {
    if (uri==null) {
      actor.removeListener(this);
    }
    else {
      actor.addListener(this);
    }
    linkMap.put(actor,uri);
    return actor;
  }
  
  @Override
  public void clicked(InputEvent event, float x, float y)  {
    String uri = linkMap.get(event.getListenerActor());
    if (uri==null) {
      Gdx.app.log("BuereauEngine","No url registered to open");
      return;
    }
    
    switch (Gdx.app.getType()) {
      case Android: {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(uri));
        ((Activity)Gdx.app).startActivity(browserIntent);
        break;
      }
      case Desktop: {
        try {
          //java.awt.Desktop.getDesktop().browse(new java.net.URI(uri));
        }
        catch (Exception e) {
          Gdx.app.error("BureauEngine","Error opening url",e);
        }
        break;
      }
    }
  }
  
}