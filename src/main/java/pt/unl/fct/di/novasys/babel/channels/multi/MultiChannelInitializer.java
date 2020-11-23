package pt.unl.fct.di.novasys.babel.channels.multi;

import pt.unl.fct.di.novasys.babel.initializers.ChannelInitializer;
import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.channel.ChannelListener;
import pt.unl.fct.di.novasys.channel.IChannel;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public class MultiChannelInitializer implements ChannelInitializer<IChannel<BabelMessage>> {
    @Override
    public MultiChannel initialize(ISerializer<BabelMessage> serializer, ChannelListener<BabelMessage> list,
                                   Properties properties, short protoId) throws IOException {
        return MultiChannel.getInstance(serializer, list, protoId, properties);
    }
}
