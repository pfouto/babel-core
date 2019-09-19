package babel.requestreply;

/**
 * An interface of a request event consumer
 *
 *
 * Implemented by:
 * @see babel.protocol.GenericProtocol
 *
 * Used in:
 * @see babel.Babel
 */
public interface IRequestConsumer {

    void deliverRequest(ProtocolRequest r);
}
