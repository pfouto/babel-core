package babel.initializers;

import babel.internal.AddressedMessage;
import babel.internal.MessageEvent;
import channel.ChannelListener;
import channel.IChannel;
import network.ISerializer;

import java.io.IOException;
import java.util.Map;

public interface ChannelInitializer<T extends IChannel<AddressedMessage>> {

    T initialize(ISerializer<AddressedMessage> serializer, ChannelListener<AddressedMessage> list,
                 Map<String, String> arguments) throws IOException;
}
