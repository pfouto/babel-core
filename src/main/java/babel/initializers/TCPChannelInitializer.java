package babel.initializers;

import babel.generic.ProtoMessage;
import babel.internal.BabelMessage;
import channel.ChannelListener;
import channel.tcp.TCPChannel;
import network.ISerializer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

public class TCPChannelInitializer implements ChannelInitializer<TCPChannel<BabelMessage>> {

    @Override
    public TCPChannel<BabelMessage> initialize(ISerializer<BabelMessage> serializer,
                                                     ChannelListener<BabelMessage> list,
                                                     Properties properties, short protoId) throws IOException {
        return new TCPChannel<>(serializer, list, properties);
    }
}
