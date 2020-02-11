package babel.generic;

import babel.events.*;
import babel.events.consumers.ChannelConsumer;
import channel.ChannelEvent;
import channel.ChannelListener;
import network.data.Host;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelToProtoForwarder implements ChannelListener<ProtoMessage> {

    final int channelId;
    final Map<Short, ChannelConsumer> consumers;

    public ChannelToProtoForwarder(int channelId) {
        this.channelId = channelId;
        consumers = new ConcurrentHashMap<>();
    }

    public void addConsumer(short protoId, ChannelConsumer consumer) {
        if (consumers.putIfAbsent(protoId, consumer) != null)
            throw new AssertionError("Consumer with protoId " + protoId + " already exists in channel");
    }

    @Override
    public void deliverMessage(ProtoMessage message, Host host) {
        ChannelConsumer channelConsumer = consumers.get(message.destProto);
        if (channelConsumer == null)
            throw new AssertionError("Channel " + channelId + " received message to protoId " +
                    message.destProto + " which is not registered in channel");
        channelConsumer.deliverMessageIn(new MessageInEvent(message, host, channelId));
    }

    @Override
    public void messageSent(ProtoMessage addressedMessage, Host host) {
        consumers.values().forEach(c -> c.deliverMessageSent(new MessageSentEvent(addressedMessage, host, channelId)));
    }

    @Override
    public void messageFailed(ProtoMessage addressedMessage, Host host, Throwable throwable) {
        consumers.values().forEach(c ->
                c.deliverMessageFailed(new MessageFailedEvent(addressedMessage, host, throwable, channelId)));
    }

    @Override
    public void deliverEvent(ChannelEvent channelEvent) {
        consumers.values().forEach(v -> v.deliverChannelEvent(new CustomChannelEvent(channelEvent, channelId)));
    }
}
