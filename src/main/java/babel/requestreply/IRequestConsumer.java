package babel.requestreply;

/**
 * An interface of a request event consumer
 *
 *
 * @see babel.protocol.GenericProtocol
 */
public interface IRequestConsumer {

    /**
     * Deliver the request event to the correct protocol
     *
     * Can be implemented differently by applications
     *
     * @param request the request to be delivered
     */
    void deliverRequest(ProtocolRequest request);
}
