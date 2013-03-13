package di.kdd.buildmon.protocol;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/***
 * The data that each peer holds for each peer.
 */

public class PeerData {
	private Set<String> peerIPs = new TreeSet<String>();
	
	public void addPeerIP(String ip) {
		//TODO FIXME assert ip validity
		peerIPs.add(ip);
	}
	
	public void removePeerIP(String ip) {
		peerIPs.remove(ip);
	}
	
	/***
	 * Returns the lowest IP. Must be used in order to find
	 * the new Captain in case of Captain node failure.
	 * @return The lowest of the peer IP addresses.
	 */
	
	public String getLowestIP() {
		return Collections.min(peerIPs);
	}
}
