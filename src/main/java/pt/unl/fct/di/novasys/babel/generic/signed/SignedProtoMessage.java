package pt.unl.fct.di.novasys.babel.generic.signed;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;

public abstract class SignedProtoMessage extends ProtoMessage {

	protected byte[] serializedMessage;
	protected byte[] signature;
	
	public SignedProtoMessage(short id) {
		super(id);
		this.serializedMessage = null;
		this.signature = null;
	}
	
	public final void signMessage(PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidSerializerException {
		if(this.serializedMessage == null) {
			SignedMessageSerializer<? extends SignedProtoMessage> serializer = this.getSerializer();
			if(serializer == null) {
				throw new InvalidSerializerException("No Serializer available for type: " + this.getClass().getCanonicalName() +
						"\nVerify that the serializer exists and is returned by the method getSerializer()");
			} else {
				ByteBuf b = Unpooled.buffer();
				serializer.serializeBody(this, b);
				this.serializedMessage = ByteBufUtil.getBytes(b);
			}
		}
		
		Signature sig = Signature.getInstance("SHA256WithRSA");
		sig.initSign(key);
		sig.update(serializedMessage);
		this.signature = sig.sign();
	}
	
	public final boolean checkSignature(PublicKey key) throws InvalidFormatException, NoSignaturePresentException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		if(this.serializedMessage == null || this.serializedMessage.length == 0)
			throw new InvalidFormatException("Message serialization format is not present. Was this message received from the network?");
		if(this.signature == null || this.signature.length == 0)
			throw new NoSignaturePresentException("This message does not contain a signature. Was this message received from the network?");
		
		Signature sig = Signature.getInstance("SHA256WithRSA");
		sig.initVerify(key);
		sig.update(this.serializedMessage);
		return sig.verify(this.signature);
	}
	
	public abstract SignedMessageSerializer<? extends SignedProtoMessage> getSerializer();

}
