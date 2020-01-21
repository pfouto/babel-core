package babel.initializers;

import babel.internal.AddressedMessage;
import channel.ChannelListener;
import channel.simpleclientserver.SimpleClientChannel;
import channel.simpleclientserver.SimpleServerChannel;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Properties;

public class ServerChannelInitializer implements ChannelInitializer<SimpleServerChannel<AddressedMessage>> {

    @Override
    public SimpleServerChannel<AddressedMessage> initialize(ISerializer<AddressedMessage> serializer,
                                                            ChannelListener<AddressedMessage> list,
                                                            Properties properties) throws UnknownHostException {
        return new SimpleServerChannel<>(serializer, list, properties);
    }
}
