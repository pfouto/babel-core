package babel.initializers;

import babel.internal.BabelMessage;
import channel.ChannelListener;
import channel.IChannel;
import network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public interface ChannelInitializer<T extends IChannel<BabelMessage>> {

    T initialize(ISerializer<BabelMessage> serializer, ChannelListener<BabelMessage> list,
                 Properties properties, short protoId) throws IOException;
}
