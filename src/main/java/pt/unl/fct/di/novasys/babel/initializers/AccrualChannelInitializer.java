package pt.unl.fct.di.novasys.babel.initializers;

import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.channel.ChannelListener;
import pt.unl.fct.di.novasys.channel.accrual.AccrualChannel;
import pt.unl.fct.di.novasys.channel.ackos.AckosChannel;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

public class AccrualChannelInitializer implements ChannelInitializer<AccrualChannel<BabelMessage>> {

    @Override
    public AccrualChannel<BabelMessage> initialize(ISerializer<BabelMessage> serializer,
                                                 ChannelListener<BabelMessage> list,
                                                 Properties properties, short protoId) throws IOException {
        return new AccrualChannel<>(serializer, list, properties);
    }
}
