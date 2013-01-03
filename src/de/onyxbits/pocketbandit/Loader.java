package de.onyxbits.pocketbandit;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.Arrays;

/**
 * Handles access to the <code>Variation</code> rule definition files.
 */
class Loader {

  /**
   * The directory (relative to the assets) that contains the rules definition
   */
  public static final String RULESDIR = "rules";
  
  /**
   * The key with which we persist the players chosen machine
   */
  private static final String KEYNAME = "rulefile";
  
  private String[] rules;
  private int index;
  private Variation[] variations;
  private Preferences prefs;
  private Json json;
  private Player[] players;
  
  public Loader(Preferences prefs) {
    if (prefs==null) throw new NullPointerException();
    this.prefs=prefs;
    json = new Json();
  }
  
  /**
   * Scan the rules definition directory
   */
  public void rescan() {
    FileHandle fh[] = Gdx.files.internal(RULESDIR).list();
    rules = new String[fh.length];
    variations = new Variation[fh.length];
    players = new Player[fh.length];
    for (int i=0;i<fh.length;i++) {
      rules[i]=fh[i].path();
    }
    // Note: file- and variationname are not connected. By convention, filenames should be
    // the lowercase version of the variation name with underscores replacing the spaces.
    Arrays.sort(rules);
    index=0;
    String name = prefs.getString(KEYNAME,rules[0]);
    for (int i=0;i<rules.length;i++) {
      if (rules[i].equals(name)) {
        index=i;
        break;
      }
    }
  }
  
  /**
   * Returns the default <code>Variation</code>
   * @return either the configured <code>Variation</code> or the first one we can find.
   */
  public Variation getDefault() {
    if (variations[index]==null) {
      variations[index]=json.fromJson(Variation.class,Gdx.files.internal(rules[index]));
    }
    return variations[index];
  }
  
  /**
   * Get the cached player object for a variation
   * @param v key
   * @return value or null if key is not found.
   */
  public Player getPlayer(Variation v) {
    for(int i=0;i<variations.length;i++) {
      if (variations[i]==v) {
        if (players[i]==null) {
          players[i]= new Player(v);
        }
        return players[i];
      }
    }
    return null;
  }
  
  /**
   * Return the next <code>Variation</code> in the ring buffer
   * @return a <code>Variation</code>
   */
  public Variation next() {
    index++;
    if (index>=rules.length) {
      index=0;
    }
    prefs.putString(KEYNAME,rules[index]);
    if (variations[index]==null) {
      variations[index]=json.fromJson(Variation.class,Gdx.files.internal(rules[index]));
    }
    return variations[index];
  }
  
  /**
   * Return the next <code>Variation</code> in the ring buffer
   * @return a <code>Variation</code>
   */
  public Variation previous() {
    index--;
    if (index<=0) {
      index=rules.length-1;
    }
    prefs.putString(KEYNAME,rules[index]);
    if (variations[index]==null) {
      variations[index]=json.fromJson(Variation.class,Gdx.files.internal(rules[index]));
    }
    return variations[index];
  }
}