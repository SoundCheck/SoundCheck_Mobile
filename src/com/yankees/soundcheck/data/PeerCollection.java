package com.yankees.soundcheck.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soundcheck.shared.Peer;
import soundcheck.shared.Zone;
import soundcheck.shared.ZoneProperties;

/**
 * Store data on discovered peers on network
 *
 */
public final class PeerCollection {
	final static Logger logger = LoggerFactory.getLogger(PeerCollection.class);

	private static final List<Peer> peerList = new ArrayList<Peer>();
	private static Map<Zone, ZoneProperties> zoneMap = new ConcurrentHashMap<Zone, ZoneProperties>();

	/**
	 * Make this class static with a private constructor
	 */
	private PeerCollection() {
		throw new AssertionError();
	}

	/**
	 * Add peer to peerList, or replace peer if already exists.
	 * Notifies any threads waiting that a change has occurred.
	 * @param peer
	 */
	public synchronized static void addPeer(Peer peer) {
		logger.trace("Adding {} to peerlist.", peer.getName());

		// Check if peer already exists in peerlist
		if( peerList.indexOf(peer) == -1 ) {
			peerList.add(peer);
		} else {
			peerList.set( peerList.indexOf(peer), peer );
		}
	}

	/**
	 * Remove multiple peers with the same address from the peerlist
	 * @param deleteList
	 */
	public synchronized static void removePeers(List<Peer> deleteList) {
		for( Peer peer : deleteList ) {
			peerList.remove(peerList.indexOf(peer));
		}
	}

	/**
	 * Remove all peers from peerList
	 */
	public synchronized static void clearPeers() {
		peerList.clear();
	}

	/**
	 * Return peer at given index in peerList
	 * @param i
	 * @return peer as a string
	 */
	public synchronized static Peer getPeerAtIndex(int i) {
		return peerList.get(i);
	}

	/**
	 * Gets a Peer from the list with the matching UID passed in,
	 * or null if the peer does not exist.
	 * 
	 * @param uid - The UID of the Peer to get
	 * @return - The Peer with the matching UID
	 */
	public synchronized static Peer getPeerByUID(String uid){

		for( Peer peer : peerList) {
			if( peer.getUid().equals(uid) ) {
				return peer;
			}
		}

		return null;
	}

	/**
	 * Return the list of peers
	 * @return peerList as an arraylist of strings
	 */
	public synchronized static List<Peer> getPeers() {
		return peerList;
	}

	/**
	 * Given a list of peers with zones, modifies any duplicate peers
	 * in the existing list with the new zones.
	 * @param newPeerList
	 */
	public synchronized static void updatePeerZones(List<Peer> newPeerList) {
		Peer peer;

		/* Note that the new peerlist does not contain correct IP and Address
		 * data, so they should only be used for seeing what the new zone is. */
		for(Peer newPeer : newPeerList) {
			if( peerList.contains(newPeer) ) {
				peer = peerList.get( peerList.indexOf(newPeer) );
				peer.setZone(newPeer.getZone());
			}
		}
	}
	
	/**
	 * Given a peer, modifies the corresponding peer in the PeerCollection
	 * with the new list of songs.
	 * 
	 * @param peer
	 */
	public synchronized static void updateSongList( Peer peer ) {
		
		Peer songPeer = peerList.get( peerList.indexOf( peer ));
		
		songPeer.setSongList( peer.getSongList() );
	}

	/**
	 * Performs a map "put" operation on the zoneMap
	 * @param zone
	 * @param name
	 */
	public synchronized static void putZone(Zone zone, ZoneProperties prop) {
		zoneMap.put(zone, prop);
	}

	/**
	 * @return the zonemap
	 */
	public synchronized static Map<Zone, ZoneProperties> getZoneMap() {
		return zoneMap;
	}

	/**
	 * @param zoneMap the zoneMap to set
	 */
	public synchronized static void setZoneMap(Map<Zone, ZoneProperties> zoneMap) {
		PeerCollection.zoneMap = zoneMap;
	}
	
	/**
	 * @return the zonemap
	 */
	public synchronized static ZoneProperties getZoneProps(Zone zone) {
		return zoneMap.get(zone);
	}
	
	/**
	 * Given a zone, finds all peers from zoneList that are paired to that zone.
	 * @param zone
	 * @return
	 */
	public static List<Peer> getPeersByZone(Zone zone) {

		List<Peer> peersInZone = new ArrayList<Peer>();

		// if (zoneMap.containsValue(zone)) {

		for (Peer p : peerList) {
			if (p.getZone().getUid().equals(zone.getUid())) {
				peersInZone.add(p);
			}
		}
		// }

		return peersInZone;

	}
	
	/**
	 * Returns all Zones as a List so that they can be displayed easier.
	 * 
	 * @return - List<Zone> of all the Zones
	 */
	public static List<Zone> getZonesAsList() {
		List<Zone> zoneList = new ArrayList<Zone>();
		
	    //Set<Zone> zoneSet = PeerCollection.getZoneMap().keySet();
		
		List<Peer> peerList = PeerCollection.getPeers();
		
		for(Peer p : peerList){
			try{
			if(!zoneList.contains(p.getZone())){
				p.getZone().setName((zoneMap.get(p.getZone()).getZoneName()));
				zoneList.add(p.getZone());
			}
			}
			catch(NullPointerException e){
				logger.error("{}",e);
				continue;
			}
		}
		
//		for(Zone zone : zoneSet){
//			if(zone.getName() != null){
//				zoneList.add(zone);
//			}
//		}
		
		return zoneList;
	}
}
