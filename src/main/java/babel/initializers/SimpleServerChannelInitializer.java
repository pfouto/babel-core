package babel.initializers;

import babel.generic.ProtoMessage;
import babel.internal.BabelMessage;
import channel.ChannelListener;
import channel.simpleclientserver.SimpleServerChannel;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class SimpleServerChannelInitializer implements ChannelInitializer<SimpleServerChannel<BabelMessage>> {

    @Override
    public SimpleServerChannel<BabelMessage> initialize(ISerializer<BabelMessage> serializer,
                                                        ChannelListener<BabelMessage> list,
                                                        Properties properties, short protoId) throws UnknownHostException {
        return new SimpleServerChannel<>(serializer, list, properties);
    }
}
