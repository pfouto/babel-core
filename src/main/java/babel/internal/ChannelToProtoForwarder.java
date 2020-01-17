package babel.internal;

import babel.consumers.ChannelConsumer;
import channel.ChannelEvent;
import channel.ChannelListener;
import network.data.Host;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelToProtoForwarder implements ChannelListener<AddressedMessage> {

    final int channelId;
    final Map<Short, ChannelConsumer> consumers;

    public ChannelToProtoForwarder(int channelId) {
        this.channelId = channelId;
        consumers = new ConcurrentHashMap<>();
    }

    public void addConsumer(short protoId, ChannelConsumer consumer) {
        if (consumers.putIfAbsent(protoId, consumer) != null) {
            throw new AssertionError("Consumer with protoId " + protoId + " already exists in channel");
        }
    }

    @Override
    public void deliverMessage(AddressedMessage addressedMessage, Host host) {
        ChannelConsumer channelConsumer = consumers.get(addressedMessage.getDestProto());
        if (channelConsumer == null) {
            throw new AssertionError("Channel " + channelId + " received message to protoId " +
                    addressedMessage.getDestProto() + " which is not registered in channel");
        }
        channelConsumer.deliverMessage(new MessageEvent(addressedMessage, host, channelId));
    }

    @Override
    public void deliverEvent(ChannelEvent<AddressedMessage> channelEvent) {
        consumers.values().forEach(v -> v.deliverChannelEvent(new ChannelEventEvent(channelEvent, channelId)));
    }
}
