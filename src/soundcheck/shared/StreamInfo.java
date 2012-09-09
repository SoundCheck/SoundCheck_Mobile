package soundcheck.shared;

import java.io.Serializable;

import soundcheck.shared.Const.StreamType;

/**
 * The StreamInfo object is used to combine all of the necessary information to describe a stream
 * in one object that can be sent between Peers.
 * 
 *
 */
public class StreamInfo implements Serializable {
	
	//Serial ID for the StreamInfo class
	private static final long serialVersionUID = 2458541938062862870L;

	//The IP Address of the stream.
	private String ipAddress;
	
	//The ID of the song that the stream is playing.  (Unique by Peer)
	private int songID;
	
	//The ID of the Peer that is hosting the stream.
	private String peerUID;
	
	//The type of the stream. (StreamType.PUBLISH, StreamType.RECEIVE)
	private StreamType streamType;
	
	//The file path of the song local to the hosting peer.
	private String localFilePath;


	/**
	 * Constructor
	 * @param ipAddress
	 * @param songID
	 * @param peerUID
	 * @param streamType
	 */
	public StreamInfo(String ipAddress, int songID, String peerUID, StreamType streamType){
		this.ipAddress = ipAddress;
		
		this.songID = songID;
		
		this.peerUID = peerUID;
		
		this.streamType = streamType;
	}


	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}


	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	/**
	 * @return the songID
	 */
	public int getSongID() {
		return songID;
	}


	/**
	 * @return the peerUID
	 */
	public String getPeerUID() {
		return peerUID;
	}


	/**
	 * @return the localFilePath
	 */
	public String getLocalFilePath() {
		return localFilePath;
	}


	/**
	 * @param localFilePath the localFilePath to set
	 */
	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}
	
	/**
	 * @return the streamType
	 */
	public StreamType getStreamType() {
		return streamType;
	}
	
	

}
