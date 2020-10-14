package babel.initializers;

import babel.generic.ProtoMessage;
import babel.internal.BabelMessage;
import channel.ChannelListener;
import channel.ackos.AckosChannel;
import network.ISerializer;

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
