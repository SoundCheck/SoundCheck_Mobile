package com.yankees.soundcheck.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Map;

import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.PhysicalAddress;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yankees.soundcheck.data.PeerCollection;

import soundcheck.shared.Const;
import soundcheck.shared.DataPacket;
import soundcheck.shared.PacketCreator;
import soundcheck.shared.Zone;
import soundcheck.shared.ZoneProperties;
import tasks.PeerManager_Task;

import android.content.Context;


/**
 * Manages the sending of streaming commands to the appropriate receiver
 * 
 *
 */

public class NetworkChannel extends ReceiverAdapter {

	final static Logger logger = LoggerFactory.getLogger(NetworkChannel.class);

	private static JChannel channel;

	/**
	 * 
	 * @param context - The application context. (Needed to open files)
	 */
	public NetworkChannel(Context context){
		
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		try {
			channel = new JChannel(context.getAssets().open("udp_bping.xml"));
			channel.setReceiver(this);
			channel.setDiscardOwnMessages(true);
			channel.connect(Const.DISCOVER_CLUSTER, null, 5000);
			logger.debug("channel created");			
		} catch (Exception e) {
			logger.error("",e);
		}


	}

	@Override
	public synchronized void viewAccepted(View view){
		logger.trace("Detected change in peers.");

		new PeerManager_Task().execute(view.getMembers());
	}

	@Override
	public void receive(Message msg){

		logger.trace("Received packet from {}.", msg.getSrc().toString() );

		MessageHandler messageHandler = new MessageHandler(this,msg);
		new Thread(messageHandler).start();

	}

	/**
	 * Sends a packet to the Address passed in.
	 * @param dst - The destination of the packet. (Null for all members of the cluster)
	 * @param packet - The packet to send.
	 */
	public static void send(Address dst, DataPacket packet){

		if(channel != null){
			try {
				logger.trace( "Sending {} packet to {}", packet.getService().toString(), dst == null ? "Everyone" : dst.toString() );

				//Check to make sure the channel is connected before sending
				if(channel.isConnected()){
					channel.send(dst, packet);
				}

			} catch (Exception e) {
				logger.error("",e);
			}
		}

	}

	public void destroy(){
		channel.close();
	}
	
	/**
	 * Gets the state from the cluster after joining.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setState(InputStream input) throws Exception{
		logger.trace("Receiving new state from cluster.");
		
		Map<Zone, ZoneProperties> zoneList = (Map<Zone, ZoneProperties>)Util.objectFromStream(new DataInputStream(input));
		
		PeerCollection.setZoneMap(zoneList);
		
		NetworkChannel.send(null, PacketCreator.createDiscoveryPacket());
	}
	
	/**
	 * Sends the current cluster state to a new member.
	 */
	@Override
	public void getState(OutputStream output) throws Exception{
		logger.trace("Uploading new state to cluster.");

        Util.objectToStream( PeerCollection.getZoneMap(), new DataOutputStream(output) );
	}
	 
	
	/** Gets the physical IP address from a given JGroups address.
	 * @param addr
	 * @return IP address or null if it could not be determined.
	 */
	public String getIp(Address addr) {
		/* WARNING. This is dangerous code, as the JGroups API hides the 
		 * IP address and it could change between releases. However, this
		 * is the cleanest and easiest way for us to do this.
		 */
		PhysicalAddress physicalAddr = (PhysicalAddress)channel.down(new Event(Event.GET_PHYSICAL_ADDRESS, addr));
		if(physicalAddr instanceof IpAddress) {
			IpAddress ipAddr = (IpAddress)physicalAddr;
			InetAddress inetAddr = ipAddr.getIpAddress();
			return inetAddr.getHostAddress();
		}

		logger.warn( "Could not determine {}'s IP address.", addr.toString() );
		return null;
	}
	


}
