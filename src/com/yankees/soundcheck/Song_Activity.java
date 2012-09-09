package com.yankees.soundcheck;

import java.util.List;

import soundcheck.shared.Const.Command;
import soundcheck.shared.Song;
import soundcheck.shared.Zone;
import soundcheck.shared.ZoneProperties;
import tasks.StreamingCommand_Task;

import com.yankees.soundcheck.data.PeerCollection;
import com.yankees.soundcheck.data.SongManager;

import customAdapters.SongAdapter;
import yankees.soundcheck.mobile.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Used for displaying the song chooser UI and receiving the user input. 
 * 
 *
 */

public class Song_Activity extends Activity {
	
	//Used as the request code when starting the AddSong_Activity
	private final int ADD_SONG = 1;
	
	//The current Zone that Songs are being viewed for
	private Zone currentZone;
	
	//The list of Songs to display
	private List<Song> songArray;
	
	//Adapter to use to display the list of songs.
	private SongAdapter songAdapter;

	/**
	 * Sets the view resource to the song_manager layout.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.song_manager);
	}

	/**
	 * Gets the list of Songs to display and adds them to the ListView.
	 */
	@Override
	public void onStart(){

		super.onStart();

		Bundle extras = this.getIntent().getExtras();

		currentZone = (Zone) extras.get("zone");

		ListView songList = (ListView) findViewById(R.id.playListView);

		songArray = SongManager.getZonePlayList(currentZone);
		
		if(songArray.size() == 0){
			Toast.makeText(getApplicationContext(), "No Songs in the Playlist", Toast.LENGTH_LONG).show();
		}

		songAdapter = new SongAdapter(this.getApplicationContext(), songArray);

		//songAdapter.setNotifyOnChange(true);

		songList.setAdapter(songAdapter);

		songList.setClickable(true);

		//Click listener for list items.
		//The selected song is selected as the currently playing song and the network is notified.
		songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				Intent resultIntent = new Intent();

				Song newSong = songArray.get(arg2);				
				
				resultIntent.putExtra("newSong", newSong);

				Song_Activity.this.setResult(RESULT_OK, resultIntent);
				
				if(songArray.size() > 0){
					new StreamingCommand_Task().execute(currentZone, songArray.get(arg2), Command.QUEUE_FRONT);
					
					SongManager.setCurrentSong(newSong, currentZone);
				}
				
				finish();
				
				overridePendingTransition(android.R.anim.slide_in_left, 0);

			}

		});
		
		registerForContextMenu(songList); //Register this Activity for a context menu.
	}
	
	@Override
	/**
	 * Creates the options menu when the menu button is pressed
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		
		MenuInflater inflater = getMenuInflater();
		
		inflater.inflate(R.menu.song_option_menu, menu);
		
		return true;
	}
	
	@Override
	/**
	 * Logic for when an options menu item is selected.
	 */
	public boolean onOptionsItemSelected(MenuItem item){

		switch(item.getItemId()){
		case R.id.back_song_activity:
			finish();
			overridePendingTransition(android.R.anim.slide_in_left, 0);
			return true;
		case R.id.addSong_songActivity:
			Intent addSongIntent = new Intent();
			
			addSongIntent.putExtra("zone", currentZone);
			
			addSongIntent.setClass(Song_Activity.this, AddSong_Activity.class);
			
			Song_Activity.this.startActivityForResult(addSongIntent, ADD_SONG);
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	/**
	 * Triggered when the add song activity returns.
	 * 
	 * requestCode - The request code associated with the activity returning
	 * resultCode - The result of the activity finishing
	 * data - Intent with any data from the finished activity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == ADD_SONG && resultCode == RESULT_OK){
			
			ZoneProperties zp = PeerCollection.getZoneProps(currentZone);
			
			Song addedSong = (Song) data.getSerializableExtra("songToAdd");
			
			zp.addLast(addedSong);
			
			songArray = SongManager.getZonePlayList(currentZone);
		}
	}
	
	@Override
	/**
	 * Creates a context menu when a list item is long clicked.
	 */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.song_context_menu, menu);
	}
	
	@Override
	/**
	 * Triggered after a context menu item is selected.
	 */
	public boolean onContextItemSelected(MenuItem item){
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch(item.getItemId()){
		
		case R.id.remove_song_context:
			removeSong(info.id);
			return true;
		}
		
		return super.onContextItemSelected(item);
	}

	/**
	 * Removes a song from the current list
	 * @param id - The position in the list to remove the song from
	 */
	private void removeSong(long id) {
		
		int row = (int) id;
		
		new StreamingCommand_Task().execute(currentZone, songArray.get(row), Command.QUEUE_REMOVE);
		
		songArray.remove(row);
		
		songAdapter.notifyDataSetChanged();
		
	}
	
	/**
	 * Triggered when the hardware back button is pressed.  Used to make sure the same animation is shown.
	 */
	@Override
	public void onBackPressed(){
		
		Intent resultIntent = new Intent();

		Song newSong = songArray.get(0);			
		
		resultIntent.putExtra("newSong", newSong);
		
		Song_Activity.this.setResult(RESULT_OK, resultIntent);
		
		finish();
		
		overridePendingTransition(android.R.anim.slide_in_left, 0);
	}


}
