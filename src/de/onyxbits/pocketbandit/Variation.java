package de.onyxbits.pocketbandit;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.Arrays;

/**
 * Constants that describe symbols, probabilities and payouts.
 */
public class Variation {

  /**
   * Name (and order) of the symbol textures
   */
  public  String symbolNames[];
  
  /**
   * Human readable name of the machine
   */
  public String machineName;
  
  /**
   * Name of the atlas file that contains the graphics.
   */
  public String atlasName = "textures/machineparts.atlas";
  
  /**
   * Symbol distribution. This is a [3][x] array that gives the odds for each symbol
   * per reel. Odds are given as indexes into <code>symbols</code>.
   */
  private int[][] weightTable;
  
  /**
   * Matrix for the pay schedule. Every row is one rule. Rules with lower index have higher
   * priority. The first rule that matches is to be taken (that is: rules containing wilds
   * should be put at the end of the table). Every rule has four columns. The first three
   * columns contain indexes into <code>symbols</code> that are matched against the payline,
   * the fourth is the payout per coin bet.
   * <p>
   * The first three columns may contain the value -1 to signal "do not care" (-1 matches
   * every symbol on the reel).
   */
  public int[][] paytable;
  
  /**
   * How much money the player should be given initially for this playstyle
   */
  public int seedCapital=0;
  
  /**
   * For debugging: not so random random picks
   */
  private int[] symbolSequence;
  
  /**
   * For debuggin: which random pick to pick next
   */
  private int symbolSequenceIndex;
  
  /**
   * The key name for storing persisting the default variation
   */
  private static final String KEYNAME = "rulefile";
  
  public Variation() {}
  
  /**
   * Helper function for expanding a compact weight table into a real one (Not used in
   * the actual game, just for creating new rulesets).
   * @param compact the compact weight table of one reel. This must align with <code>symbolNames</code>.
   * Each element is the amount of the according symbol on the reel.
   * @return the expanded weight table
   */
  private static int[] toWeightTable(int... compact) {
    int len = compact.length;
    int sum=0;
    for (int i=0;i<len;i++) {
      sum+=compact[i];
    }
    
    int[] ret= new int[sum];
    int pos=0;
    
    for (int i=0;i<len;i++) {
      int tmp=compact[i];
      while(tmp>0) {
        ret[pos]=i;
        pos++;
        tmp--;
      }
    }
    return ret;
  }
  
  /**
   * Match a payline against the <code>paytable</code>.
   * @param payline the three symbols on the payline (index into <code>symbolNames</code>
   * @return index into paytable or -1 if no rule matched. If several rules match, the first
   * one matching is returned (lower index=higher priority).
   */
  private int match(int[] payline) {
    for (int x=0;x<paytable.length;x++) {
      if (payline[0]==paytable[x][0] || paytable[x][0]==-1) {
        if (payline[1]==paytable[x][1] || paytable[x][1]==-1) {
          if (payline[2]==paytable[x][2] || paytable[x][2]==-1) {
            return x;
          }
        }
      }
    }
    return -1;
  }
  
  /**
   * Calculate the payout for a given payline
   * @param bet how many coins (0-3) were bet
   * @param payline the three symbols on the payline (index into <code>symbolNames</code>
   * @return amount of coins won (a bet of 0 always wins 0).
   */
  public int getPayout(int bet, int[] payline) {
    int tmp=match(payline);
    return tmp==-1 ? 0 : paytable[tmp][3]*bet;
  }
  
  /**
   * Randomly select a new symbol.
   * @param reel which <code>weightTable</code> (0-2) to take probilities from.
   * @return the rolled image as an index into <code>symbolNames</code>
   */
  public int pick(int reel) {
    int ret = 0;
    if (symbolSequence!=null && symbolSequenceIndex<symbolSequence.length) {
      ret = symbolSequence[symbolSequenceIndex];
      symbolSequenceIndex++;
    }
    else {
      int idx = SlotMachine.rng.nextInt(weightTable[reel].length);
      ret=weightTable[reel][idx];
    }
    return ret;
  }
  
  /**
   * Query symbol faces to show on the reels initially.
   * @return 3x3 symbol faces (index into <code>symbolNames</code>) packed into a single
   * array. A new reel begins every three symbols, within the reel, symbols are ordered from
   * bottom to top.
   */
  public int[] getInitialFaces() {
    // Note: to make things easy, we just show winning combinations, so there
    // need to be at least three rules at the start of the paytable that don't
    // contain jokers.
    // Also note: This assumes that the best paying combo is the first in the
    // paytable (and should be shown on the payline).
    int[] ret = {
      paytable[1][0],
      paytable[0][0],
      paytable[2][0],
      
      paytable[1][1],
      paytable[0][1],
      paytable[2][1],
      
      paytable[1][2],
      paytable[0][2],
      paytable[2][2],
    };
    return ret;
  }

  /**
   * List all variations
   * @return the names of the available rule files (sorted alphabetically)
   */
  public static String[] listVariations() {
    FileHandle fh[] = Gdx.files.internal("rules").list();
    String[] ret = new String[fh.length];
    for (int i=0;i<fh.length;i++) {
      ret[i]=fh[i].path();
    }
    Arrays.sort(ret);
    return ret;
  }
  
  /**
   * Load serialized <code>Variation</code> description from a JSON file
   * @param fh the file to read from
   * @return the reconstructed <code>Variation</code>
   */
  public static Variation loadVariation(FileHandle fh) {
    return new Json().fromJson(Variation.class,fh);
  }
  
  /**
   * Load the default variation
   * @return a variation.
   */
  public static Variation loadDefaultVariation() {
    String[] rules = listVariations();
    return loadVariation(Gdx.files.internal(Gdx.app.getPreferences(SlotMachine.PREFSNAME).getString(KEYNAME,rules[0])));
  }
  
  /**
   * Make the next in list variation the default one and load it
   * @return the variation that comes alphabetically after the current one
   */
  public static Variation loadNextVariation() {
    String[] rules = listVariations();
    Preferences prefs = Gdx.app.getPreferences(SlotMachine.PREFSNAME);
    String current = prefs.getString(KEYNAME,rules[0]);
    for (int i=0;i<rules.length;i++) {
      if (rules[i].equals(current)) {
        i++;
        if (i>=rules.length) i=0;
        prefs.putString(KEYNAME,rules[i]);
        prefs.flush();
        break;
      }
    }
    return loadDefaultVariation();
  }
  
  /**
   * Make the previous in list variation the default one and load it
   * @return the variation that comes alphabetically before the current one
   */
  public static Variation loadPreviousVariation() {
    String[] rules = listVariations();
    Preferences prefs = Gdx.app.getPreferences(SlotMachine.PREFSNAME);
    String current = prefs.getString(KEYNAME,rules[0]);
    for (int i=0;i<rules.length;i++) {
      if (rules[i].equals(current)) {
        i--;
        if (i<=0) i=rules.length-1;
        prefs.putString(KEYNAME,rules[i]);
        prefs.flush();
        break;
      }
    }
    return loadDefaultVariation();
  }
}