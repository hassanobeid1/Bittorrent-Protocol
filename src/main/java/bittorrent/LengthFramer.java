/**
 * Length Framer
 * A message to be transmitted begins with 2 bytes indicating its size
 * 
 * @author Hassan
 */

package bittorrent;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public class LengthFramer implements Framer{

	public static final int MAXMESSAGELENGTH = 65535;
	public static final int BYTEMASK = 0xff;
	public static final int SHORTMASK = 0xffff;
	public static final int BYTESHIFT = 8;
	
	// wrapper for data I/O
	private DataInputStream in; 
	
	public ByteBuffer buffer;
	/**
	 * Constructor
	 * @param in : InputStream
	 * @throws IOException
	 */
	public LengthFramer(InputStream in) throws IOException {
		this.in = new DataInputStream(in);
	}
	
	public LengthFramer(ByteBuffer buffer) {
		this.buffer = buffer;
	}
	/**
	 * Function that build the message to be send
	 * @param message : the initial message to be used to build the new message
	 * @param out : OutputStream
	 * @throws IOException if the message is too long
	 */
	public void frameMsg(byte[] message, OutputStream out) throws IOException {
		 if (message.length > MAXMESSAGELENGTH) {
			 throw new IOException("message too long");
		 }
		 
		 // Message's size
		 int messageSize = 32768;
		 
		 // New message that starts with 2 bytes indicating the message's size
		 byte[] message2 = new byte[message.length + 2];
	     
		 // The 2 bytes below represent the size of the message
		 message2[0] = (byte) ((messageSize >> BYTESHIFT) & BYTEMASK);
		 message2[1] = (byte) (messageSize & BYTEMASK);
		 
		 // Copy the message initial message to the new message
	 	 for(int i = 0; i < message.length; i++)
			message2[i+2] = message[i];
	 	
	 	 
		 // write length prefix
		 // out.write((size >> BYTESHIFT) & BYTEMASK);
		 // out.write(size & BYTEMASK);
		 
		 // write message
		  out.write(message);
		  out.flush();
		}
		 
	public byte[] nioNextMsg() {
		int length;
		
		int position = buffer.position();
		length = buffer.getInt();
		
		byte[] msg = new byte[length+4];
		for (int i = 0; i < length+4; i++)
			msg[i] = buffer.array()[i+position];
		
		buffer.position(length+4+position);
		System.err.println("position " + buffer.position());
		return msg;
	}
	/**
	 * Function to read a received message
	 * @return the received message
	 * @throws IOException if the size of the message != 2
	 */
	public byte[] nextMsg() throws IOException {
		 int length;
		 try {

			 length = in.readUnsignedShort(); // read 2 bytes
		 } 
		 
		 catch (EOFException e) { // no (or 1 byte) message
			 return null;
		 }
		 
		 
		 // 0 <= length <= 65535
		 byte[] msg = new byte[length];
		 in.readFully(msg); // if exception, it's a framing error.
		 return msg;
		 }
	
}
