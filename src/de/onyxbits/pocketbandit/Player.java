package de.onyxbits.pocketbandit;

import com.badlogic.gdx.*;


/**
 * Game state. Note: global variables are protected for performance sake. 
 * They should not be modified externally.
 */
public class Player {

  /**
   * How many games were played so far?
   */
  protected int round;
  
  /**
   * How much money does the player currently have on hand?
   */
  protected int credit;
  
  /**
   * All time highscore for the currently played variation
   */
  protected int highscore;
  
  /**
   * Number of coins the player is currently betting.
   */
  protected int bet;
  
  /**
   * Game variant
   */
  protected Variation variation;
  
  /**
   * How many rounds the player has lost in a row
   */
  protected int streakOfBadLuck;
  
  /**
   * How many rounds the player has won in a row
   */
  protected int streakOfLuck;
  
  /**
   * How often did the player play in a row without betting?
   */
  protected int freeloaderCount;
  
  /**
   * <code>Symbol</code>S on the payline. Only stable when the machine is idle.
   */
  protected int[] payline = new int[3];
  
  
  /**
   * Construct a new game state
   * @param variation rules to use
   * @param credit Cash on hand
   */
  public Player(Variation variation) {
    if (variation==null) throw new NullPointerException(); // Crash early
    this.variation = variation;
    // We start out with either the seed capital or the previous winnings. Whichever is higher
    credit=Math.max(variation.seedCapital,SlotMachine.prefs.getInteger(toKey(true),0));
    highscore=SlotMachine.prefs.getInteger(toKey(false),credit);
  }
  
  /**
   * Transform a varition name into a key name (for persisting credits and highscore)
   * @param ch true to get the key for saving cretis, false to get the key for highscores.
   * @return the variation's name in lowercase and with underscores replacing spaces and a suffix
   * accoring to ch.
   */
  private String toKey(boolean ch) {
    if (ch) {
      return "variation."+variation.machineName.replace(" ","_").toLowerCase()+".credits";
    }
    else {
      return "variation."+variation.machineName.replace(" ","_").toLowerCase()+".highscore";
      
    }
  }
  
  /**
   * Make a bet (must be called before setting the reels in motion): Transfer funds from
   * the cash on hand into the escalator, adjust the <code>freeloadCounter</code> accordingly.
   * @param amount number of coins to transfer from <code>credit</code> to <code>bet</code>
   */
  public void gamble(int amount) {
    if (amount==0) {
      freeloaderCount++;
    }
    else {
      freeloaderCount=0;
    }
    bet=amount;
    credit-=bet;
  }
  
  /**
   * Mark up the current round as lost.
   * Note: credits and highscroe are persisted, but preferences have to be flushed externally.
   */
  public void loose() {
    bet=0;
    streakOfBadLuck++;
    streakOfLuck=0;
    SlotMachine.prefs.putInteger(toKey(true),credit);
    SlotMachine.prefs.putInteger(toKey(false),highscore);
  }
  
  /**
   * Mark up the current round as won: add the prize to the credits and reset the <code>lostRounds</code>
   * counter. Note: credits and highscroe are persisted, but preferences have to be flushed externally.
   * @param prize how much to add to the credits
   * @param jpot true to also payout the jackpot.
   */
  public void win(int prize) {
    credit+=prize;
    streakOfBadLuck=0;
    streakOfLuck++;
    if (credit>highscore) highscore=credit;
    SlotMachine.prefs.putInteger(toKey(true),credit);
    SlotMachine.prefs.putInteger(toKey(false),highscore);
  }
  
}
