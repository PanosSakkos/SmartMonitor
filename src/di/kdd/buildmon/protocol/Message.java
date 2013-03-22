package di.kdd.buildmon.protocol;

import java.io.BufferedReader;
import java.io.IOException;

import di.kdd.buildmon.protocol.IProtocol.Tag;

/***
 * The message that is exchanged in every communication under
 * the BuildMondProtocol.
 */

public class Message {
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
	 * Initializes a BuildMonMessage instance with the given tag 
	 * and payload.
	 * 
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
		this.tag = Tag.valueOf(in.readLine());

		while((line = in.readLine()) != null) {
			this.payload += line + '\n';
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
