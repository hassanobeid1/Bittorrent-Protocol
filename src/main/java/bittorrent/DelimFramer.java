/**
 * Delimiter Framer
 * The message is transmitted with '\n' at the end as a delimiter
 * At the reception, we retrieve the message before the '\n'
 * 
 * @author Hassan
 */
package bittorrent;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DelimFramer {

	private InputStream in; // data source
	private static final byte DELIMITER = '\n'; // message delimiter (0a as byte)
 
	/**
	 * Constructor
	 * @param in : InputStream
	 */
	public DelimFramer(InputStream in) {
		this.in = in;
	}

	/**
	 * Function that build the message to be send
	 * @param message : the initial message to be used to build the new message
	 * @param out : OutputStream
	 * @throws IOException if the byte sequence to be framed contains the delimiter
	 */
	public void frameMsg(byte[] message, OutputStream out) throws IOException {
		
		// ensure that the message does not contain the delimiter
		for (byte b : message) {
			if (b == DELIMITER) {
				throw new IOException("Message contains delimiter");
			}
		}
	  
		// Build the new message containing the delimiter at its end
		byte[] message2 = new byte[message.length + 1];
		int i;
		for(i = 0; i<message.length; i++)
			message2[i] = message[i];
		message2[i] = 10;
	  
		out.write(message);
		//out.write(DELIMITER);
		out.flush();
	}
	 

	/**
	 * Scans a given stream, extracting the next message
	 * If the end of stream occurs before finding the delimiter,
	 * @throws IOException if any bytes have been read since construction 
	 * of the framer or the last delimiter; otherwise return
	 * null to indicate that all messages have been received.
	 * @return the received message
	 */
	public byte[] nextMsg() throws IOException {
		ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
		int nextByte;
	 
		// fetch bytes until find delimiter
		while ((nextByte = in.read()) != DELIMITER) {
			if (nextByte == -1) { // end of stream?
			  
				// if we did not read any byte (stream is empty)
				if (messageBuffer.size() == 0)  
					return null;
			  
				// if bytes followed by end of stream: framing error
				else 
					throw new EOFException("Non-empty message without delimiter");
				}	
			messageBuffer.write(nextByte); // write byte to buffer
		}
	  
		return messageBuffer.toByteArray();
	}
	
}
