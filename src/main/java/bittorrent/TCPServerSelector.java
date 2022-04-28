package bittorrent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/*
* SELECTOR : main managing the different cannals, 
* AKA the exchanges around the torrent to
*/
public class TCPServerSelector {
  public static final int BUFSIZE = 66000;  /// Buffer size (bytes)
  private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
  public static Torrent to ;
  public static byte[][] piecesPerChannel; 
  public static boolean[] havingPieces; 
  public static int[] pointorBlockPerChannel; 
  public static Peer[]  peers ;  ;     	//pairs that have exchanged the file
  public static FileManager fileManager ; 
  public static Bitfield personnalBitfield; 
  public static LengthFramer lf;
  public TCPServerSelector(Torrent to) {
	  this.to = to; 
	  System.out.println("il y a " + to.getNumberOfPieces() +"pieces");
  }
  
  
  public void startSelector() throws IOException {
	fileManager = new FileManager(to);	
	//initialisation du bitfield : lecture du fichier file de FileManager ! 
	personnalBitfield = fileManager.getBitfieldUponFile(); 
	int[] test = personnalBitfield.getCorrespondingIntArray(); 
	for (int l=0; l< to.getNumberOfPieces(); l++ ) {
		System.out.println("bitfield["+l+"] = "+ test[l]);
	}
	///tracker
	String trackerAdress = "127.0.0.1";  
	int trackerPort = 6969; 
	String trackerId = "-BE0001-n%9A%B4%40%2Cb..zq%5D%9D";
	///Client
	String clientAdress = "127.0.0.1"; 
	int clientPort = 61245;
	/// Constructing the path of the file to create
	Path currentPath = Paths.get(".");
	Path fullPath = currentPath.toAbsolutePath();
	
	// Create a selector to multiplex listening sockets and connections
    Selector selector = Selector.open();
	System.out.println("openning selector \n"
	+"for selecting wich peer to ask for the " 
	+ TCPServerSelector.getTorrent().getNumberOfPieces() + " pieces");
    /// Getting peers from a sendGETAnnounce with opentracker
    Connection con2 = new Connection(clientAdress, clientPort,trackerAdress, trackerPort, trackerId);
	try {
		con2.sendGETAnnounce(getTorrent().getInfo_hash(), clientPort, 0, 0, getTorrent().getFileLength(), 60);
	} catch (Exception e) { e.printStackTrace();} //clientPort is were we wait for connections
	peers = con2.getPeers(); /// list of peers (except tracker) IP, port    TODO fix 
    // Create listening socket channel for each port having the file and register to selector

	//peers.add(new Peer(InetAddress.getLocalHost(), 54545));

	int i = 0; 
	while (peers[i] != null && i< Connection.NB_PEERS_MAX) {
		 Peer peer = peers[i]; 
    	 System.out.println("pair " + i + " : port OP_CONNECT : " + peer.getPort());
    	 InetSocketAddress socketAddress = new InetSocketAddress(peer.getAdress(), peer.getPort());
    	 SocketChannel channel = SocketChannel.open();
    	 channel.configureBlocking(false);
         channel.connect(socketAddress);
         channel.register(selector, SelectionKey.OP_CONNECT); /// Register selector with channel.    	 
         System.out.println( channel.toString() + "connected & registered " ); 
         i++; 
    } 
	
	//register the passive canal
	ServerSocketChannel listnChannel = ServerSocketChannel.open();
    listnChannel.socket().bind(new InetSocketAddress(clientPort));
    listnChannel.configureBlocking(false);
    listnChannel.register(selector, SelectionKey.OP_ACCEPT);
	

    // Create a protocol
    TCPProtocol protocol = new EchoSelectorProtocol(BUFSIZE);
    ///start handling our different sockets
    int j = 0;
    while (j<150) { // Run forever, processing available I/O operations
      // Wait for some channel to be ready (or timeout)
      if (selector.select(TIMEOUT) == 0) { // returns # of ready channels
        System.out.print(".");
        j++; 
        continue;
      }
      j=0; 
      // Get iterator on set of keys with I/O to process
      Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
      while (keyIter.hasNext()) {
        SelectionKey key = keyIter.next(); // Key is bit mask
        if (key.isAcceptable()) { ///the canal is the passive one : 
          System.out.println("handling accept");
          protocol.handleAccept(key);
        }
        if (key.isValid() && key.isReadable()) {/// Client socket channel has pending data?
          System.out.println("handling read");  
          protocol.handleRead(key);
        }

        
        if (key.isValid() && key.isWritable()) {/// Client socket channel is available for writing and
            System.out.println("handling write");
        	protocol.handleWrite(key);// and the key is valid (i.e., channel not closed)
        }
        if(key.isValid() && key.isConnectable()) {// en phase de connection
            System.out.println("handling connect");
        	protocol.handleConnect(key); 
        }
      }
      keyIter.remove(); /// remove from set of selected keys : NECESSARY?? 
    }
    System.out.println("more than 150 iterations done. closing selector"); 
    selector.close(); // closing everything 
  }

public static Torrent getTorrent() {
	return to;
}
}
