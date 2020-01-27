package babel.initializers;

import babel.generic.ProtoMessage;
import channel.ChannelListener;
import channel.simpleclientserver.SimpleClientChannel;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class ClientChannelInitializer implements ChannelInitializer<SimpleClientChannel<ProtoMessage>> {

    @Override
    public SimpleClientChannel<ProtoMessage> initialize(ISerializer<ProtoMessage> serializer,
                                                     ChannelListener<ProtoMessage> list,
                                                     Properties properties) throws UnknownHostException {
        return new SimpleClientChannel<>(serializer, list, properties);
    }
}
