package tasks;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soundcheck.shared.PacketCreator;
import soundcheck.shared.Zone;
import soundcheck.shared.ZoneProperties;

import android.os.AsyncTask;

import com.yankees.soundcheck.SoundCheck_Activity;
import com.yankees.soundcheck.data.PeerCollection;
import com.yankees.soundcheck.network.NetworkChannel;
/**
 * ZoneInfo_Task is an AsyncTask which is used to get information for all zones on the network.
 * 
 *
 */
public class ZoneInfo_Task extends AsyncTask<Void, Void, Zone> {
	
	//Logger for this class
	final static Logger logger = LoggerFactory.getLogger(ZoneInfo_Task.class);
	
	/**
	 * The action that is called when the Task has finished executing.
	 * 
	 * @param zone - Zone returned from doInBackground
	 */
	@Override
	protected void onPostExecute(Zone zone){
		
		SoundCheck_Activity.changeDisplayedZone(zone);
		
		this.cancel(true); //This task only needs to be performed once, cancel after it has returned.
	}

	/**
	 * The work that is performed in the background.  Task waits up to 10 seconds for a Zone to be discovered.
	 * 
	 */
	@Override
	protected Zone doInBackground(Void... params) {
		
		NetworkChannel.send(null, PacketCreator.createDiscoveryPacket());
		
		int times = 0;
		
		//The PeerCollection zoneMap holds all discovered zones.
		while(PeerCollection.getZoneMap().size() == 0 && times <= 10){
			
			try {
				Thread.sleep(1000);  //Sleep for 1 sec in between to reduce CPU usage
			} catch (InterruptedException e) {
				logger.warn("",e);
			}
			
			times++;
		}
		
		Zone firstZone = new Zone();
		
		//A zone has been discovered, retrieve the first one in the list to display on the UI.
		if(PeerCollection.getZoneMap().size() > 0){
			
			Set<Entry<Zone, ZoneProperties>> zoneMapSet = PeerCollection.getZoneMap().entrySet();
			
			//Entry<Zone, ZoneProperties> firstEntry = zoneMapSet.iterator().next();
			
			Entry<Zone, ZoneProperties> firstEntry;
			
			Iterator<Entry<Zone, ZoneProperties>> it = zoneMapSet.iterator();
			
			firstEntry = it.next();
			
			while(it.hasNext()){
				Entry<Zone, ZoneProperties> entry = it.next();
				Zone entryZone = entry.getKey();
				entryZone.setName(entry.getValue().getZoneName());
			}
			
			//Get song information for the chosen zone
			new SongInfo_Task().execute(firstEntry.getKey());
			
			firstZone = firstEntry.getKey();
			
			firstZone.setName(firstEntry.getValue().getZoneName());
		}
		else{
			//No zones found, set the message accordingly
			firstZone.setName("No Zones Found");
		}
		
		logger.info("Zone scan complete");
		
		return firstZone;
	}

}
