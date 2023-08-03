package pt.unl.fct.di.novasys.babel.initializers;

import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.channel.ChannelListener;
import pt.unl.fct.di.novasys.channel.tcp.SharedTCPChannel;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public class SharedTCPChannelInitializer implements ChannelInitializer<SharedTCPChannel<BabelMessage>> {

    @Override
    public SharedTCPChannel<BabelMessage> initialize(ISerializer<BabelMessage> iSerializer, ChannelListener<BabelMessage> channelListener,
                                                     Properties properties, short protoId) throws IOException {
        return new SharedTCPChannel<>(iSerializer, channelListener, properties);
    }
}
