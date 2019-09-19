package babel.requestreply;

/**
 * An interface of a reply event consumer
 *
 *
 * Implemented by:
 * @see babel.protocol.GenericProtocol
 *
 * Used in:
 * @see babel.Babel
 */
public interface IReplyConsumer {

    void deliverReply(ProtocolReply r);
}
