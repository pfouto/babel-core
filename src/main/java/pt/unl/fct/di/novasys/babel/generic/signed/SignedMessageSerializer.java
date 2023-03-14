package pt.unl.fct.di.novasys.babel.generic.signed;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.novasys.network.ISerializer;

/**
 * This serializer is used to serialize/deserialize SignedProtoMessage objects.
 * Defines a serialization format just like ISerializer.
 *
 * @param <T> The type of SignedProtoMessage to be serialized/deserialized
 */
public abstract class SignedMessageSerializer<T extends SignedProtoMessage> implements ISerializer<T> {

	@Override
	public void serialize(T msg, ByteBuf out) throws IOException {
        if(msg.serializedMessage == null || msg.serializedMessage.length == 0) {
        	ByteBuf serial = Unpooled.buffer();
        	serial.writeShort(msg.getId());
        	this.serializeBody(msg, serial);
        	msg.serializedMessage = ByteBufUtil.getBytes(serial.slice());
        }
    	
    	out.writeInt(msg.serializedMessage.length);
        out.writeBytes(msg.serializedMessage);
        if(msg.signature == null || msg.signature.length == 0)
        	out.writeShort(0);
        else {
        	out.writeShort(msg.signature.length);
        	out.writeBytes(msg.signature);
        }
    }


	@Override
	public T deserialize(ByteBuf in) throws IOException {
		int msgBodyLen = in.readInt();
		
		byte[] msgData = new byte[msgBodyLen];
		in.readBytes(msgData);
		T msg = deserializeBody(Unpooled.wrappedBuffer(msgData, Short.BYTES, msgBodyLen - Short.BYTES));
		msg.serializedMessage = msgData; 
		
		short signatureSize = in.readShort();
		if(signatureSize > 0) {
			msg.signature = new byte[signatureSize];
			in.readBytes(msg.signature);
		} else {
			msg.signature = null;
		}
		
		return msg;
	}
	
	public abstract void serializeBody(T signedProtoMessage, ByteBuf out) throws IOException;
	
	public abstract T deserializeBody(ByteBuf in) throws IOException;
	
}