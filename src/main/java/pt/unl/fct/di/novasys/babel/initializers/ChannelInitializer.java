package pt.unl.fct.di.novasys.babel.initializers;

import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.channel.ChannelListener;
import pt.unl.fct.di.novasys.channel.IChannel;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public interface ChannelInitializer<T extends IChannel<BabelMessage>> {

    T initialize(ISerializer<BabelMessage> serializer, ChannelListener<BabelMessage> list,
                 Properties properties, short protoId) throws IOException;
}
