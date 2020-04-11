package babel.initializers;

import babel.generic.ProtoMessage;
import channel.ChannelListener;
import channel.ackos.AckosChannel;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class AckosChannelInitializer implements ChannelInitializer<AckosChannel<ProtoMessage>> {

    @Override
    public AckosChannel<ProtoMessage> initialize(ISerializer<ProtoMessage> serializer,
                                                     ChannelListener<ProtoMessage> list,
                                                     Properties properties, short protoId) throws UnknownHostException {
        return new AckosChannel<>(serializer, list, properties);
    }
}
