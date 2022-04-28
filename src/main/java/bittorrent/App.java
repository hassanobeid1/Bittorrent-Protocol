package bittorrent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Main class
 *
 */
public class App 
{

	public static InputStream in;
	public static OutputStream out;
	public static LengthFramer lf;
	public static Torrent to;
	
	
    public static void main( String[] args )throws Exception
    {
    	// Server name or IP address (Vuze) 
    	String server = "127.0.0.1"; 		 
    	int servPort = 55218;     	// Server port(Vuze) 
    	//Tracker
    	String trackerAdress = "127.0.0.1"; 
    	InetAddress trackerInetAdress = InetAddress.getByName(trackerAdress);  
    	int trackerPort = 6969; 
    	//String trackerId = Utils.randomPeerId(); 
    	String trackerId = "-BE0001-n%9A%B4%40%2Cb..zq%5D%9D";
    	//Client
    	String clientAdress = "127.0.0.1"; 
    	int clientPort = 61245; 
		Peer[] peers ;     	//pairs that have exchanged the file
    	to = new Torrent(new File("src/resources/torrents/iceberg.jpg.torrent"));
		
    	//SEEDER - TRACKER PART
		System.out.println("		HTTP SEEDER");
    	// HHTP : The seeder communicate with the tracker : saying he got the file ! 
    	Seeder seeder = new Seeder(); 

    	seeder.contactTracker(trackerAdress, trackerPort, to.getInfo_hash(), to.getFileLength());
		//TO DO : reception du OK par le seeder ?
		
		// LEECHER - TRACKER PART : in theory the leecher as the tracker for the seeder ip 
		System.out.println("		HTTP LEECHER");
		Connection con2 = new Connection(clientAdress, clientPort,trackerAdress, trackerPort, trackerId);
		con2.sendGETAnnounce(to.getInfo_hash(), seeder.getPortlistening(), 0, 0, to.getFileLength(), 60);
    
		peers = con2.getPeers(); //recuperating the list of peers (exept tracker) IP, port
		//1)Tracker ok
		//2)LEECHER - SEEDER (Vuze) 		
    	// Creation of the socket that is connected to server on specified port
    	int i=0; 
		while(peers[i]!=null) { //for each peer
    		Socket socket = new Socket(peers[i].getAdress().toString().substring(1), peers[i].getPort());
        	// Creation of streams to get and send messages
			
//			Socket socket = new Socket(server, servPort);
			in = socket.getInputStream();
			out = socket.getOutputStream();
	  		lf = new LengthFramer(in);
    		
	  		/// the leecher (0%) starts
        	BittorrentConnection.blockingSendHandshake(to.getInfo_hash_hex(), in, out);
    		socket.close();
    		
    	}
    }
}
