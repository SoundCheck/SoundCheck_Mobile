package com.yankees.soundcheck;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soundcheck.shared.Song;
import soundcheck.shared.Zone;
import soundcheck.shared.Const.Command;
import soundcheck.shared.ZoneProperties;
import tasks.CreateNetworkChannel_Task;
import tasks.StreamingCommand_Task;
import tasks.ZoneInfo_Task;

import com.yankees.soundcheck.data.PeerCollection;
import com.yankees.soundcheck.network.NetworkChannel;

import customAdapters.SongAdapter;

import yankees.soundcheck.mobile.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This class is the main entry point into the app.
 * 
 *
 */
public class SoundCheck_Activity extends Activity {

	//Logger for the class
	final static Logger logger = LoggerFactory.getLogger(SoundCheck_Activity.class);
	
	//Used for testing to indicate if the app is being run on an x86 Android machine.
	private final boolean forX86 = false;
	
	//The NetworkChannel to be used for JGroups communication
	private static NetworkChannel channel;

	//List used to display the zone currently being controlled.
	private static List<Zone> zones;
	
	//List used to display the currently playing song.
	private static List<Song> songs;	
	
	//Dialog used as a splash screen
	protected Dialog mSplashDialog;
	
	//Request codes for the other activities that are launched for result
	private final int ZONE_ACTIVITY = 1;
	private final int SONG_ACTIVITY = 2;
	
	//Adapters for the Lists
	private static ArrayAdapter<Zone> zoneAdapter;
	private static SongAdapter songAdapter;
	
	//Indicates whether the current song is playing or paused.
	private static boolean isPlaying = false;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initialize the ArrayLists
		songs = new ArrayList<Song>();

		zones = new ArrayList<Zone>();
		
		MyStateSaver data = (MyStateSaver) getLastNonConfigurationInstance();
		
		if(data != null){
			if(data.showSplashScreen){
				showSplashScreen();
			}
			
			if(data.currentSong != null){
				songs.add(data.currentSong);
			}
			if(data.currentZone != null){
				zones.add(data.currentZone);
			}
			
			setContentView(R.layout.control_screen);
		}
		else{
			showSplashScreen();
			setContentView(R.layout.control_screen);
		}

		
		
		
	}

	//Shows the splash dialog for 5.5 seconds and then removes it.
	private void showSplashScreen() {
		mSplashDialog = new Dialog(this, R.style.SplashScreen);
		mSplashDialog.setContentView(R.layout.splash_screen);
		mSplashDialog.setCancelable(false);
		mSplashDialog.show();

		// Set Runnable to remove splash screen just in case
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			public void run() {
				removeSplashScreen();
			}
		}, 5500);
	}

	/**
	 * Removes the splash screen dialog
	 */
	protected void removeSplashScreen() {
		if(mSplashDialog != null){
			mSplashDialog.dismiss();
			mSplashDialog = null;
		}
		
	}

	/**
	 * Sets up the Activity when it is started.
	 */
	@Override
	public void onStart(){
		super.onStart();
		
		togglePlayButton();

		//See if the phone is connected to WiFi
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected() || forX86) {

			//Only create a new controller if one does not exist
			//Do not want to connect to the cluster again if the app has only been paused.
			if (channel == null) {				
				new CreateNetworkChannel_Task().execute(this.getApplicationContext());
				
				new ZoneInfo_Task().execute();
			}	

		} 
		//Device is not connected to WiFi, inform the user.
		else {
			AlertDialog wifiWarningDialog = new AlertDialog.Builder(this).create(); // Using this instead of getApplicationContext()
								                                                    // because context is null at this point

			wifiWarningDialog.setTitle("WARNING");

			wifiWarningDialog.setMessage("Device must be connected to a WiFi network.");

			wifiWarningDialog.setButton("OK", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// Open WiFi manager so that the user can connect to
							// a wireless network.
							try {
								SoundCheck_Activity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
							} catch (android.content.ActivityNotFoundException e) {
								finish();
							}
						}
					});

			wifiWarningDialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {

						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});

			wifiWarningDialog.show();

		}
		
		//Get the ListViews from the layout
		ListView songList = (ListView) findViewById(R.id.songListView);

		ListView zoneList = (ListView) findViewById(R.id.zoneListView);
		
		//zoneList.setBackgroundColor(R.color.blue);
		//songList.setBackgroundColor(R.color.blue);
		
		zoneAdapter = new ArrayAdapter<Zone>(this, R.layout.list_view_item, zones);

		zoneAdapter.setNotifyOnChange(true); //Setting this to true tells the UI to automatically update if the ArrayList is changed 
		
		songAdapter = new SongAdapter(this.getApplicationContext(), songs);

		//songAdapter.setNotifyOnChange(true);

		songList.setAdapter(songAdapter);

		zoneList.setAdapter(zoneAdapter);

		songList.setClickable(true);
		zoneList.setClickable(true);

		//Click listener for the song list item
		songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				Intent songActivityIntent = new Intent();

				songActivityIntent.putExtra("zone", zones.get(0));

				songActivityIntent.setClass(SoundCheck_Activity.this, Song_Activity.class);

				SoundCheck_Activity.this.startActivityForResult(songActivityIntent, SONG_ACTIVITY); //Start the Song_Activity for result so selected song will be listened for.
				
				SoundCheck_Activity.this.overridePendingTransition(R.anim.slide_down, 0);
			}
		});

		//Click listener for the zone list items
		zoneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				Intent zoneManagerIntent = new Intent();

				zoneManagerIntent.setClass(SoundCheck_Activity.this, Peer_Activity.class);

				SoundCheck_Activity.this.startActivityForResult(zoneManagerIntent, ZONE_ACTIVITY); //Start the Zone_Activity for result so the selected song will be listened for.
				
				SoundCheck_Activity.this.overridePendingTransition(R.anim.slide_up, 0);

			}
		});
	}

	/**
	 * Called when the app is closed by the system.
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();

		if(channel != null){
			channel.destroy();
		}
	}
	
	@Override
	public void onPause(){
		
		super.onPause();
		
		if(mSplashDialog != null){
			mSplashDialog.dismiss();
		}
	}

	/**
	 * The action that is fired when the "Play" button is pressed.
	 * @param v - The current UI View.
	 */
	public void togglePlay(View v){

		ImageButton playButton = (ImageButton) findViewById(R.id.playButton);

		if(songs.size() > 0){

			if (songs.get(0).getTitle() != null) {

				if (isPlaying) {
					playButton.setBackgroundResource(R.drawable.play_button_def);
					isPlaying = false;
					new StreamingCommand_Task().execute(zones.get(0), songs.get(0),	Command.PAUSE);
				} else {
					playButton.setBackgroundResource(R.drawable.pause_button_def);
					isPlaying = true;
					new StreamingCommand_Task().execute(zones.get(0), songs.get(0),Command.PLAY);
				}
			}
		}
	}
	
	public void togglePlayButton(){
		ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
		playButton.setBackgroundResource(R.drawable.play_button_def);		
		
	}

	/**
	 * The action that is fired when the "Next Song" button is pressed.
	 * @param v - The current UI view
	 */
	public void nextSong(View v){

		if(zones.size() > 0){
			ZoneProperties zoneProp = PeerCollection.getZoneProps(zones.get(0));

			if(zoneProp != null){
				if (zoneProp.getPlayList().size() > 1) {
					Song nextSong = zoneProp.getPlayList().get(1);
					
					//zoneProp.removeSong(songs.get(0));

					changeDisplayedSong(nextSong);

					new StreamingCommand_Task().execute(zones.get(0), songs.get(0), Command.NEXT);

				} 
				else {
					Toast.makeText(getApplicationContext(), "No more songs in the list.", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	/**
	 * Stops playback of the current song.
	 * @param v
	 */
	public void stopPlayback(View v ){
		if(songs.size() > 0){
			if(songs.get(0).getTitle() != null){
				new StreamingCommand_Task().execute(zones.get(0), songs.get(0), Command.TEARDOWN);
			}
		}
	}

	/**
	 * Triggered when either the Song_Activity or Peer_Activity returns its result.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){

		if(requestCode == ZONE_ACTIVITY && resultCode == RESULT_OK){

			changeDisplayedZone((Zone) data.getSerializableExtra("newZone"));

		}
		else if(requestCode == SONG_ACTIVITY && resultCode == RESULT_OK){

			changeDisplayedSong((Song) data.getSerializableExtra("newSong"));

		}

	}	

	/**
	 * Sets the displayed zone name for the main control screen.
	 * @param newZone - The name of the new zone to display.
	 */
	public static void changeDisplayedZone(Zone newZone){
		
		zones.clear();
		
		if(newZone != null){
			zones.add(newZone);
		}
		
		zoneAdapter.notifyDataSetChanged();  //Manually triggering this as the auto change does not seem to be working.
	}

	/**
	 * Sets the displayed song name for the main control screen.
	 * @param newSong - The song name that is to be displayed.
	 */
	public static void changeDisplayedSong(Song newSong) {

		songs.clear();

		if (newSong != null) {
			songs.add(newSong);
		}

		songAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Private class that is used for displaying the splash screen.
	 * The screen is not shown if the state passed in indicates that is should not be.
	 *
	 */
	private class MyStateSaver{
		public boolean showSplashScreen = false;
		
		public Song currentSong;  //Save the song on orientation change
		
		public Zone currentZone; //Save the zone on orientation change
	}
	
	/**
	 * Determines whether the splash screen should be shown based on the state passed in.
	 */
	@Override
	public Object onRetainNonConfigurationInstance(){
		MyStateSaver data = new MyStateSaver();
		
		if(mSplashDialog != null){
			data.showSplashScreen = true;
			removeSplashScreen();
		}
		
		data.currentSong = songs.get(0);
		
		data.currentZone = zones.get(0);		
		
		return data;
	}
	
	/**
	 * Sets the NetworkChannel that the app is to use
	 * @param newChannel
	 */
	public static void setChannel(NetworkChannel newChannel){
		channel = newChannel;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		
		MenuInflater inflater = getMenuInflater();
		
		inflater.inflate(R.menu.control_screen_option_menu, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		
		switch(item.getItemId()){
		case R.id.refreshView:
			new ZoneInfo_Task().execute();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
		
	}
}