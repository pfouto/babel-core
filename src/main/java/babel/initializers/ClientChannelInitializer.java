package babel.initializers;

import babel.internal.AddressedMessage;
import channel.ChannelListener;
import channel.ackos.AckosChannel;
import channel.simpleclientserver.SimpleClientChannel;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class ClientChannelInitializer implements ChannelInitializer<SimpleClientChannel<AddressedMessage>> {

    @Override
    public SimpleClientChannel<AddressedMessage> initialize(ISerializer<AddressedMessage> serializer,
                                                     ChannelListener<AddressedMessage> list,
                                                     Properties properties) throws UnknownHostException {
        return new SimpleClientChannel<>(serializer, list, properties);
    }
}
