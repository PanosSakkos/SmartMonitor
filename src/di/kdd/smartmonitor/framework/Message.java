package di.kdd.smartmonitor.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;

import android.util.Log;

import di.kdd.smartmonitor.framework.ISmartMonitor.Tag;

/***
 * The message that is exchanged in every communication under
 * the BuildMondProtocol.
 */

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	private Tag tag;
	private String payload;
	
	/***
	 * Returns the message's tag and it must be one of the
	 * tags that the di.kdd.protocol.IProtocol interface defines.
	 * 
	 * @return The message's tag
	 */
	
	public Tag getTag() {
		return tag;
	}
	
	/***
	 * Returns the message's payload which will may vary, 
	 * based on the tag of the message.
	 *
	 * @return The message's payload
	 */
	
	public String getPayload() {
		return payload;
	}

	/***
	 * Returns the requested string of the payload
	 * @param index Which string of the payload to return
	 * @return The String at the @index position of the payload
	 */
	
	public String getPayloadAt(int index) {
		String delimeter = "\n";
		String []tokens = payload.split(delimeter);
		
		return tokens[index];
	}
	
	/***
	 * Initialize Message with ignored payload
	 * @param tag The message's tag
	 */
	
	public Message(Tag tag) {
		this.tag = tag;
	}
	
	/***
	 * @param tag The message's tag
	 * @param payload The message's payload
	 * @throws InvalidTagException 
	 */
	
	public Message(Tag tag, String payload) {
		this.tag = tag;
		this.payload = payload;
	}
	
	/***
	 * Initializes a Message instance, given a BufferedReader, which is
	 * a socket's input stream, from within the system.
	 * @param in The socket's Buffered
	 * @throws IOException 
	 */
	
	public Message(BufferedReader in) throws IOException {
		String line;
		line = in.readLine();
Log.d("message", line);
		this.tag = Tag.valueOf(line);

		while((line = in.readLine()) != null) {
			this.payload += line + '\n';
		}
	}
	
	/***
	 * Adds data to the message with the form of a string  
	 * @param data The data to add to the paylod
	 */
	
	public void addToPaylod(String data) {
		if(payload == null) {
			payload = new String();
			payload = data;
		}
		else {
			payload += "\n" + data;			
		}		
	}
	
	/***
	 * Returns the tag and the payload, separated by a new line character.
	 */
	
	@Override
	public String toString() {
		return payload == null ? tag.name() : tag.name() + "\n" + payload;
	}
}
