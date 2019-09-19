package babel.protocol.event;

import babel.Babel;

/**
 * An abstract class that represent an event that is used to
 * perform interactions between protocols in the local process
 *
 * @see babel.requestreply.ProtocolRequest
 * @see babel.requestreply.ProtocolReply
 */
public abstract class ProtocolInteraction extends ProtocolEvent{

    private short senderID;

    private short destinationID;

    /**
     * Creates a protocol interaction event with the provided type
     * The type should be either REQUEST_EVENT or REPLY_EVENT
     * @see ProtocolEvent
     * @param type type of the event
     */
    public ProtocolInteraction(EventType type) {
        super(type);
    }

    /**
     * Creates a protocol interaction event with the provided type and numeric identifier
     * The type should be either REQUEST_EVENT or REPLY_EVENT
     * @see ProtocolEvent
     * @param type type of the event
     * @param id numeric identifier
     */
    public ProtocolInteraction(EventType type, short id) {
        super(type, id);
    }

    /**
     * Creates a protocol interaction event with the provided type, numeric identifier,
     * and sender and destination protocol numeric identifier
     * The type should be either REQUEST_EVENT or REPLY_EVENT
     * @param type type of the event
     * @param id numeric identifier of the event
     * @param sender numeric identifier of the sender protocol
     * @param destination numeric identifier of the destination protocol
     */
    public ProtocolInteraction(EventType type, short id, short sender, short destination) {
        super(type, id);
        this.senderID = sender;
        this.destinationID = destination;
    }

    /**
     * Returns the numeric identifier of the destination protocol
     * @return the numeric identifier
     */
    public short getDestinationID() {
        return destinationID;
    }

    /**
     * Returns the name of the destination protocol
     * @return the name of the protocol
     */
    public String getDestination() {
        return Babel.getInstance().getProtocolName(destinationID);
    }

    /**
     * Sets the numeric identifier of the protocol to which the event is destined
     * @param destinationID the numeric identifier
     */
    public void setDestination(short destinationID) {
        this.destinationID = destinationID;
    }

    /**
     * Returns the numeric identifier of the sender protocol
     * @return the numeric identifier
     */
    public short getSenderID() {
        return senderID;
    }

    /**
     * Returns the name of the sender protocol
     * @return the name of the protocol
     */
    public String getSender() {
        return Babel.getInstance().getProtocolName(senderID);
    }

    /**
     * Sets the numeric identifier of the protocol that sends the event
     * @param senderID the numeric identifier
     */
    public void setSender(short senderID) {
        this.senderID = senderID;
    }

    /**
     * Sets the sender and destination of the event to be the inverted of the provided
     * protocol interaction event
     *
     * NOTE: do NOT call
     * <pre>
     *     protocolInteractionEvent.invertDestination(protocolInteractionEvent)
     * </pre>
     *
     * Use this method to set the sender and destination protocol numeric identifiers
     * of a Reply Event to a Request Event
     * @see babel.requestreply.ProtocolReply
     * @see babel.requestreply.ProtocolRequest
     *
     * @param protocolInteractionEvent the protocol interaction event to be used as template
     */
    public void invertDestination(ProtocolInteraction protocolInteractionEvent) {
        this.destinationID = protocolInteractionEvent.senderID;
        this.senderID = protocolInteractionEvent.destinationID;
    }

}
