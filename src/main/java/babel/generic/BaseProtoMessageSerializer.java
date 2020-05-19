package babel.generic;

import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.io.IOException;
import java.util.Map;

public class BaseProtoMessageSerializer implements ISerializer<ProtoMessage> {

    Map<Short, ISerializer<? extends ProtoMessage>> serializers;

    public BaseProtoMessageSerializer(Map<Short, ISerializer<? extends ProtoMessage>> serializers) {
        this.serializers = serializers;
    }

    public void registerProtoSerializer(short msgCode, ISerializer<? extends ProtoMessage> protoSerializer) {
        if (serializers.putIfAbsent(msgCode, protoSerializer) != null)
            throw new AssertionError("Trying to re-register serializer in Babel: " + msgCode);
    }

    @Override
    public void serialize(ProtoMessage msg, ByteBuf byteBuf) throws IOException {
        byteBuf.writeShort(msg.sourceProto);
        byteBuf.writeShort(msg.destProto);
        byteBuf.writeShort(msg.getId());
        ISerializer iSerializer = serializers.get(msg.getId());
        iSerializer.serialize(msg, byteBuf);
    }

    @Override
    public ProtoMessage deserialize(ByteBuf byteBuf) throws IOException {
        short source = byteBuf.readShort();
        short dest = byteBuf.readShort();
        short id = byteBuf.readShort();
        ProtoMessage deserialize = serializers.get(id).deserialize(byteBuf);
        deserialize.destProto = dest;
        deserialize.sourceProto = source;
        return deserialize;
    }
}