package babel.initializers;

import babel.generic.ProtoMessage;
import babel.internal.BabelMessage;
import channel.ChannelListener;
import channel.simpleclientserver.SimpleClientChannel;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class SimpleClientChannelInitializer implements ChannelInitializer<SimpleClientChannel<BabelMessage>> {

    @Override
    public SimpleClientChannel<BabelMessage> initialize(ISerializer<BabelMessage> serializer,
                                                     ChannelListener<BabelMessage> list,
                                                     Properties properties, short protoId) throws UnknownHostException {
        return new SimpleClientChannel<>(serializer, list, properties);
    }
}
