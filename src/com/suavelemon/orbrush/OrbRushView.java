package com.suavelemon.orbrush;



import java.util.Random;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
//import com.google.android.gms.samples.ads.R;


public class OrbRushView extends SurfaceView implements SurfaceHolder.Callback{

	private Context myContext;
	private SurfaceHolder mySurfaceHolder;
	private Bitmap backgroundImg;
	private int screenW = 1;
	private int screenH = 1;
	private boolean running = false;
	private boolean onTitle = true;
	private OrbRushThread thread;
	private Bitmap centerOrb;
	private Bitmap orb;
	private int backgroundOrigW;
	private int backgroundOrigH;
	private float scaleW;
	private float scaleH;
	private int origTouchY;
	private float drawScaleW;
	private float drawScaleH;
	private Matrix matrix = new Matrix();
	private int movement = 0;
	private int orbSide = 0;
	private float orbX;
	private float orbY;
	private int orbCycles = 50;
	public boolean orbNeeded = false;
	private float orbStepSizeX = 0;
	private float orbStepSizeY = 0;
	Random rand = new Random();
	private float sensitivity = (float)0.35;
	private int orbColor = 1;
	private Bitmap settingsGears;
	private float totalRotation = 0;
	private int orbCombo = 0;
	private double orbAngle = 0;
	//private boolean orbChecked = false;
	private Paint blackPaint;
	private float scale;
	//private int orbScore = 0;
	protected long startTime = 0;
	protected long elapsedTime = 0;
	private Bitmap startButton;
	public boolean adLoaded = false;
	private boolean startPressed = false;

	private int orbLives = 1;
	private boolean gameOver = false;
	private Bitmap gameOverMessage;
	public int highestCombo = 0;
	private static SoundPool sounds;
	private static int hitSound;
	private static int missSound;
	private static int hitSound2;
	public boolean soundOn = true;
	public static final String PREFERENCES_NAME = "MyPreferences";

	
	public OrbRushView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		thread = new OrbRushThread(holder, context, new Handler() {
			@Override
			public void handleMessage(Message m) {
				
			}
		});
		setFocusable(true);
	}
	
	public OrbRushThread getThread() {
		return thread;
	}
	
	class OrbRushThread extends Thread {
		
		public OrbRushThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
			mySurfaceHolder = surfaceHolder;
			myContext = context;
			backgroundImg = BitmapFactory.decodeResource(context.getResources(), 
					R.drawable.titlescreen1);
			backgroundOrigW = backgroundImg.getWidth();
			backgroundOrigH = backgroundImg.getHeight();
			scale = myContext.getResources().getDisplayMetrics().density;
			Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/titlefont.ttf");
			blackPaint = new Paint();
			blackPaint.setTypeface(tf);
			blackPaint.setAntiAlias(true);
			blackPaint.setColor(Color.CYAN);
			blackPaint.setStyle(Paint.Style.STROKE);
			blackPaint.setTextAlign(Paint.Align.LEFT);
			blackPaint.setTextSize(scale * 25);
			sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
			hitSound = sounds.load(myContext, R.raw.beep, 1);
			missSound = sounds.load(myContext, R.raw.misssound, 1);
		}
		
		@Override
		public void run() {
			while(running) {
				Canvas c = null;
				try {
					c = mySurfaceHolder.lockCanvas(null);
					synchronized(mySurfaceHolder) {
						if(!onTitle) {

							if(!orbNeeded && !gameOver) {
								animateOrb();
							}
						}

						
						draw(c);
					}
				}
				finally {
					if(c != null) {
						mySurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
		
		private void draw(Canvas canvas) {
			try {
				if(onTitle) {
					canvas.drawBitmap(backgroundImg, 0, 0, null);
					
					if(!startPressed) {
						startButton = BitmapFactory.decodeResource(getResources(), R.drawable.startbutton);
						startButton = Bitmap.createScaledBitmap(startButton, (int)(180 * drawScaleH),
								(int)(180 * drawScaleH), true);
						canvas.drawBitmap(startButton, (screenW / 2) - (startButton.getWidth() / 2), 
							(screenH / 2) - (startButton.getHeight() / 2), null);
					}
					else {
						startButton = BitmapFactory.decodeResource(getResources(), R.drawable.startbuttonpressed);
						startButton = Bitmap.createScaledBitmap(startButton, (int)(180 * drawScaleH),
								(int)(180 * drawScaleH), true);
						canvas.drawBitmap(startButton, (screenW / 2) - (startButton.getWidth() / 2), 
								(screenH / 2) - (startButton.getHeight() / 2), null);
					}
				}
				if(!onTitle) {
					//canvas.drawBitmap(centerOrb, (screenW / 2) - (centerOrb.getWidth() / 2),
					//		(screenH / 2) - (centerOrb.getHeight() / 2), null);
					canvas.drawBitmap(backgroundImg, 0, 0, null);

					if(orbCombo < 10) {
						canvas.drawText("Combo: x" + orbCombo, screenW / 2 - 100 * drawScaleW,
								screenH - blackPaint.getTextSize(), blackPaint);
					}
					else {
						canvas.drawText("Combo: x" + orbCombo + "!", screenW / 2 - 100 * drawScaleW,
								screenH - blackPaint.getTextSize(), blackPaint);
					}
					
					centerOrb = BitmapFactory.decodeResource(getResources(), R.drawable.centerorb);
					centerOrb = Bitmap.createScaledBitmap(centerOrb, 
							(int)(180 * drawScaleH),
							(int)(180 * drawScaleH), true);
					/*settingsGears = BitmapFactory.decodeResource(getResources(),
							R.drawable.settings_gears);
					settingsGears = Bitmap.createScaledBitmap(settingsGears, 
							(int)(settingsGears.getWidth() * scaleH / 3),
							(int)(settingsGears.getHeight() * scaleH / 3), true);*/
					canvas.drawBitmap(settingsGears, 0, 0, null);
					centerOrb = rotateBitmap(centerOrb, (float)((sensitivity + 0.5) * movement));
					canvas.drawBitmap(centerOrb, (screenW / 2) - (centerOrb.getWidth() / 2),
							(screenH / 2) - (centerOrb.getHeight() / 2),
							new Paint(Paint.ANTI_ALIAS_FLAG));

					movement = 0;
					centerOrb.recycle();
					centerOrb = null;
					if(orbSide != 0) {
						canvas.drawBitmap(orb, orbX, orbY, null);

						//canvas.drawBitmap(orb, (screenW / 2) - orb.getWidth() / 2,
						//		(screenH / 2) - orb.getHeight() / 2, null);
					}					
				}
				if(gameOver) {
					gameOverMessage = BitmapFactory.decodeResource(getResources(), R.drawable.gameovermessage);
					gameOverMessage = Bitmap.createScaledBitmap(gameOverMessage, (int)(400 * drawScaleW),
							(int)(300 * drawScaleW), true);
					canvas.drawBitmap(gameOverMessage, (screenW / 2) - (gameOverMessage.getWidth() / 2),
							(screenH / 2) - (gameOverMessage.getHeight() / 2),
							new Paint(Paint.ANTI_ALIAS_FLAG));
				}
			}
			catch(Exception e) {
				
			}
			
		}
		
		boolean doTouchEvent(MotionEvent event) {
			synchronized(mySurfaceHolder) {
				int eventaction = event.getAction();
				int X = (int)event.getX();
				int Y = (int)event.getY();
				
				switch(eventaction) {
				
				case MotionEvent.ACTION_DOWN:
					if(!onTitle && X > (600 * drawScaleW) && X < (800 * drawScaleW) &&
							(Y > 0 * drawScaleH) && (Y < 600 * drawScaleH))
					{
						origTouchY = Y;
					}
					if(onTitle && X > (screenW / 2) - (startButton.getWidth() / 2) && 
					X < (screenW / 2) + (startButton.getWidth() / 2) && 
					Y > (screenH / 2) - (startButton.getHeight() / 2) &&
					Y < (screenH / 2) + (startButton.getHeight() / 2)) {
						startPressed = true;
					}
					
					break;
					
				case MotionEvent.ACTION_MOVE:
					if(!onTitle && !gameOver && X > (600 * drawScaleW) && X < (800 * drawScaleW) &&
							(Y > 0 * drawScaleH) && (Y < 600 * drawScaleH))
					{
						movement = Y - origTouchY;
						origTouchY = Y;
					}
					break;
					
					
				case MotionEvent.ACTION_UP:
					startPressed = false;

					if(!onTitle && X > 0 && X < (settingsGears.getWidth()) &&
							Y > 0 && Y < settingsGears.getHeight())
						{
							showSettingsDialog();
						}
					if(gameOver) {
						gameOver();
					}
					if(onTitle && X > (screenW / 2) - (startButton.getWidth() / 2) && 
							X < (screenW / 2) + (startButton.getWidth() / 2) && 
							Y > (screenH / 2) - (startButton.getHeight() / 2) &&
							Y < (screenH / 2) + (startButton.getHeight() / 2)) {
						backgroundImg = BitmapFactory.decodeResource(myContext.getResources(),
								R.drawable.newbackground);
						backgroundImg = Bitmap.createScaledBitmap(backgroundImg, screenW, screenH, true);
						//scaleW = (float)screenW / (float)backgroundOrigW;
						//scaleH = (float)screenH / (float)backgroundOrigH;
						//centerOrb = BitmapFactory.decodeResource(myContext.getResources(),
						//		R.drawable.centerorbplaceholder);
						//centerOrb = Bitmap.createScaledBitmap(centerOrb, (int)(centerOrb.getWidth() * scaleW),
						//		(int)(centerOrb.getHeight() * scaleW), true);
						pickOrb();
						showInstructions();
						onTitle = false;
					}
					

					break;
				}
			}
			return true;
		}
		
		public void setSurfaceSize(int width, int height) {
			synchronized(mySurfaceHolder) {
				screenW = width;
				screenH = height;
				drawScaleW = (float)screenW / (float)800;
				drawScaleH = (float)screenH / (float)600;
				backgroundImg = Bitmap.createScaledBitmap(backgroundImg, width, height, true);
				scaleW = (float)screenW / (float)backgroundOrigW;
				scaleH = (float)screenH / (float)backgroundOrigH;
				settingsGears = BitmapFactory.decodeResource(getResources(),
						R.drawable.settingsicon);
				settingsGears = Bitmap.createScaledBitmap(settingsGears, 
						(int)(75 * scaleH),
						(int)(75 * scaleH), true);
				
			}
		}
		
		public void setRunning(boolean b) {
			running = b;
		}
		
		//Helper method to rotate the bitmap of the center orb by angle number of degrees
		public Bitmap rotateBitmap(Bitmap source, float angle)
		{
			totalRotation = totalRotation + angle;
			//Check if total rotation angle exceeds 360 degrees, if so, mod it to get back within
			//the bounds
			if(Math.abs(totalRotation) > 360)
			{
				totalRotation = totalRotation % 360;
			}
			//Check if angle is negative, if so, add 360 to it to get a positive angle
			if(totalRotation < 0) {
				totalRotation = totalRotation + 360;
			}
			matrix.postRotate(angle);
		   	return Bitmap.createBitmap(source, 0,
		   		  0,
		   		  source.getWidth(), source.getHeight(), matrix, true);

		}
		
		//This method randomly selects the starting position of the incoming orbs
		public void pickOrb() {
			if(orbLives <= 0) 
			{
				gameOver = true;
				//gameOver();
			}
			if(orbCombo == 5) {
				orbCycles = 40;
			}
			if(orbCombo == 10) {
				orbCycles = 35;
			}
			if(orbCombo == 15)
			{
				orbCycles = 30;
			}
			if(orbCombo == 25) {
				orbCycles = 25;
			}
			if(orbCombo == 40) {
				orbCycles = 20;
			}
			orbColor = rand.nextInt(4) + 1;
			//orbColor = 1 corresponds to the blue orb
			if(orbColor == 1){
				orb = BitmapFactory.decodeResource(myContext.getResources(),
						R.drawable.blueball);
				orb = Bitmap.createScaledBitmap(orb, (int)(40 * drawScaleH),
						(int)(40 * drawScaleH), true);

			}
			//orbColor = 2 corresponds to red orb
			if(orbColor == 2){
				orb = BitmapFactory.decodeResource(myContext.getResources(),
						R.drawable.redball);
				orb = Bitmap.createScaledBitmap(orb, (int)(40 * drawScaleH),
						(int)(40 * drawScaleH), true);

			}
			//orbColor = 3 corresponds to green orb
			if(orbColor == 3){
				orb = BitmapFactory.decodeResource(myContext.getResources(),
						R.drawable.greenball);
				orb = Bitmap.createScaledBitmap(orb, (int)(40 * drawScaleH),
						(int)(40 * drawScaleH), true);

			}
			//orbColor = 4 corresponds to yellow orb
			if(orbColor == 4){
				orb = BitmapFactory.decodeResource(myContext.getResources(),
						R.drawable.orangeball);
				orb = Bitmap.createScaledBitmap(orb, (int)(40 * drawScaleH),
						(int)(40 * drawScaleH), true);

			}
			orbSide = rand.nextInt(4) + 1;

			//The leftmost side:
			if(orbSide == 1) {
				orbY = rand.nextInt(screenH);
				orbX = - orb.getWidth();
				orbStepSizeX = ((screenW / 2) - (orb.getWidth() / 2) - orbX) / orbCycles;
				orbStepSizeY = ((screenH / 2) - (orb.getHeight() / 2) - orbY) / orbCycles;

			}
			//The top side
			if(orbSide == 2) {
				orbX = rand.nextInt(screenW);
				orbY = - orb.getHeight();
				orbStepSizeX = ((screenW / 2) - (orb.getWidth() / 2) - orbX) / orbCycles;
				orbStepSizeY = ((screenH / 2) - (orb.getHeight() / 2) - orbY) / orbCycles;

			}
			//The right side
			if(orbSide == 3) {
				orbY = rand.nextInt(screenH);
				orbX = screenW;
				orbStepSizeX = ((screenW / 2) - (orb.getWidth() / 2) - orbX) / orbCycles;
				orbStepSizeY = ((screenH / 2) - (orb.getHeight() / 2) - orbY) / orbCycles;

			}
			//The bottom side
			if(orbSide == 4) {
				orbX = rand.nextInt(screenW);
				orbY = screenH;
				orbStepSizeX = ((screenW / 2) - (orb.getWidth() / 2) - orbX) / orbCycles;
				orbStepSizeY = ((screenH / 2) - (orb.getHeight() / 2) - orbY) / orbCycles;

			}
			orbNeeded = false;
		}
		
		public void animateOrb() {
			if(orbNeeded == false) {
				if(orbSide == 1) {
					orbX = orbX + orbStepSizeX;
					orbY = orbY + orbStepSizeY;
				}
				if(orbSide == 2) {
					orbX = orbX + orbStepSizeX;
					orbY = orbY + orbStepSizeY;
				}
				if(orbSide == 3) {
					orbX = orbX + orbStepSizeX;
					orbY = orbY + orbStepSizeY;
				}
				if(orbSide == 4) {
					orbX = orbX + orbStepSizeX;
					orbY = orbY + orbStepSizeY;
				}
			}
			
			//The logic to detect when the orbs contact with the center orb
			//The -(orb.getWidht() / 2) is to offset the drawn top left corner of the orbs
			//The remaining +/- constant is the actual "zone" around the center orb that
			//triggers contact
			//REMEMBER: orb.getWidth() == orb.getHeight() because the bitmap loaded is SQUARE!!
			if(orbX > (screenW / 2) - (orb.getWidth() / 2) - (75 * drawScaleH) && 
					orbX < (screenW / 2) - (orb.getWidth() / 2) + (75 * drawScaleH) &&
					orbY > (screenH / 2) - (orb.getWidth() / 2) - (75 * drawScaleH) &&
					orbY < (screenH / 2) - (orb.getWidth() / 2) + (75 * drawScaleH) && !orbNeeded) {
				orbNeeded = true;


				processContact();
				pickOrb();

				
			}
		}
		
		//A method to show the dialog when the settings are clicked
		private void showSettingsDialog() {
			final Dialog settingsDialog = new Dialog(myContext);
			settingsDialog.setCancelable(false);
			settingsDialog.setContentView(R.layout.settings_dialog);
			orbNeeded = true;
			final CheckBox checkBox = (CheckBox)settingsDialog.findViewById(R.id.soundon);
			checkBox.setChecked(soundOn);
			final SeekBar mySeekBar = (SeekBar)settingsDialog.findViewById(R.id.sensitivityBar);
			mySeekBar.setMax(100);
			mySeekBar.setProgress((int)((sensitivity - 0.35) * 100));
			Button okButton = (Button)settingsDialog.findViewById(R.id.okayButton);
			okButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//The new sensitivity will be the user selected value on the progress bar
					//plus an minimum offset
					sensitivity = (((float)(mySeekBar.getProgress()) / (float)100) + (float)0.35);
					soundOn = checkBox.isChecked();
					SharedPreferences settings = myContext.getSharedPreferences(PREFERENCES_NAME, 0); 
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean("soundSetting", soundOn);
					editor.commit();
					orbNeeded = false;
					settingsDialog.dismiss();
				}
			});
			Button cancelButton = (Button)settingsDialog.findViewById(R.id.cancelButton);
			cancelButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//The new sensitivity will be the user selected value on the progress bar
					//plus an minimum offset
					orbNeeded = false;

					settingsDialog.dismiss();

				}
			});
			
			settingsDialog.show();
		}
		
		//A method that shows a how-to-play dialog
		private void showInstructions() {
			final Dialog instructionDialog = new Dialog(myContext);
			instructionDialog.setCancelable(false);
			instructionDialog.setContentView(R.layout.instruction_dialog);
			orbNeeded = true;
			Button gotItButton = (Button)instructionDialog.findViewById(R.id.gotitButton);
			gotItButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					orbNeeded = false;
					instructionDialog.dismiss();
				}
			});
			instructionDialog.show();
		}
		
		//A method to open up the game over dialog
		private void gameOver() {
			adLoaded = false;
			final Dialog gameOverDialog = new Dialog(myContext);
			gameOverDialog.setCancelable(false);
			gameOverDialog.setContentView(R.layout.gameover_dialog);
			AdView adView = (AdView) gameOverDialog.findViewById(R.id.adView);
			adView.setAdListener(new myAdListener());
			adView.loadAd(new AdRequest.Builder().build());
		    /*dRequest adRequest = new AdRequest.Builder()
		        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		        .addTestDevice("TEST_EMULATOR")
		        .build();
		    adView.loadAd(adRequest);*/
			/*AdView adView = new AdView(myContext);
			adView.setAdSize(AdSize.BANNER);
		    adView.setAdUnitId("ca-app-pub-6223448552194013/8070152485");
		    LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout1);
		    layout.addView(adView);*/
			orbNeeded = true;
				TextView comboText = (TextView)gameOverDialog.findViewById(R.id.orbcombotext);
				comboText.setText("Total: " + orbCombo);
				TextView highScore = (TextView)gameOverDialog.findViewById(R.id.orbscoretext);
				highScore.setText("Best: " + highestCombo);
			Button replayButton = (Button)gameOverDialog.findViewById(R.id.replayButton);
			replayButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//Reset the score and combo values
					//orbScore = 0;
					orbCombo = 0;
					orbLives = 1;
					orbCycles = 50;
					gameOver = false;
					orbNeeded = false;
					pickOrb();
					gameOverDialog.dismiss();
				}
			});

			gameOverDialog.show();
		}
		
		//A helper method to check and handle collision between the orbs and the center orb
		private void processContact() {
			double centerX = orbX + orb.getWidth() / 2;
			double centerY = orbY + orb.getWidth() / 2;
			centerX = centerX - (screenW / 2);
			centerY = (screenH / 2) - centerY;
			orbAngle = Math.atan2(centerX, centerY);
			orbAngle = orbAngle * 180 / 3.14159265;
			if(orbAngle < 0) {
				orbAngle = orbAngle + 360;
			}
			float rotationG = totalRotation + 45;
			if(rotationG > 360) {
				rotationG = rotationG - 360;
			}
			float rotationY = totalRotation + 135;
			if(rotationY > 360) {
				rotationY = rotationY - 360;
			}
			float rotationB = totalRotation + 225;
			if(rotationB > 360) {
				rotationB = rotationB - 360;
			}
			float rotationR = totalRotation + 315;
			if(rotationR > 360) {
				rotationR = rotationR - 360;
			}
			if(orbColor == 1 && (Math.abs(rotationB - orbAngle) <= 45 
					|| Math.abs(rotationB - orbAngle) >= 315))
			{
				orbCombo++;
				if(soundOn) {
					AudioManager audioManager = (AudioManager)myContext.getSystemService(Context.AUDIO_SERVICE);
					float volume = (float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					sounds.play(hitSound, volume, volume, 1, 0, 1);
				}
				//orbScore = orbScore + 1 + orbCombo;
			}
			else if(orbColor == 2 && (Math.abs(rotationR - orbAngle) <= 45 
					|| Math.abs(rotationR - orbAngle) >= 315))
			{
				orbCombo++;
				if(soundOn) {
					AudioManager audioManager = (AudioManager)myContext.getSystemService(Context.AUDIO_SERVICE);
					float volume = (float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					sounds.play(hitSound, volume, volume, 1, 0, 1);
				}
				//orbScore = orbScore + 1 + orbCombo;
			}
			else if(orbColor == 3 && (Math.abs(rotationG - orbAngle) <= 45 
					|| Math.abs(rotationG - orbAngle) >= 315))
			{

				orbCombo++;
				if(soundOn) {
					AudioManager audioManager = (AudioManager)myContext.getSystemService(Context.AUDIO_SERVICE);
					float volume = (float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					sounds.play(hitSound, volume, volume, 1, 0, 1);
				}
				//orbScore = orbScore + 1 + orbCombo;
			}
			else if(orbColor == 4 && (Math.abs(rotationY - orbAngle) <= 45 
					|| Math.abs(rotationY - orbAngle) >= 315))
			{

				orbCombo++;
				if(soundOn) {
					AudioManager audioManager = (AudioManager)myContext.getSystemService(Context.AUDIO_SERVICE);
					float volume = (float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					sounds.play(hitSound, volume, volume, 1, 0, 1);
				}
				//orbScore = orbScore + 1 + orbCombo;
			}
			else {
				if(highestCombo < orbCombo) {
					highestCombo = orbCombo;
					SharedPreferences settings = myContext.getSharedPreferences(PREFERENCES_NAME, 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putInt("highestcombo", highestCombo);
					editor.commit();
					
				}
				orbCycles = 50;
				orbLives--;
				if(soundOn) {
					AudioManager audioManager = (AudioManager)myContext.getSystemService(Context.AUDIO_SERVICE);
					float volume = (float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					sounds.play(missSound, volume, volume, 1, 0, 1);
				}
			}
			
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return thread.doTouchEvent(event);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		thread.setSurfaceSize(width, height);
	}
	
	@Override 
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		if(thread.getState() == Thread.State.NEW) {
			thread.start();
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.setRunning(false);
	}
	private class myAdListener extends AdListener {
		@Override	
		public void onAdLoaded() {
			adLoaded = true;
		}
	}
}
