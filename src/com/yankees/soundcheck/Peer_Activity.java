package com.yankees.soundcheck;

import java.util.List;

import soundcheck.shared.Peer;
import soundcheck.shared.Zone;
import tasks.SongInfo_Task;

import com.yankees.soundcheck.data.PeerCollection;

import yankees.soundcheck.mobile.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


/**
 * 
 * Used for displaying the Zone UI to the user and getting the user input regarding zones.
 * 
 *
 */
public class Peer_Activity extends Activity {
	
	/**
	 * Sets the view resource to be the zone_manager layout.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.zone_manager);
	}
	
	/**
	 * Gets the list of Zones to display and puts them into a list. 
	 */
	@Override
	public void onStart(){
		
		super.onStart();
		
		ListView zoneList = (ListView) findViewById(R.id.masterZoneListView);
		
		//Get the list of zones as an list which are the values from the zone list map
		final List<Zone> zoneArray = PeerCollection.getZonesAsList();
		
		if(zoneArray.size() == 0){
			Toast.makeText(getApplicationContext(), "No Zones Found", Toast.LENGTH_LONG).show();
			
			this.runOnUiThread(new Runnable(){

				public void run() {
					Zone defaultZone = new Zone();
					defaultZone.setName("No Zones Found");
					//SoundCheck_Activity.changeDisplayedSong(new Song("No Songs in Playlist", "", "", "", 0, 0));
					SoundCheck_Activity.changeDisplayedSong(null);
					SoundCheck_Activity.changeDisplayedZone(defaultZone);
				}
				
			});
			
			//zoneArray.add("Dummy Zone 1");
			
			//zoneArray.add("Dummy Zone 2");
		}
		
		zoneList.setAdapter(new ArrayAdapter<Zone>(this, R.layout.list_view_item, zoneArray));
		
		zoneList.setClickable(true);
		
		//Define the onItemClick listener and event
		zoneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				Intent resultIntent = new Intent();
				
				resultIntent.putExtra("newZone", zoneArray.get(arg2));
				
				new SongInfo_Task().execute(zoneArray.get(arg2));
				
				Peer_Activity.this.setResult(RESULT_OK, resultIntent);
				
				finish();
				
				overridePendingTransition(android.R.anim.slide_in_left, 0);
			}
				
		});
		
		//Define the onItemLongClickListener
		zoneList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				Zone zone = zoneArray.get(arg2);  //Get the name of the zone clicked
				
				List<Peer> peers = PeerCollection.getPeersByZone(zone);  //Find all peers in that zone
				
				if(peers.size() > 0){  //Should always be true, but just in case

					CharSequence[] peerArray = new CharSequence[peers.size()];  //Need a CharSequence[] for the dialog

					for(int i=0; i < peerArray.length; i++){
						peerArray[i] = peers.get(i).getName();  //Place all of the peer elements in the array
					}


					AlertDialog.Builder builder = new AlertDialog.Builder(Peer_Activity.this);

					builder.setTitle("Peers in " + zone);

					builder.setItems(peerArray, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();  //Close the dialog if an element is selected.  No other action needs to occur.
						}
					});				


					builder.show();
				}
				else{
					Toast.makeText(getApplicationContext(), "No Peers in the selected zone.", Toast.LENGTH_SHORT).show();  //Should not happen but display this toast if there are no peers in the zone.
				}
				
				return false;
			}
			
		});
		
	}
	
	/**
	 * Making use of the same menu for the Peer_Activity and the Song_Activity.
	 * Need to remove the "Add Song" option when using it here.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		
		menu.removeItem(R.id.addSong_songActivity);
		
		return true;
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
		}
		
		return super.onOptionsItemSelected(item);
	}


	/**
	 * Triggered when the hardware back button is pressed.
	 * Used to ensure the same animation is shown when going back using the back button.
	 */
	@Override
	public void onBackPressed(){
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, 0);
	}
	

}
