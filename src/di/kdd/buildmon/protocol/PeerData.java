package di.kdd.buildmon.protocol;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/***
 * The data that each peer holds for each peer.
 * Thread-safe.
 */

public class PeerData {
	private Protocol protocolThread;
	private Set<String> peerIPs = new TreeSet<String>();
	
	/***
	 * @param protocolThread The Protocol instance that will 
	 * be notified for thew new IP addresses.
	 */
	
	public PeerData(Protocol protocolThread) {
		this.protocolThread = protocolThread;
	}
	
	/***
	 * Adds an IP address of a new peer
	 * @param ip The new peer's IP address
	 */
	
	public synchronized void addPeerIP(String ip) {
		//TODO check ip validity
		peerIPs.add(ip);		
		protocolThread.newPeerAdded(ip);
	}
	
	/***
	 * Removes the IP address of a fallen peer
	 * @param ip The IP address to remove
	 */
	
	public synchronized void removePeerIP(String ip) {
		peerIPs.remove(ip);
	}
	
	/***
	 * Returns the lowest IP. Must be used in order to find
	 * the new Captain in case of Captain node failure.
	 * @return The lowest of the peer IP addresses.
	 */
	
	public synchronized String getLowestIP() {
		return Collections.min(peerIPs);
	}
	
	/***
	 * Return the IP addresses of the peers, separated by a new line character
	 */
	
	@Override
	public synchronized String toString() {
		String string = new String();
		
		for(String ip : peerIPs) {
			string += ip + '\n';
		}
		
		return string;
	}
}
