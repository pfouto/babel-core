package babel.requestreply;

/**
 * An interface of a request event consumer
 *
 *
 * @see babel.protocol.GenericProtocol
 */
public interface IReplyConsumer {

    /**
     * Deliver the reply event to the correct protocol
     *
     * Can be implemented differently by applications
     *
     * @param reply the reply to be delivered
     */
    void deliverReply(ProtocolReply reply);
}
