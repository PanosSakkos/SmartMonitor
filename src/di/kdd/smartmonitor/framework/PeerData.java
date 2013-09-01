package di.kdd.smartmonitor.framework;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.conn.util.InetAddressUtils;

import di.kdd.smartmonitor.framework.ISmartMonitor.Tag;


import android.util.Log;

/***
 * The data that each peer holds for each peer.
 * Thread-safe.
 */

public class PeerData implements IObservable {
	private IObserver masterNodeObserver;
	private String nodeIP;
	private String masterIP;
	private TreeSet<String> peerIPs = new TreeSet<String>();

	private static final String TAG = "PeerData";
	
	public PeerData(){
		nodeIP = PeerData.getLocalIpAddress();
	}
	
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress().toString())) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
//	        Log.e(LOG_TAG, ex.toString());
	    }
	    return null;
	}
	
	
	
	public String getNodeIP() {
		return nodeIP;
	}
	
	public void setMasterIP(String masterIP) {
		
		if(masterIP.charAt(0) == '/') {
			masterIP = masterIP.substring(1);
		}

		this.masterIP = masterIP;
	}
	
	/* IObservable implementation */
	
	@Override
	public void subscribe(IObserver observer) {
		this.masterNodeObserver = observer;
	}

	@Override
	public void unsubscribe(IObserver observer) {
		this.masterNodeObserver = null;
	}

	@Override
	public void notify(String message) {
		if(masterNodeObserver != null) {
			masterNodeObserver.update(message);
		}
	}
	
	/***
	 * Adds an IP address of a new peer
	 * @param ip The new peer's IP address
	 */
	
	public synchronized void addPeerIP(String ip) {
		//TODO check ip validity

		/* Remove the / character that is added from the socket.getInetAddress method */

		if(ip.charAt(0) == '/') {
			ip = ip.substring(1);
		}
		
		if(peerIPs.contains(ip) == false) {
			peerIPs.add(ip);	
			
			Log.i(TAG, "Added " + ip);
			
			if(nodeIP == null) {
				nodeIP = ip;
			}
			
			/* Notify the Master node about the new peer's IP address
			 * in order to broadcast the new IP to the peers.
			 */
			
			notify(ip);
		}
	}
	
	

	
	/***
	 * Given a BufferedReader of a socket input stream, parses the payload 
	 * per line and stores the IP addresses that finds
	 * @param in Socket input stream holding the payload with the IP addresses
	 * @throws IOException
	 */
	
	public void addPeersFromMessage(Message message) throws IOException {
		String peerDataLine;
		Scanner in = new Scanner(message.getPayload());

		while(in.hasNextLine()) {
			peerDataLine = in.nextLine();
			addPeerIP(peerDataLine);
			
			Log.i(TAG, "Added " + peerDataLine + " from message");
		}
	}
	
	/***
	 * Removes the IP address of a fallen peer
	 * @param ip The IP address to remove
	 */
	
	public synchronized void removePeerIP(String ip) {
		/* Remove the / character that is added from the socket.getInetAddress method */

		if(ip.charAt(0) == '/') {
			ip = ip.substring(1);
		}

		peerIPs.remove(ip);
		
		Log.i(TAG, "Removed peer IP-address: " + ip);
		
		/* If this node is the Master, inform the Peer nodes for the failure in order to
		 * update their states.
		 */
		
		if(masterNodeObserver != null) {
			((MasterNode) masterNodeObserver).broadcastCommand(new Message(Tag.FAILED_PEER, ip));
		}
		
	}
	
	/***
	 * Returns the lowest IP. Must be used in order to find
	 * the new Captain in case of Captain node failure.
	 * @return The lowest of the peer IP addresses.
	 */
	
	public synchronized String getLowestIP() {
		return peerIPs.first();
	}
			
	public synchronized void forgetMasterIP() {
		Log.i(TAG, "Forgeting Master's IP adddress " + masterIP);

		peerIPs.remove(masterIP);
		masterIP = null;
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
