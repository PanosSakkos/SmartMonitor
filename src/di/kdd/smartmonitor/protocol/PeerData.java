package di.kdd.smartmonitor.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

/***
 * The data that each peer holds for each peer.
 * Thread-safe.
 */

public class PeerData {
	private MasterNode captainNode;
	private Set<String> peerIPs = new TreeSet<String>();

	private static final String TAG = "peer data";
	
	public PeerData() {		
	}
	
	/***
	 * @param captainNode The Captain node to notify when a new peer is added from the KnockKnock thread
	 */
	
	public PeerData(MasterNode captainNode) {
		this.captainNode = captainNode;
	}
	
	/***
	 * Adds an IP address of a new peer
	 * @param ip The new peer's IP address
	 */
	
	public synchronized void addPeerIP(String ip) {
		//TODO check ip validity

		if(peerIPs.contains(ip) == false) {
			peerIPs.add(ip);		
			Log.d(TAG, "Added " + ip);
			
			/* Notify the Captain node about the new peer's IP address
			 * in order to broadcast the new IP to the peers.
			 */
			
			if(captainNode != null) {
				captainNode.newPeerAdded(ip);
			}
		}
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
	
	public void addPeersFromStream(BufferedReader in) throws IOException {
		String peerDataLine;

		while((peerDataLine = in.readLine()) != null) {
			addPeerIP(peerDataLine);
			Log.d(TAG, "Added " + peerDataLine + " from input stream");
		}
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
