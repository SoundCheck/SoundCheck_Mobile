package tasks;

import java.util.List;

import soundcheck.shared.PacketCreator;
import soundcheck.shared.Song;
import soundcheck.shared.Zone;
import com.yankees.soundcheck.SoundCheck_Activity;
import com.yankees.soundcheck.data.SongManager;
import com.yankees.soundcheck.network.NetworkChannel;

import android.os.AsyncTask;

/**
 * SongInfo_Task runs in the background to get the playlist for a specific zone.
 * 
 *
 */
public class SongInfo_Task extends AsyncTask<Zone, Void, Song> {

	/**
	 * Retrieves the playlist for Zone passed in.
	 * 
	 * @param[0] - Zone to retrieve the playlist for.
	 * 
	 * @return - Song that is the first to be played.
	 */
	@Override
	protected Song doInBackground(Zone... params) {
		
		Zone selectedZone = (Zone) params[0];
		
		NetworkChannel.send(null, PacketCreator.createRequestZoneMapPacket());
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		List<Song> songList = SongManager.getZonePlayList(selectedZone);
		
		Song firstSong = new Song("No Songs in Playlist", "", "", "", 0, 0);
		
		if(songList.size() > 0){
			firstSong = songList.get(0);
		}
		
		return firstSong;
	}
	
	/**
	 * Action when the background work has completed.  The main UI screen is updated
	 * to show the song that is passed in.
	 * 
	 * @param - Song to display on the control screen.
	 */
	@Override
	protected void onPostExecute(Song song){
		
		SoundCheck_Activity.changeDisplayedSong(song);
	}

}
