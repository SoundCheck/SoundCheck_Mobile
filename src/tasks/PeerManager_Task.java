package tasks;

import java.util.ArrayList;
import java.util.List;

import org.jgroups.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.AsyncTask;

import com.yankees.soundcheck.data.PeerCollection;
import com.yankees.soundcheck.network.NetworkChannel;

import soundcheck.shared.DataPacket;
import soundcheck.shared.PacketCreator;
import soundcheck.shared.Peer;

/**
 * Manages determining of who to add or delete to the PeerList.
 *
 */
public class PeerManager_Task extends AsyncTask<List<Address>, Void, Void> {
	final static Logger logger = LoggerFactory.getLogger(PeerManager_Task.class);

	private List<Peer> peerList;
	private List<Address> viewMembers;
	
	private List<Peer> deleteList;
	private List<Address> addList;
	private DataPacket packet;
	
//	//List to store the addresses for all mobile devices that have been discovered.
//	private static List<Address> mobileAddresses;
//
//	/**
//	 * @return the mobileAddresses
//	 */
//	public static List<Address> getMobileAddresses() {
//		if(mobileAddresses == null){
//			mobileAddresses = new ArrayList<Address>();
//		}
//		return mobileAddresses;
//	}

//	/**
//	 * Constructor takes parameters for the threads usage.
//	 * @param peerList
//	 * @param viewMembers
//	 */
//	public PeerManager( List<Peer> peerList, List<Address> viewMembers ) {
//		this.peerList = peerList;
//		this.viewMembers = viewMembers;
//	}

	/**
	 * Delete peers that no longer exist from memory, and request more information
	 * on new peers that didn't exist in memory before.
	 * @return 
	 */
	@Override
	protected Void doInBackground(List<Address>... params) {
		
		this.peerList = PeerCollection.getPeers();
		this.viewMembers = params[0];
		
		
		synchronized(this) {
			deleteList = getDeletions();
			addList = getAdditions();
			
			PeerCollection.removePeers(deleteList);
			packet = PacketCreator.createDiscoveryPacket();
			
			// Send discovery packets to all peers we have no information on yet.
			for( Address addr : addList ) {
				NetworkChannel.send(addr, packet);
			}
		}
		
		return null;

	}

	/**
	 * If peers in peerlist do not exist in the given list, remove them
	 * from the peerlist.
	 * @return The list of peers that should be deleted.
	 */
	private List<Peer> getDeletions() {
		List<Peer> deleteList = new ArrayList<Peer>();
		boolean isPeerDead;

		for( Peer peer : peerList ) {
			isPeerDead = true;
			
			if( viewMembers.contains(peer.getAddress()) ) {
				isPeerDead = false;
			}

			// Peer was not found in view
			if( isPeerDead == true ) {
				logger.trace("Removing peer {}.", peer.toString());
				deleteList.add(peer);
				
//				//This check removes mobile devices that have left the group from the list of mobile peers
//				if(mobileAddresses.contains(peer.getAddress())){
//					mobileAddresses.remove(peer.getAddress());
//					
//					//Don't need to be using the memory if there are no mobile devices being used
//					//Lazy loading strategy will ensure the list will be re-created when needed again.
//					if(mobileAddresses.size() == 0){
//						mobileAddresses = null;
//					}
//				}
			}
		}
		return deleteList;
	}

	/**
	 * If a member of the given list are not existent in the peerlist,
	 * add it to the list.
	 * @return The list of addresses that should be added.
	 */
	private List<Address> getAdditions() {
		List<Address> addList = new ArrayList<Address>();
		boolean isNewPeer;
		
		for( Address member : viewMembers ) {
			isNewPeer = true;

			for( Peer peer : peerList ) {
				if( member.equals(peer.getAddress()) ) {
					// Peer was found
					isNewPeer = false;
					break;
				}
			}

			// Member was not found in peerlist
			if( isNewPeer == true ) {
				logger.trace("Detected new peer at {}.", member.toString());
				addList.add(member);
			}
		}
		return addList;
	}

//	public static void addMobileAddress(Address src) {
//		if(mobileAddresses == null){
//			mobileAddresses = new ArrayList<Address>();
//		}
//		
//		mobileAddresses.add(src);
//		
//	}
}
