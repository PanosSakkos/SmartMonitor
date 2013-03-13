package di.kdd.buildmon.protocol;

/***
 * The message that is exchanged in every communication under
 * the BuildMondProtocol.
 *
 */

public class BuildMonMessage {

	private String tag;
	private String payload;
	
	/***
	 * Returns the message's tag and it must be one of the
	 * tags that the di.kdd.protocol.IProtocol interface defines.
	 * 
	 * @return The message's tag
	 */
	
	public String getTag() {
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
	
	public BuildMonMessage(String tag, String payload) throws InvalidTagException {
		assertTag(tag);
		
		this.tag = tag;
		this.payload = payload;
	}

	/***
	 * Asserts that the tag is valid, according to the tags
	 * that are defined in the di.kdd.protocol.IProtocol interface.
	 * 
	 * @param The tag that needs to be asserted.
	 */
	
	private void assertTag(String tag) throws InvalidTagException {
		if(tag.equals(IProtocol.HEARTBEAT_TAG)) {
			return;
		}
		
		if(tag.equals(IProtocol.KNOCK_KNOCK_TAG)) {
			return;
		}
		
		if(tag.equals(IProtocol.GET_PEAKS)) {
			return;
		}
		
		throw new InvalidTagException();
	}
}
