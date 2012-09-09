package tasks;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yankees.soundcheck.data.PeerCollection;
import com.yankees.soundcheck.network.NetworkChannel;

import soundcheck.shared.Const.Command;
import soundcheck.shared.PacketCreator;
import soundcheck.shared.Peer;
import soundcheck.shared.Song;
import soundcheck.shared.StreamInfo;
import soundcheck.shared.Zone;
import soundcheck.shared.Const.StreamType;
import android.os.AsyncTask;

/**
 * StreamingCommand_Task sends streaming related commands to the peers in the network in the background using
 * an AsyncTask.
 * 
 *
 */
public class StreamingCommand_Task extends AsyncTask<Object, Void, Void> {
	
	//Logger for the class
	final static Logger logger = LoggerFactory.getLogger(StreamingCommand_Task.class);

	/**
	 * The command packet is created and sent to the other peers. 
	 * 
	 * @param[0] - Zone to send the command to
	 * @param[1] - Song object to use in the packet
	 * @param[2] - Command to use in the packet
	 */
	@Override
	protected Void doInBackground(Object... params) {
		
		Zone zone = null;
		Song song = null;
		Command cmd = null;

		//Cast the Object params as the actual types		
		try {
			zone = (Zone) params[0];
			song = (Song) params[1];
			cmd = (Command) params[2];
		} catch (ClassCastException e) {
			logger.error("", e);
		}
		
		int id = song.getId();
		
		Peer hostingPeer = null;

		for ( Peer peer : PeerCollection.getPeers() ) {

			// check if the peers song of that id equals the requested song
			if ( peer.getSongByID(id).equals(song) ) {
				hostingPeer = peer;
			}
		}

		List<Peer> peerList = PeerCollection.getPeers();

		//Send the command to the entire network for a playlist change
		if(cmd == Command.QUEUE_FRONT || cmd == Command.QUEUE_BACK || cmd == Command.QUEUE_REMOVE){
			NetworkChannel.send(null, PacketCreator.createNewQueuePacket(cmd, song, zone));
		}
		
		//Send all other commands to the specific zone they are intended for.
		else{

			StreamInfo strInfo = new StreamInfo(null, song.getId(), hostingPeer.getUid(), StreamType.PUBLISH);

			//Loop through all peers and send the packet to the ones whose zone's match
			for(Peer p : peerList){

				if(p.getZone().getUid().equals(zone.getUid())){

					NetworkChannel.send(p.getAddress(), PacketCreator.createStreamCommandPacket(cmd, strInfo, zone));
				}
			}
		}

				
		return null; //Nothing needs to be returned.
	}


}
