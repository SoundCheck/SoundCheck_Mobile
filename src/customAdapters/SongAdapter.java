package customAdapters;

import java.util.ArrayList;
import java.util.List;

import soundcheck.shared.Song;
import yankees.soundcheck.mobile.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Custom display Adapter for Song objects.  Puts the title of the song on the top line and the artist and duration
 * on the bottom line.
 * 
 *
 */
public class SongAdapter extends BaseAdapter {
	
	private List<Song> orig_items;  //The original items / complete list
	private List<Song> items;  //List that changes to reflect the filter that is applied
	private Context context;  //App context used for inflating layouts
	
	/**
	 * Constructor for the adapter.
	 * 
	 * @param context - The context to be used
	 * @param songs - The complete list of songs to be displayed
	 */
	public SongAdapter(Context context, List<Song> songs){
		this.orig_items = songs;
		this.items = songs;
		this.context = context;
	}

	/**
	 * Returns the total number of items currently being displayed
	 */
	public int getCount() {
		return items.size();
	}

	/**
	 * Returns the item at the specified index
	 * 
	 * @param position - The index to retrieve the element at.
	 */
	public Object getItem(int position) {
		return items.get(position);
	}

	/**
	 * Returns the ID of the item at the specified index. Currently the ID is
	 * equal to the index so position is returned.
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Configures each list element to be displayed.
	 * 
	 * Each item is inflated using the song_list_item layout
	 */
	public View getView(int position, View convertView, ViewGroup parent) {

		View songView = convertView;


		if(songView == null){
			LayoutInflater li = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			songView = li.inflate(R.layout.song_list_item, null);
		}

		//Get the song from the list
		Song songToAdd = (Song) this.getItem(position);

		//Set the TextViews with the proper text from the Song object
		if(songToAdd != null){
			TextView titleText = (TextView)songView.findViewById(R.id.songTitle);

			TextView artistText = (TextView) songView.findViewById(R.id.songArtist);

			TextView durationText = (TextView) songView.findViewById(R.id.songDuration);

			if(titleText != null){

				titleText.setText(songToAdd.getTitle());
			}

			if(artistText != null && songToAdd.getArtist() != null){
				artistText.setText(songToAdd.getArtist());
			}

			if(durationText != null && songToAdd.getDurationFormatted() != null){
				durationText.setText(songToAdd.getDurationFormatted());
			}
		}


		return songView;
	}
	
	/**
	 * Filters the list using the string passed in.
	 * The filter is checked to see if it is a prefix of the song title.
	 * 
	 * @param filter - The string to check against the song title.
	 */
	public void filter(String filter){
		
		List<Song> filteredList = new ArrayList<Song>();

		//Filter passed in, check all elements in orig_items
		if(filter.length() > 0){

			for(Song song : orig_items){
				if(song.getTitle().startsWith(filter)){  //See if the song title starts with the filter (Is the filter a prefix of the title?)
					filteredList.add(song);
				}
			}
			
			items = filteredList;  //Set items to the list of matching elements
		}
		else{
			items = orig_items;  //No filter passed in, set items to be the list of all elements
		}

		notifyDataSetChanged();  //Inform the adapter that the list needs to be updated.

	}

}
