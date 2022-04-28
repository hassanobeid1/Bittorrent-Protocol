/**
 * Interface of Framer to be implemented in the classes DelimFramer and LengthFramer
 * 
 * @author Hassan 
 */
package bittorrent;

import java.io.IOException;
import java.io.OutputStream;

public interface Framer {
	
	void frameMsg(byte[] message, OutputStream out) throws IOException;
	
	byte[] nextMsg() throws IOException;
}