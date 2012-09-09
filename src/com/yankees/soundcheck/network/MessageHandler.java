package com.yankees.soundcheck.network;

import java.util.List;

import org.jgroups.Address;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soundcheck.shared.DataPacket;
import soundcheck.shared.PacketCreator;
import soundcheck.shared.Peer;
import soundcheck.shared.Song;
import soundcheck.shared.Zone;
import soundcheck.shared.ZonePair;
import soundcheck.shared.ZoneProperties;
import com.yankees.soundcheck.data.PeerCollection;

/**
 * Determines which handler to pass the packet to
 * based on the packets contents.
 *
 */
public class MessageHandler implements Runnable {
	final static Logger logger = LoggerFactory.getLogger(MessageHandler.class);

	private final Message msg;
	private final NetworkChannel channel;

	/**
	 * Constructor for MessageHandler
	 * @param msg - The message to be processed
	 */
	public MessageHandler( NetworkChannel channel, Message msg ) {
		this.msg = msg;
		this.channel = channel;
	}


	/**
	 * Message handler.  Passes the packet data to the proper controller based on the Service type.
	 */
	private void handleMessage() {

		DataPacket packet = (DataPacket)msg.getObject();

		switch( packet.getService() ) {

		case DISCOVERY:
			// When receiving a discovery packet, send local data to requester
			logger.trace("Received discovery packet from {}.", msg.getSrc().toString());

			if(!msg.getSrc().toString().contains("localhost")){
				NetworkChannel.send(msg.getSrc(), PacketCreator.createDiscoveryPacket());
			}

			//Peer thisPeer = channel.getThisPeer();
			//channel.send( msg.getSrc(), PacketCreator.createDataPacket(thisPeer) );
			break;

		case PEER_DATA:
			// When receiving a data packet, add new peer to peerlist
			logger.trace("Received data packet from {}.", msg.getSrc().toString());

			Peer newPeer = (Peer) packet.getData();
			Address srcAddr = msg.getSrc();
			String srcIp = channel.getIp(srcAddr);

			newPeer.setAddress(srcAddr);
			newPeer.setIp(srcIp);

			logger.trace("New Peer\n\tName: {}\n\tIP: {}", newPeer.getName(),newPeer.getIp());

			PeerCollection.addPeer(newPeer);

			break;

		case STREAMING:
			// Receiving a stream control packet
			logger.trace("Received stream control packet from {}.", msg.getSrc().toString());

			//streamControl.receive(msg);
			break;
		case ZONE_MAPPING:
			// A new zone has been created or a zone has been renamed.

			ZonePair zonePair = (ZonePair) packet.getData();

			PeerCollection.putZone(zonePair.getZone(), zonePair.getProp());

			break;
		case QUEUE_CHANGE:

			logger.trace("QUEUE_CHANGE packet received from {}", msg.getSrc().toString());

			// Get current zone data
			Zone zone = packet.getZone();
			ZoneProperties zoneProp = PeerCollection.getZoneProps(zone);

			// Change queue in proper way
			switch (packet.getCommand()) {
			case QUEUE_FRONT:
				zoneProp.addFirst((Song) packet.getData());
				break;
			case QUEUE_BACK:
				zoneProp.addLast((Song) packet.getData());
				break;
			case QUEUE_REMOVE:
				zoneProp.removeSong((Song) packet.getData());
				break;
			case NEW_QUEUE:
				zoneProp.setPlayList((List<Song>) packet.getData());
			default:
				logger.warn("Unknown QUEUE_CHANGE command {}", packet.getCommand().toString());
			}

			// Write new queue to peer collection
			PeerCollection.putZone(zone, zoneProp);
			break;
		case PEER_ZONE:
			// Modify the zones for received peers
			@SuppressWarnings("unchecked")
			List<Peer> peerList = (List<Peer>)packet.getData();

			PeerCollection.updatePeerZones(peerList);
			break;
		case SONGLIST:

			logger.trace("Received song list update from {}", msg.getSrc().toString());
			// receiving data packet, update songlist for sending peer

			// peer object passed in data packet
			Peer songPeer = (Peer) packet.getData();

			// get peer with same uid from local collection and update songlist to newly passed peer
			PeerCollection.updateSongList(songPeer);

			break;


		default:
			logger.warn("Unrecognized packet type \"{}\" received from {}.", packet.getService().toString(), msg.getSrc().toString() );
		}
	}

	/**
	 * Implementation of run() from Runnable.  handleMessage() is called to process the message.
	 */
	public void run() {
		handleMessage();

	}


}
