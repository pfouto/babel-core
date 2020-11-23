package pt.unl.fct.di.novasys.babel.initializers;

import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.channel.ChannelListener;
import pt.unl.fct.di.novasys.channel.tcp.TCPChannel;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public class TCPChannelInitializer implements ChannelInitializer<TCPChannel<BabelMessage>> {

    @Override
    public TCPChannel<BabelMessage> initialize(ISerializer<BabelMessage> serializer,
                                                     ChannelListener<BabelMessage> list,
                                                     Properties properties, short protoId) throws IOException {
        return new TCPChannel<>(serializer, list, properties);
    }
}
