package de.onyxbits.pocketbandit;

import com.badlogic.gdx.backends.android.AndroidApplication;
import android.view.View;


public class AndroidGame extends AndroidApplication {
  
  public void onCreate (android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initialize(new SlotMachine(), false);
  }
}
