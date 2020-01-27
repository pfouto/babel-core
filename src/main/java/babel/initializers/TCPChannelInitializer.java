package babel.initializers;

import babel.generic.ProtoMessage;
import channel.ChannelListener;
import channel.tcp.TCPChannel;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class TCPChannelInitializer implements ChannelInitializer<TCPChannel<ProtoMessage>> {

    @Override
    public TCPChannel<ProtoMessage> initialize(ISerializer<ProtoMessage> serializer,
                                                     ChannelListener<ProtoMessage> list,
                                                     Properties properties) throws UnknownHostException {
        return new TCPChannel<>(serializer, list, properties);
    }
}
