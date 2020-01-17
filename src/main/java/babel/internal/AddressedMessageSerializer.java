package babel.internal;

import babel.protocol.ProtoMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressedMessageSerializer implements ISerializer<AddressedMessage> {

    Map<Short, ISerializer<? extends ProtoMessage>> serializers;

    public AddressedMessageSerializer(Map<Short, ISerializer<? extends ProtoMessage>> serializers){
        this.serializers = serializers;
    }

    public void registerProtoSerializer(short msgCode, ISerializer<? extends ProtoMessage> protoSerializer){
        if (serializers.putIfAbsent(msgCode, protoSerializer) != null)
            throw new AssertionError("Trying to re-register serializer in Babel" + msgCode);
    }

    @Override
    public void serialize(AddressedMessage msg, ByteBuf byteBuf) throws IOException {
        byteBuf.writeShort(msg.getSourceProto());
        byteBuf.writeShort(msg.getDestProto());
        short id = msg.getMsg().getId();
        byteBuf.writeShort(id);
        serializers.get(id).serialize(msg.getMsg(), byteBuf);
    }

    @Override
    public AddressedMessage deserialize(ByteBuf byteBuf) throws IOException {
        short source = byteBuf.readShort();
        short dest = byteBuf.readShort();
        short id = byteBuf.readShort();
        return new AddressedMessage(serializers.get(id).deserialize(byteBuf), source, dest);
    }
}