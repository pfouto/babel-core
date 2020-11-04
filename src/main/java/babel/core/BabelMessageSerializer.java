package babel.core;

import babel.generic.ProtoMessage;
import babel.internal.BabelMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.io.IOException;
import java.util.Map;

public class BabelMessageSerializer implements ISerializer<BabelMessage> {

    Map<Short, ISerializer<? extends ProtoMessage>> serializers;

    public BabelMessageSerializer(Map<Short, ISerializer<? extends ProtoMessage>> serializers) {
        this.serializers = serializers;
    }

    public void registerProtoSerializer(short msgCode, ISerializer<? extends ProtoMessage> protoSerializer) {
        if (serializers.putIfAbsent(msgCode, protoSerializer) != null)
            throw new AssertionError("Trying to re-register serializer in Babel: " + msgCode);
    }

    @Override
    public void serialize(BabelMessage msg, ByteBuf byteBuf) throws IOException {
        byteBuf.writeShort(msg.getSourceProto());
        byteBuf.writeShort(msg.getDestProto());
        byteBuf.writeShort(msg.getMessage().getId());
        ISerializer iSerializer = serializers.get(msg.getMessage().getId());
        if(iSerializer == null){
            throw new AssertionError("No serializer found for message id " + msg.getMessage().getId());
        }
        iSerializer.serialize(msg.getMessage(), byteBuf);
    }

    @Override
    public BabelMessage deserialize(ByteBuf byteBuf) throws IOException {
        short source = byteBuf.readShort();
        short dest = byteBuf.readShort();
        short id = byteBuf.readShort();
        ISerializer<? extends ProtoMessage> iSerializer = serializers.get(id);
        if(iSerializer == null){
            throw new AssertionError("No deserializer found for message id " + id);
        }
        ProtoMessage deserialize = iSerializer.deserialize(byteBuf);
        return new BabelMessage(deserialize, source, dest);
    }
}