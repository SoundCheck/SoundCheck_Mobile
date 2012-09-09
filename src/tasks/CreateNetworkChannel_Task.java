package tasks;

import com.yankees.soundcheck.SoundCheck_Activity;
import com.yankees.soundcheck.network.NetworkChannel;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Runs in the background to setup a NetworkChannel so that communications can occur using the JGroups cluster.
 * 
 *
 */
public class CreateNetworkChannel_Task extends AsyncTask<Context, Void, NetworkChannel>{

	/**
	 * Creates the NetworkChannel and returns the channel object.
	 * 
	 * @param[0] - Context, The application context needed to open files.
	 */
	@Override
	protected NetworkChannel doInBackground(Context... arg0) {
		
		NetworkChannel channel = new NetworkChannel(arg0[0]);
		
		return channel;
	}
	
	/**
	 * Executed when the AsyncTask has completed.  The channel for the application is set to the one
	 * passed in.
	 * 
	 * @param resultChannel - The NetworkChannel to be used by the application.
	 */
	@Override
	protected void onPostExecute(NetworkChannel resultChannel){
		
		SoundCheck_Activity.setChannel(resultChannel);
		
	}

}
