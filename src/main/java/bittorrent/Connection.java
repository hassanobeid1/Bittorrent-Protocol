package bittorrent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import be.adaxisoft.bencode.BDecoder;
import be.adaxisoft.bencode.BEncodedValue;

/**
 * 
 * @author jade
 *	Connection allows to create a connection between the client and its peer 
 */
public class Connection {
	public static int NB_PEERS_MAX = 10; 
	public static  int nb_peers = 0 ; 
	private String ip_client;
	private int port_client; //TO DO
	private String ip_peer; 
	private int port_peer; 
	private String peer_id; 

	private  Peer[] peers = new Peer[NB_PEERS_MAX];     	//pairs that have exchanged the file
	
	public Peer[] getPeers() {
		return peers;
	}

	public void setPeers(Peer[] peers) {
		this.peers = peers;
	}
	
	public  void addPeer(Peer p) throws IndexOutOfBoundsException {
		if(nb_peers< NB_PEERS_MAX) {
			peers[nb_peers] = p; 
			nb_peers++; 
		}
		else {
		 throw new IndexOutOfBoundsException("NB_PEERS_MAX atteinds ! ");  
		}
	}

	
	
	public Connection(String ip_client, int port_client, String ip_peer, int port_peer, String peer_id) {
		super();
		this.ip_client = ip_client;
		this.port_client = port_client;
		this.ip_peer = ip_peer;
		this.port_peer = port_peer;
		this.peer_id = peer_id;
	}

	/*
	 * request seeder --> tracker : info_hash + peer_id + port 
	 * request client --> tracker : info_hash + port_listenning + uploaded, downloaded, lef + nb_peers_wanted
	 * */
	public void sendGETAnnounce(byte[] info_hash, int port_listening, int uploaded, int downloaded, int left, int nb_peers_wanted) throws Exception  {
		int compact =1 ; 
		String event_type = "started"; 
		//info_hash and peer_id must be properly escaped
		String hash_encoded = Utils.byteArrayToURLString(Utils.hexStringToByteArray(Utils.bytesToHex(info_hash))); 
		String http_request = "http://"
				+ip_peer +":" + port_peer
				+"/announce?" 
				+"info_hash="+ hash_encoded
				+"&peer_id="+peer_id
				+"&supportcrypto=1"
				+"&port="+ port_listening
				+"&azudp="+port_listening //port number on witch the client is listening on == for leechers
				+"&uploaded="+uploaded //total amount uploaded in base ten ASCII
				+"&downloaded="+downloaded //total amount downloaded
				+"&left=" +left//The number of bytes this client still has to download in base ten ASCII.
				+"&numwant="+nb_peers_wanted
				+"&compact="+compact
				+"&event=" + event_type; 
		
		 URL url_encoded = new URL(http_request);
		 System.out.println("a request is sent to the tracker (@ 6969)" );
		 System.out.println(http_request); 
		 /* Ouvre une connection avec l'object URL */
		 HttpURLConnection connection = (HttpURLConnection) url_encoded.openConnection();
		    
		 //Methode GET
		 connection.setRequestMethod("GET");
		  
		//Response from the connection : 
		InputStream in = connection.getInputStream();
		byte[] receiveBuf = new byte[200];
        int recvMsgSize;   // Size of received message
        recvMsgSize = in.read(receiveBuf); //saving the response (of size recvMsgSize) in this buffer 
        
        if(recvMsgSize == -1) throw new Exception("TRACKER NOT RESPONDING");
        System.out.println("recvMsgSize" + recvMsgSize);
        byte[] body = null; 
        
        body = Arrays.copyOf(receiveBuf, recvMsgSize); //shaping the buffer to its size
        System.out.println("body = " + Utils.bytesToHex(body));
 		System.out.println("body ISO_8859_1 = " + new String(body, "ISO_8859_1"));
		
 		ByteArrayInputStream bais = new ByteArrayInputStream(body);
 		BDecoder readerRes = new BDecoder(bais); //decoding the body
        Map<String, BEncodedValue> doc = readerRes.decodeMap().getMap(); //mapping it : String / Value
        
        //list of peers that are currently transferring the file
        byte [] peersbyte = doc.get("peers").getBytes();// byteArray holding peers 
        System.out.println("peersbyte "+ Utils.bytesToHex(peersbyte)); 	    
        System.out.println("");

        int peer_number =1; 
        //1 : tracker
        byte[] adress1byte = Arrays.copyOfRange(peersbyte, 0, 4);
        InetAddress adress1 = InetAddress.getByAddress(adress1byte); 
        
        byte[] port1byte = Arrays.copyOfRange(peersbyte,4,6); 
        int port1 = Utils.from2ByteArray(port1byte);
        
        System.out.println("port1 = "+ port1  ); 	    
        while((peersbyte.length/6)>peer_number){
	        peer_number ++; 
	        //2 : 
	        byte[] adress2byte = Arrays.copyOfRange(peersbyte,6,10);
	        InetAddress adress2 = InetAddress.getByAddress(adress2byte); 
	        byte[] port2byte = Arrays.copyOfRange(peersbyte,10,12); 
	        int port2= Utils.from2ByteArray(port2byte);
	        System.out.println("port1 = "+ port1 + "  port2 = "+ port2 ); 	    
	        System.out.println("");
	        peers[peer_number-2] = new Peer(adress2, port2); // real seeder : vuze port
	        nb_peers ++; 
        }
        System.out.println("open tracker told us about " + nb_peers + " peers");
	}	
}