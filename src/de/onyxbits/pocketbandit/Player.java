package de.onyxbits.pocketbandit;

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
  public Player(Variation variation, int credit) {
    this.variation = variation;
    this.credit=credit;
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
   */
  public void loose() {
    bet=0;
    streakOfBadLuck++;
    streakOfLuck=0;
  }
  
  /**
   * Mark up the current round as won: add the prize to the credits and reset the <code>lostRounds</code>
   * counter.
   * @param prize how much to add to the credits
   * @param jpot true to also payout the jackpot.
   */
  public void win(int prize) {
    credit+=prize;
    streakOfBadLuck=0;
    streakOfLuck++;
  }
  
}
