package babel.channels.multi;

import babel.generic.ProtoMessage;
import babel.initializers.ChannelInitializer;
import channel.ChannelListener;
import channel.IChannel;
import network.ISerializer;

import java.io.IOException;
import java.util.Properties;

public class MultiChannelInitializer implements ChannelInitializer {
    @Override
    public IChannel<ProtoMessage> initialize(ISerializer serializer, ChannelListener list, Properties properties) throws IOException {
        return MultiChannel.getInstance(serializer, list, protoId, properties);
    }
}
