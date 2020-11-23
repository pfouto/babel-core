package pt.unl.fct.di.novasys.babel.initializers;

import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.channel.ChannelListener;
import pt.unl.fct.di.novasys.channel.tcp.MultithreadedTCPChannel;
import pt.unl.fct.di.novasys.network.ISerializer;

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
