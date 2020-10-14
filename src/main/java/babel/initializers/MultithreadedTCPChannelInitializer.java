package babel.initializers;

import babel.generic.ProtoMessage;
import babel.internal.BabelMessage;
import channel.ChannelListener;
import channel.tcp.MultithreadedTCPChannel;
import channel.tcp.TCPChannel;
import network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public class MultithreadedTCPChannelInitializer implements ChannelInitializer<MultithreadedTCPChannel<BabelMessage>> {

    @Override
    public MultithreadedTCPChannel<BabelMessage> initialize(ISerializer<BabelMessage> serializer,
                                                            ChannelListener<BabelMessage> list,
                                                            Properties properties, short protoId) throws IOException {
        return new MultithreadedTCPChannel<>(serializer, list, properties);
    }
}
