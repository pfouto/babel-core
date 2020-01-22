package babel.runtime.initializers;

import babel.runtime.AddressedMessage;
import channel.ChannelListener;
import channel.ackos.AckosChannel;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class AckosChannelInitializer implements ChannelInitializer<AckosChannel<AddressedMessage>> {

    @Override
    public AckosChannel<AddressedMessage> initialize(ISerializer<AddressedMessage> serializer,
                                                     ChannelListener<AddressedMessage> list,
                                                     Properties properties) throws UnknownHostException {
        return new AckosChannel<>(serializer, list, properties);
    }
}
