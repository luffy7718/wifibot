

package com.example.wifibot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;



public class MainActivity<QNetworkRequest> //permet l'accés au réseaux
extends Activity //héritage classe
implements OnClickListener, OnTouchListener, OnSeekBarChangeListener, OnCheckedChangeListener {//	héritage interface

	private envoie wcs = null;
	private static String IP = "172.16.121.14";
	private static int PORT = 15020;
	private static int REFRESH_TIME = 100;

	private static int VOLTAGE_MAX = 13;
	private static int VOLTAGE_LIMIT = 11;

	private static int SPEED_MAX = 360;
	private static int SPEED_DEFAULT = 200;

	public static boolean MOTOR_CONTROL_PID_10 = false;//vérifie le controle erreur du déplacement du wifibot
	
	private static int BATTERY_FULL = 18;

	private static int IR_MAX = 150;
	public static int IR_LIMIT = 20;
	private WebView webView;
	private Timer timer = null;
	public int voltage = 0;
	public int current = 0;
	public int speed = SPEED_DEFAULT;
	public int irDg = 0;
	public int irDd = 0;
	public int irAg = 0;
	public int irAd = 0;
	public int batteryState = 0;
	public boolean onSecurity = false;//pour les capteurs infrarouges
	public boolean onMotorControl = false;//vérifie le control des moteurs
public String lienHaut;
public String lienBas;
public String lienInit;
	Handler handler = new Handler();//met à jour l'ihm


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}//changer la police du thread
		setContentView(R.layout.main);
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("http://172.16.121.14:8080/javascript_simple.html");
		lienHaut="/?action=command&dest=0&plugin=0&id=10094853&group=1&value=-200";
	    lienBas="/?action=command&dest=0&plugin=0&id=10094853&group=1&value=200";
	    lienInit="/?action=command&dest=0&plugin=0&id=10094855&group=1&value=1";
	    Button camhaut = (Button) findViewById(R.id.buttoncamHaut);
	    
	    camhaut.setOnClickListener(new View.OnClickListener() {
	    	   public void onClick(View v) {
	    		    MainActivity.this.webView.loadUrl("http://172.16.121.14:8080"+lienHaut);
	    		    MainActivity.this.webView.reload();
	    		    MainActivity.this.webView.loadUrl("http://172.16.121.14:8080/javascript_simple.html");
	    		    
	    		   }});
	    Button camBas = (Button) findViewById(R.id.buttoncamBas);
	    camBas.setOnClickListener(new View.OnClickListener() {
	    	   public void onClick(View v) {
	    		    MainActivity.this.webView.loadUrl("http://172.16.121.14:8080"+lienBas);
	    		    MainActivity.this.webView.reload();
	    		    MainActivity.this.webView.loadUrl("http://172.16.121.14:8080/javascript_simple.html");
	    		   }});	
	    Button camInit = (Button) findViewById(R.id.buttoncamInit);
	    camInit.setOnClickListener(new View.OnClickListener() {
	    	   public void onClick(View v) {
	    		    MainActivity.this.webView.loadUrl("http://172.16.121.14:8080"+lienInit);
	    		    MainActivity.this.webView.reload();
	    		    MainActivity.this.webView.loadUrl("http://172.16.121.14:8080/javascript_simple.html");
	    		   }});	
	   
	    webView.reload();
	    
		Button btnhaut = (Button) findViewById(R.id.button1);//bouton haut
		btnhaut.setOnTouchListener(this);

		Button btnBas = (Button) findViewById(R.id.button4);// bouton bas
		btnBas.setOnTouchListener(this);

		Button btngch = (Button) findViewById(R.id.button2);//bouton gauche
		btngch.setOnTouchListener(this);

		Button btndrt = (Button) findViewById(R.id.button3);//bouton droite
		btndrt.setOnTouchListener(this);

		Button btnRotate = (Button) findViewById(R.id.button6);//bouton rotation
		btnRotate.setOnTouchListener(this);

		ToggleButton btnConnected = (ToggleButton) findViewById(R.id.button5);//bouton connexion
		btnConnected.setOnClickListener(this);

		SeekBar sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);//vitesse
		sbSpeed.setOnSeekBarChangeListener(this);
		sbSpeed.setMax(SPEED_MAX);
		sbSpeed.setProgress(speed);

		TextView tvState = (TextView) findViewById(R.id.tvState);//état
		tvState.setText("état: déconnectée");

		TextView tvSpeed = (TextView) findViewById(R.id.tvSpeed);//vitesse text
		tvSpeed.setText("vitesse: " + speed);

		TextView tvVoltage = (TextView) findViewById(R.id.tvVoltage);//nombre de volt
		tvVoltage.setText("Voltage: 0");

		ProgressBar pgVoltage = (ProgressBar) findViewById(R.id.pgVoltage);//volt actuelle
		pgVoltage.setMax(VOLTAGE_MAX);

		ProgressBar pgFR = (ProgressBar) findViewById(R.id.pgFR);
		pgFR.setMax(IR_MAX);

		ProgressBar pgFL = (ProgressBar) findViewById(R.id.pgFL);
		pgFL.setMax(IR_MAX);

		ProgressBar pgBR = (ProgressBar) findViewById(R.id.pgBR);
		pgBR.setMax(IR_MAX);
		ProgressBar pgBL = (ProgressBar) findViewById(R.id.pgBL);
		pgBL.setMax(IR_MAX);

	
		
		btnConnected.setChecked(false);// ne s'affiche pas car état déconnectée
		btnhaut.setEnabled(false);
		btnBas.setEnabled(false);
		btngch.setEnabled(false);
		btndrt.setEnabled(false);
		btnRotate.setEnabled(false);
		sbSpeed.setEnabled(false);
		pgVoltage.setEnabled(false);
		pgFR.setEnabled(false);
		pgFL.setEnabled(false);
		pgBR.setEnabled(false);
		pgBL.setEnabled(false);
		webView.setEnabled(false);
	
	}


	@Override
	public boolean onTouch(View elem, MotionEvent event) {

		int action = event.getAction();

		if(elem.getId() == R.id.button1) {//déplacer vers le haut
			if (action == MotionEvent.ACTION_DOWN){
				wcs.forward(speed);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_CANCEL){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_POINTER_UP){
				wcs.nothing();
			}
		}
		else if(elem.getId() == R.id.button4) {//déplacer vers le bas
			if (action == MotionEvent.ACTION_DOWN){
				wcs.backward(speed);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_CANCEL){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_POINTER_UP){
				wcs.nothing();
			}
		}
		else if(elem.getId() == R.id.button2) {//déplacer vers la gauche
			if (action == MotionEvent.ACTION_DOWN){
				wcs.direction(speed, true, true);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_CANCEL){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_POINTER_UP){
				wcs.nothing();
			}
		}
		else if(elem.getId() == R.id.button3) {//déplacer vers la droite
			if (action == MotionEvent.ACTION_DOWN){
				wcs.direction(speed, false, true);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_CANCEL){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_POINTER_UP){
				wcs.nothing();
			}
		}
		else if(elem.getId() == R.id.button6) {//bouton rotation
			if (action == MotionEvent.ACTION_DOWN){
				wcs.rotate(speed, true);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_CANCEL){
				wcs.nothing();
			}
			else if (action == MotionEvent.ACTION_POINTER_UP){
				wcs.nothing();
			}
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		Button btnhaut = (Button) findViewById(R.id.button1);
		Button btnBas = (Button) findViewById(R.id.button4);
		Button btngch = (Button) findViewById(R.id.button2);
		Button btndrt = (Button) findViewById(R.id.button3);
		Button btnRotate = (Button) findViewById(R.id.button6);
		ToggleButton btnConnected = (ToggleButton) findViewById(R.id.button5);
		TextView tvState = (TextView) findViewById(R.id.tvState);
		SeekBar sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
		TextView tvVoltage = (TextView) findViewById(R.id.tvVoltage);
		ProgressBar pgVoltage = (ProgressBar) findViewById(R.id.pgVoltage);
		ProgressBar pgFR = (ProgressBar) findViewById(R.id.pgFR);
		ProgressBar pgFL = (ProgressBar) findViewById(R.id.pgFL);
		ProgressBar pgBR = (ProgressBar) findViewById(R.id.pgBR);
		ProgressBar pgBL = (ProgressBar) findViewById(R.id.pgBL);
	

		if(v.getId() == R.id.button5){//connexion

			if(((ToggleButton) v).isChecked()) {//option

				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				String ip = pref.getString("ip", MainActivity.IP);

				try {// connexion socket
					msocket = new Socket();
					msocket.connect(new InetSocketAddress(ip, MainActivity.PORT), 1000);

					InputStream is = msocket.getInputStream();
					DataInputStream dis = new DataInputStream(is);

					OutputStream out = msocket.getOutputStream();
					DataOutputStream dos = new DataOutputStream(out);

					timer = new Timer();
					wcs = new envoie(this);
					wcs.configure(dos,dis);
					timer.scheduleAtFixedRate(wcs, 0, MainActivity.REFRESH_TIME);//rafraichir les données envoyées

					btnhaut.setEnabled(true);//affichage car connectée
					btnBas.setEnabled(true);
					btngch.setEnabled(true);
					btndrt.setEnabled(true);
					btnRotate.setEnabled(true);
					sbSpeed.setEnabled(true);
					pgVoltage.setEnabled(true);
					pgFR.setEnabled(true);
					pgFL.setEnabled(true);
					pgBR.setEnabled(true);
					pgBL.setEnabled(true);
			
					webView.setEnabled(true);
					tvState.setText("état: connectée");

				}
				catch (Exception e) {
					tvState.setText("état: " + e.getMessage());
					btnConnected.setChecked(false);
				}
			}
			else {
				btnhaut.setEnabled(false);
				btnBas.setEnabled(false);
				btngch.setEnabled(false);
				btndrt.setEnabled(false);
				btnRotate.setEnabled(false);
				sbSpeed.setEnabled(false);
				tvState.setText("état: déconnectée");
				pgVoltage.setEnabled(false);
				pgVoltage.setProgress(0);
				pgFR.setEnabled(false);
				pgFR.setProgress(0);
				pgFL.setEnabled(false);
				pgFL.setProgress(0);
				pgBR.setEnabled(false);
				pgBR.setProgress(0);
				pgBL.setEnabled(false);
				pgBL.setProgress(0);
				webView.setEnabled(false);
		
				tvVoltage.setText("Voltage: 0");
				if(timer != null)
					timer.cancel();
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {//menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}   

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//option
		switch (item.getItemId()) {
		case R.id.Settings:
			Intent i = new Intent(this, option.class);
			startActivity(i);
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(timer != null)
			timer.cancel();
	}


	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	}


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {//commencement de la progression sekkbar
	}


	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {//arret de la progression sekkbar
		speed = seekBar.getProgress();
		TextView tvSpeed = (TextView) findViewById(R.id.tvSpeed);
		tvSpeed.setText("Speed: " + seekBar.getProgress());
	}


	/**
	 * 
	 */
	public Runnable updateUI = new Runnable() {//changement de valeur en se connectant
		public void run() {
			TextView tvVoltage = (TextView) findViewById(R.id.tvVoltage);
			float voltage_value = (float) (MainActivity.this.voltage/10.0);
			tvVoltage.setText("Voltage: " + voltage_value);

			((TextView) findViewById(R.id.tvCurrent)).setText(MainActivity.this.current*100 + "mA");

			ProgressBar pgVoltage = (ProgressBar) findViewById(R.id.pgVoltage);
			pgVoltage.setProgress((int)voltage_value);

			ProgressBar pgFR = (ProgressBar) findViewById(R.id.pgFR);
			pgFR.setProgress((int)irDd);

			ProgressBar pgFL = (ProgressBar) findViewById(R.id.pgFL);
			pgFL.setProgress((int)irDg);

			ProgressBar pgBR = (ProgressBar) findViewById(R.id.pgBR);
			pgBR.setProgress((int)irAd);

			ProgressBar pgBL = (ProgressBar) findViewById(R.id.pgBL);
			pgBL.setProgress((int)irAg);

			//IR label couleur
			if(irDd > IR_LIMIT) {
				((TextView) findViewById(R.id.tvFR)).setTextColor(Color.RED);
			}
			else {
				((TextView) findViewById(R.id.tvFR)).setTextColor(Color.WHITE);
			}

			if(irDg > IR_LIMIT) {
				((TextView) findViewById(R.id.tvFL)).setTextColor(Color.RED);
			}
			else {
				((TextView) findViewById(R.id.tvFL)).setTextColor(Color.WHITE);
			}

			if(irAd > IR_LIMIT) {
				((TextView) findViewById(R.id.tvBR)).setTextColor(Color.RED);
			}
			else {
				((TextView) findViewById(R.id.tvBR)).setTextColor(Color.WHITE);
			}

			if(irAg > IR_LIMIT) {
				((TextView) findViewById(R.id.tvBL)).setTextColor(Color.RED);
			}
			else {
				((TextView) findViewById(R.id.tvBL)).setTextColor(Color.WHITE);
			}


			if(voltage < VOLTAGE_LIMIT) {
				((TextView) findViewById(R.id.tvVoltage)).setTextColor(Color.RED);
			}
			else {
				((TextView) findViewById(R.id.tvVoltage)).setTextColor(Color.WHITE);
			}

		
			if(batteryState == BATTERY_FULL) {
				((TextView) findViewById(R.id.tvVoltage)).setText("BATTERY compléte");
			}

		}
	};
	private Socket msocket;

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		
	}


}