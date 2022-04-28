package bittorrent;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;


/// Description of the protocol we aim to follow for each type of key
public class EchoSelectorProtocol implements TCPProtocol {
  private int bufSize; // Size of I/O buffer
  public EchoSelectorProtocol(int bufSize) {
    this.bufSize = bufSize; 
  }


  public void handleConnect(SelectionKey key) {
	//get the SocketChannel :
	    SocketChannel clntChan = (SocketChannel) key.channel();
	    boolean unfinished = false; 
	    try {
	    	unfinished = !clntChan.finishConnect(); 
		} catch (java.net.ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			if(!unfinished) {
			    key.interestOps(SelectionKey.OP_WRITE);
			}//else wait 
  }

// Reading the buffer from the canal and putting it back on In AND Out
//as long as the client write, we read
  public void handleRead(SelectionKey key) throws IOException {
    // Client socket channel has pending data
    SocketChannel clntChan = (SocketChannel) key.channel();
    //System.out.println(key.attachment());
    ByteBuffer buf = (ByteBuffer) key.attachment(); 
    //System.out.println("buf == " + buf);
    long bytesRead = clntChan.read(buf); ///size of bytes writen
    //System.out.println("bytesRead == " + bytesRead); 
    if (bytesRead == -1) { // other end closed the SocketChannel
      System.out.println("closing the clntChan : nothing read.. " );
      clntChan.close();
    } else if (bytesRead > 0) { ///the socket isn't closed and we received something
      /// sending the adapted response with state machine
      BittorrentMessages.nonBlockingCheckMessage(buf.array(), bytesRead,  clntChan); 
      key.interestOps(SelectionKey.OP_READ); ///continue to read 
      buf.flip(); 
      //System.out.println("flip"); //refactoring the buffer
      buf.clear();
      //System.out.println("clear");
    }
  }
  public void handleWrite(SelectionKey key) throws IOException {
	  /*
	     * Channel is available for writing, and key is valid (i.e., client channel
	     * not closed).
	     */
		    SocketChannel clntChan = (SocketChannel) key.channel();
		    System.out.println("création d'un buffer de taille " + bufSize);
		    clntChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));
		    System.out.println("key : " + key.toString());
		    Peer peer = BittorrentMessages.findPeerBySocketChannel(clntChan);
			///we need to start the bittorrent exchange with an handshake
			try {
				System.out.println("nonBlockingSendHandshake");
				if (!peer.peer_handshaked)
					BittorentSeedingConnection.nonBlockingSendHandshake(TCPServerSelector.getTorrent().getInfo_hash_hex(), clntChan );
				peer.peer_handshaked = true;
				///now this connection started : we can start waiting for a response, 
				///that we will analyse with our state machine	
				key.interestOps(SelectionKey.OP_READ);
			} catch (IOException e) {
				e.printStackTrace();
			} 
			  }
	  

  
  //TO DO : SEEDING
  /*
  * Accepting a connection request on the passive canal
  * to seed  data to the requesting peer
  -> saving the new canal on the selectors key
  */
public void handleAccept(SelectionKey key) {
	try {
		SocketChannel clntChan = ((ServerSocketChannel)key.channel()).accept();
		clntChan.configureBlocking(false);
		clntChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));
		//Connection.addPeer(new Peer(clntChan.socket().getInetAddress(), clntChan.socket().getPort())); 
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}



}
