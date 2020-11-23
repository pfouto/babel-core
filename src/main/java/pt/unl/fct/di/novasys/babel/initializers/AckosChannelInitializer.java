package pt.unl.fct.di.novasys.babel.initializers;

import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.channel.ChannelListener;
import pt.unl.fct.di.novasys.channel.ackos.AckosChannel;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class AckosChannelInitializer implements ChannelInitializer<AckosChannel<BabelMessage>> {

    @Override
    public AckosChannel<BabelMessage> initialize(ISerializer<BabelMessage> serializer,
                                                 ChannelListener<BabelMessage> list,
                                                 Properties properties, short protoId) throws UnknownHostException {
        return new AckosChannel<>(serializer, list, properties);
    }
}
