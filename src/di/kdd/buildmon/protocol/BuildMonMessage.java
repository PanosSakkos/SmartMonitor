package di.kdd.buildmon.protocol;

import di.kdd.buildmon.protocol.IProtocol.Tag;

/***
 * The message that is exchanged in every communication under
 * the BuildMondProtocol.
 */

public class BuildMonMessage {
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
	 * @param payload The message's paylaod
	 * @throws InvalidTagException 
	 */
	
	public BuildMonMessage(Tag tag, String payload) {
		this.tag = tag;
		this.payload = payload;
	}
}
