package com.suavelemon.orbrush;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
//import com.suavelemon.orbrush.OrbRushView.myAdListener;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class OrbRushActivity extends ActionBarActivity {
	
	public static final String PREFERENCES_NAME = "MyPreferences";
	public boolean soundEnabled = true;
	public int highestcombo = 0;
	
	private OrbRushView myOrbRushView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.orbrush_layout);
		myOrbRushView = (OrbRushView)findViewById(R.id.orb);
		myOrbRushView.setKeepScreenOn(true);
		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
		highestcombo = settings.getInt("highestcombo", 0);
		myOrbRushView.highestCombo = highestcombo;
		soundEnabled = settings.getBoolean("soundSetting", true);
		myOrbRushView.soundOn = soundEnabled;



	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		//MenuItem gameSensitivity = menu.add(0, GAME_SENSITIVITY, 0, "Sensitivity");
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		/*switch(item.getItemId()) {
		case GAME_SENSITIVITY:

		}*/
		return true;
	}




}
