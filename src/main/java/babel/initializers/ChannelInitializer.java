package babel.initializers;

import babel.generic.ProtoMessage;
import channel.ChannelListener;
import channel.IChannel;
import network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public interface ChannelInitializer<T extends IChannel<ProtoMessage>> {

    T initialize(ISerializer<ProtoMessage> serializer, ChannelListener<ProtoMessage> list,
                 Properties properties, short protoId) throws IOException;
}
