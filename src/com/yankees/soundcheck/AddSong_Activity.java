package com.yankees.soundcheck;

import java.util.List;

import com.yankees.soundcheck.data.SongManager;

import customAdapters.SongAdapter;

import soundcheck.shared.Song;
import soundcheck.shared.Zone;
import soundcheck.shared.Const.Command;
import tasks.StreamingCommand_Task;
import yankees.soundcheck.mobile.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Activity that allows a user to add a song to a Zone's queue.
 * All available songs on the network are displayed for selection.
 * 
 *
 */
public class AddSong_Activity extends Activity{ 
	
	/**
	 * Sets the view resource.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_song_view);
	}
	
	/**
	 * Creates the ListView and gets the Song objects that are to be displayed.
	 */
	@Override
	public void onStart(){
		
		super.onStart();
		
		Bundle extras = this.getIntent().getExtras();
		
		final Zone zone = (Zone) extras.get("zone");
		
		final List<Song> songList = SongManager.getAvailableSongs();
		
		ListView songListView = (ListView) findViewById(R.id.masterSongListView);
		
		final SongAdapter songAdapter = new SongAdapter(this.getApplicationContext(), songList);
		
		songListView.setAdapter(songAdapter);
		
		songListView.setClickable(true);
		
		//The click listener for list items.  Adds the selected song to the queue and sends the command to the network.
		songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				
				Intent addedSongIntent = new Intent();
				
				//Song addedSong = songList.get(arg2);
				
				Song addedSong = (Song) songAdapter.getItem(arg2);
				
				addedSongIntent.putExtra("songToAdd", addedSong);
				
				AddSong_Activity.this.setResult(RESULT_OK, addedSongIntent);
				
				new StreamingCommand_Task().execute(zone, addedSong, Command.QUEUE_BACK);
				
				finish();
			}			
		});
			
		EditText searchBox = (EditText) findViewById(R.id.songSearchBox);
		
		searchBox.addTextChangedListener(new TextWatcher(){

			public void afterTextChanged(Editable s) {
							
			}

			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}

			public void onTextChanged(CharSequence s, int start, int before,int count) {
				songAdapter.filter(s.toString());
				
			}
			
		});
		
		songListView.requestFocus();
	}

}
