package babel.initializers;

import babel.generic.ProtoMessage;
import channel.ChannelListener;
import channel.tcp.MultithreadedTCPChannel;
import channel.tcp.TCPChannel;
import network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public class MultithreadedTCPChannelInitializer implements ChannelInitializer<MultithreadedTCPChannel<ProtoMessage>> {

    @Override
    public MultithreadedTCPChannel<ProtoMessage> initialize(ISerializer<ProtoMessage> serializer,
                                                            ChannelListener<ProtoMessage> list,
                                                            Properties properties, short protoId) throws IOException {
        return new MultithreadedTCPChannel<>(serializer, list, properties);
    }
}
