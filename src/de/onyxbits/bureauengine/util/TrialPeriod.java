package de.onyxbits.bureauengine.util;

import com.badlogic.gdx.Preferences;

/**
 * Utility class for giving the user time to test something for a reasonable
 * number of times before having to make a decission. This class is designed
 * for reminding the user. It is not fit for licensing.
 * <p>
 * The idea of the <code>TrialPeriod</code> is that a counter get's increased
 * everytime the app is started (call <code>trialed()</code> from the boot
 * code of the game). After a reasonable amount of time and launches, 
 * <code>isOver()</code> will return <code>true</code>, signalling that 
 * further actions can be taken.<br>
 * Additionally, the result of the <code>TrialPeriod</code> can be persisted
 * in a state variable. The state, however, is not evaluated internally.
 * <p>
 * Sometimes it is desirable to extend a trial period. The easiest way to do
 * this is to call the <code>reset()</code> method which will simply wipe
 * all data from the persistent storage, allowing for a completely fresh start.
 * The more sophisticated approach is to use two trial objects (with two different
 * key sets) and leave the first one in state <code>ENDED</code>.
 */
public class TrialPeriod {

  /**
   * Unknown state
   */
  public static final int UNKNOWN = 0;
  
  /**
   * Positive state
   */
  public static final int POSITIVE = 1;
  
  /**
   * Negative state
   */
  public static final int NEGATIVE = 2;
  
  /**
   * Currently on trial
   */
  public static final int INPROGRESS = 4;
  
  /**
   * Trial period ended with undetermined result
   */
  public static final int ENDED = 8;
  
  /**
   * Trial ended with positive user feed back
   */
  public static final int ENDEDPOSITIVELY = ENDED | POSITIVE;
  
  /**
   * Trial ended with negative user feedback
   */
  public static final int ENDEDNEGATIVELY = ENDED | NEGATIVE;

  private Preferences prefs;
  private boolean directFlush;
  private String keyLaunchCount;
  private String keyFirstLaunch;
  private String keyState;
  private long minLaunches = 10;
  private long minTime = 3 * 24 * 60 * 60 * 1000; // Three days

  /**
   * Configure the persistant storage.
   * @param prefs persistent storage
   * @param directFlush whether or not to call <code>Preferences.flush()</code>.
   * @param keyLaunchCount name of the key with which to count how often the game was started
   * @param keyFirstLaunch name of the key with which to record the date of the first launch
   * @param keyActive name of the key with which to store the state.
   */
  public TrialPeriod(Preferences prefs, boolean directFlush, String keyLaunchCount, String keyFirstLaunch, String keyState) {
    if (prefs==null || keyLaunchCount==null || keyFirstLaunch == null || keyState==null) {
      throw new NullPointerException(); // Crash early
    }
    this.prefs=prefs;
    this.directFlush = directFlush;
    this.keyLaunchCount=keyLaunchCount;
    this.keyFirstLaunch=keyFirstLaunch;
    this.keyState=keyState;
  }
  
  /**
   * Configure the minimum amount of time that has to pass since the first launch before
   * <code>isOver()</code> will return true;
   * @param minTime time in milliseconds
   * @return the object for method chaining.
   */
  public TrialPeriod withMinTime(long minTime) {
    this.minTime=minTime;
    return this;
  }
  
  /**
   * Configure the minimum amount of launches that have to occur before <code>isOver()</code>
   * returns true
   * @param minLauncher number of times the game has to be started in addition to minimum time.
   * @return the object for method chaining.
   */
  public TrialPeriod withMinLaunches(long minLaunches) {
    this.minLaunches=minLaunches;
    return this;
  }
  
  /**
   * Check if the trialperiod is over
   * @return true if the time is over and the minimum number of launches have occured
   */
  public boolean isOver() {
    long fl = prefs.getLong(keyFirstLaunch,-1);
    long lc = prefs.getLong(keyLaunchCount,-1);
    if (lc >= minLaunches && fl+minTime <= System.currentTimeMillis()) {
      return true;
    }
    else {
      return false;
    }
  }
  
  /**
   * Should be called once everytime the application is started
   * @return the object for method chaining.
   */
  public TrialPeriod trialed() {
    long tmp = prefs.getLong(keyFirstLaunch,0);
    if (tmp==0) {
      prefs.putLong(keyFirstLaunch,System.currentTimeMillis());
    }
    tmp = prefs.getLong(keyLaunchCount,0);
    prefs.putLong(keyLaunchCount,tmp+1);
    if (directFlush) prefs.flush();
    return this;
  }
  
  /**
   * Restart the trialcounter by removing all the keys from the storage.
   */
  public void reset() {
    prefs.remove(keyLaunchCount);
    prefs.remove(keyFirstLaunch);
    prefs.remove(keyState);
    if (directFlush) prefs.flush();
  }
  
  /**
   * Query the state of the <code>TrialPeriod</code>
   * @return integer encoded state. If no state has been set yet, this
   * will be UNKNOWN.
   */
  public int getState() {
    return prefs.getInteger(keyState,UNKNOWN);
  }
  
  /**
   * Set the state of the trial. Note: The state is not evaluated internally.
   * It is recommended to use the predefined states, but not nescessary.
   * @param int encoded state.
   */
  public void setState(int state) {
    prefs.putInteger(keyState,state);
    if (directFlush) prefs.flush();
  }
}