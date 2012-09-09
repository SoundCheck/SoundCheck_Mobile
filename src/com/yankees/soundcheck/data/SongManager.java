package com.yankees.soundcheck.data;

import java.util.ArrayList;
import java.util.List;

import soundcheck.shared.Peer;
import soundcheck.shared.Song;
import soundcheck.shared.Zone;
import soundcheck.shared.ZoneProperties;

/**
 * Used for managing song information.
 * 
 *
 */
public class SongManager {
	
	/**
	 * Gets the playlist (queue) for a Zone.
	 * 
	 * @param zone - The Zone to get the playlist for
	 * 
	 * @return - The playlist for the Zone as a List<Song>
	 */
	public static List<Song> getZonePlayList(Zone zone){
		
		ZoneProperties zoneProp = PeerCollection.getZoneProps(zone);
		
		return zoneProp.getPlayList();
		
	}
	
	/**
	 * Returns all of the songs that are available on the network.
	 * 
	 * @return - List of all Songs available on the network
	 */
	public static List<Song> getAvailableSongs(){
		List<Song> allPeerSongs = new ArrayList<Song>();
		
		//List<Peer> peersInZone = PeerCollection.getPeersByZone(zone);
		
		List<Peer> peers = PeerCollection.getPeers();
		
		for(Peer p : peers){
			
			for(Song s : p.getSongList()){
				if(!allPeerSongs.contains(s)){
					allPeerSongs.add(s);
				}
			}
		}
		
		return allPeerSongs;
		
	}

	public static void setCurrentSong(Song newSong, Zone currentZone) {
		
		ZoneProperties zoneProp = PeerCollection.getZoneProps(currentZone);
		
		zoneProp.addFirst(newSong);

	}
	
}
