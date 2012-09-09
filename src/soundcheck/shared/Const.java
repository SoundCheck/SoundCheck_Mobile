package soundcheck.shared;


/**
 * Constant variables that may be used throughout the program
 *
 */
public class Const {
	// Packet data
	public final static String PROJECT_NAME = "SoundCheck";
	
	// Services are for network use
	public enum Service {
		REQUEST_ZONE_MAP // Ask an existing peer for it's zone map
		,GET_ZONE_MAP // Initialize with another peer's zone map
		,DISCOVERY // Sent to ask for more data
		,PEER_DATA // Information on the local peer. Sent in response to discovery
		,PEER_EDIT // A peer has changed (name change, usually)
		,PEER_STATUS // A peer has changed privacy
		,PEER_ZONE // A peer's zone has changed
		,ZONE_MAPPING // A zone-name mapping
		,INTERPROCESS // Packet is for interprocess use. See commands
		,STREAMING // Packet is for streaming use
		,STREAMING_ACK	// Packet it for stream setup acknowledgments
		,SONGLIST // Songlist has been changed for a peer
		,QUEUE_CHANGE // Change is being made to zone's queue
	}
	
	// Commands are for interprocess use.
	public enum Command {
		// Zone management commands
				REQUEST_UPDATE // Request the most recent peer data
				,SET_PEERLIST // Send a list of known SoundCheck peers
				,SET_LOCAL_PEER // Send the local peer's data
				,ZONE_MAP // Contains a new zone map
				,PEER_ZONE_CHANGE // A peer's zone has changed
				,PEER_STATUS_CHANGE // A peer's status has changed
				
				,SET_SONGLIST // Packet is for passing a peer's songlist
				,LIBRARY_UPDATE	// Request the most recent library
				
				,NEW_ZONE // A new zone has been created
				
				,CONFIG // Set configuration options
				
				,QUEUE_FRONT // add song to front of queue
				,QUEUE_BACK // add song to back of queue
				,QUEUE_REMOVE //Remove a song from the queue
				,NEW_QUEUE // Replace the contents of the current queue

				// Streaming commands
				,SETUP  //Create an audio stream
				,PLAY  //Play the audio stream
				,PAUSE //Pause the audio stream
				,NEXT //Skip to the next audio stream
				,TEARDOWN  //Stop the audio stream

				,GET_ZONE_LIST  //Indicates that the list of zones should be returned
				,GET_ZONE_INFO  //Indicates that a zone's playlist should be returned

				,PASSWORD_SET	// Indicates that the user has requested a password be assigned to the selected zone
			}

	//Stream state related information
	public enum PlayBack_State {INIT, PLAYING, SETUP, PAUSED, TEARDOWN};

	//Type of Stream to create
	public enum StreamType {RECEIVE, PUBLISH};

	// Broadcast Discovery constants.
	public final static String DISCOVER_CLUSTER = "SoundCheckDiscovery";

	// Interprocess constants
	public final static int INTERFACE_PORT = 9998;
	
	//Address for StreamPublishers to begin trying to stream on
	public final static String STARTING_MCAST_ADDRESS = "226.0.0.0";
}
