package babel.core;

import babel.internal.*;
import channel.ChannelEvent;
import channel.ChannelListener;
import network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelToProtoForwarder implements ChannelListener<BabelMessage> {

    private static final Logger logger = LogManager.getLogger(ChannelToProtoForwarder.class);

    final int channelId;
    final Map<Short, GenericProtocol> consumers;

    public ChannelToProtoForwarder(int channelId) {
        this.channelId = channelId;
        consumers = new ConcurrentHashMap<>();
    }

    public void addConsumer(short protoId, GenericProtocol consumer) {
        if (consumers.putIfAbsent(protoId, consumer) != null)
            throw new AssertionError("Consumer with protoId " + protoId + " already exists in channel");
    }

    @Override
    public void deliverMessage(BabelMessage message, Host host) {
        GenericProtocol channelConsumer;
        if (message.getDestProto() == -1 && consumers.size() == 1)
            channelConsumer = consumers.values().iterator().next();
        else
            channelConsumer = consumers.get(message.getDestProto());

        if (channelConsumer == null) {
            logger.error("Channel " + channelId + " received message to protoId " +
                    message.getDestProto() + " which is not registered in channel");
            throw new AssertionError("Channel " + channelId + " received message to protoId " +
                    message.getDestProto() + " which is not registered in channel");
        }
        channelConsumer.deliverMessageIn(new MessageInEvent(message, host, channelId));
    }

    @Override
    public void messageSent(BabelMessage addressedMessage, Host host) {
        consumers.values().forEach(c -> c.deliverMessageSent(new MessageSentEvent(addressedMessage, host, channelId)));
    }

    @Override
    public void messageFailed(BabelMessage addressedMessage, Host host, Throwable throwable) {
        consumers.values().forEach(c ->
                c.deliverMessageFailed(new MessageFailedEvent(addressedMessage, host, throwable, channelId)));
    }

    @Override
    public void deliverEvent(ChannelEvent channelEvent) {
        consumers.values().forEach(v -> v.deliverChannelEvent(new CustomChannelEvent(channelEvent, channelId)));
    }
}
