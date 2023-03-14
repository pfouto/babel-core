package pt.unl.fct.di.novasys.babel.generic.signed;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;

/**
 * <p>Abstract Message class to be extended by protocol-specific messages, similar to ProtoMessage.
 * Used just like ProtoMessage, but requires implementing the getSerializer() method.
 *
 * <p>This class also provides methods to sign and check the signature of the message, by calling the signMessage() and
 * checkSignature() methods.
 */
public abstract class SignedProtoMessage extends ProtoMessage {

	private static final String SignatureAlgorithm = "SHA256withRSA";
	
	private static final Logger logger = LogManager.getLogger(SignedProtoMessage.class);
	
	protected byte[] serializedMessage;
	protected byte[] signature;
	
	public SignedProtoMessage(short id) {
		super(id);
		this.serializedMessage = null;
		this.signature = null;
	}
	
	public final void signMessage(PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidSerializerException {
		if(this.serializedMessage == null) {
			SignedMessageSerializer<SignedProtoMessage> serializer = (SignedMessageSerializer<SignedProtoMessage>) this.getSerializer();
			if(serializer == null) {
				throw new InvalidSerializerException("No Serializer available for type: " + this.getClass().getCanonicalName() +
						"\nVerify that the serializer exists and is returned by the method getSerializer()");
			} else {
				ByteBuf b = Unpooled.buffer();
				b.writeShort(this.getId());
				try {
					serializer.serializeBody(this, b);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				this.serializedMessage = ByteBufUtil.getBytes(b.slice());
			}
		}
		
		Signature sig = Signature.getInstance(SignedProtoMessage.SignatureAlgorithm);
		sig.initSign(key);
		sig.update(serializedMessage);
		this.signature = sig.sign();
	}
	
	public final boolean checkSignature(PublicKey key) throws InvalidFormatException, NoSignaturePresentException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		if(this.serializedMessage == null || this.serializedMessage.length == 0)
			throw new InvalidFormatException("Message serialization format is not present. Was this message received from the network?");
		if(this.signature == null || this.signature.length == 0)
			throw new NoSignaturePresentException("This message does not contain a signature. Was this message received from the network?");
		
		Signature sig = Signature.getInstance(SignedProtoMessage.SignatureAlgorithm);
		sig.initVerify(key);
		sig.update(this.serializedMessage);
		boolean valid = sig.verify(this.signature);
			logger.debug("Invalid signature on message: <" + this.getClass().getCanonicalName() + "> :: " + this.toString());
		return valid;
	}
	
	public abstract SignedMessageSerializer<? extends SignedProtoMessage> getSerializer();

}
