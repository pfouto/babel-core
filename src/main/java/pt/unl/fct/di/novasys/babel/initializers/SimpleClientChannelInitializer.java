package pt.unl.fct.di.novasys.babel.initializers;

import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.channel.ChannelListener;
import pt.unl.fct.di.novasys.channel.simpleclientserver.SimpleClientChannel;
import pt.unl.fct.di.novasys.network.ISerializer;

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
