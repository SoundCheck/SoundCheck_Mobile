package soundcheck.shared;

import java.io.Serializable;
import java.util.UUID;

public class Zone implements Serializable {
	private static final long serialVersionUID = -3990308439172422627L;
	
	private UUID uid;
	private String name;

	public Zone() {
		this.uid = UUID.randomUUID();
	}

	@Override
	public boolean equals(Object zone) {
		if(zone instanceof Zone) {
			if( uid.equals( ((Zone)zone).getUid())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return uid.hashCode();
	}

	/**
	 * @return the uid
	 */
	public UUID getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(UUID uid) {
		this.uid = uid;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
