package bittorrent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class AppSeeder {
	
	public static void main( String[] args )throws Exception
    {
		
    	String server = "127.0.0.1"; 		 
    	int servPort = 54545;     	// Server port(Vuze) 
    
    	String trackerAdress = "127.0.0.1"; 
    	InetAddress trackerInetAdress = InetAddress.getByName(trackerAdress);  
    	int trackerPort = 6969; 
    	
    	//String trackerId = Utils.randomPeerId(); 
    	String trackerId = "-BE0001-n%9A%B4%40%2Cb..zq%5D%9D";
    	
    	//Client
    	String clientAdress = "127.0.0.1"; 
    	int clientPort = 61245; 
    	
    	//Peers
		Peer[] peers ;     	//pairs that have exchanged the file
    	Torrent to = new Torrent(new File("src/resources/torrents/iceberg.jpg.torrent"));
		

		
        // SEEDING PART
		
		ServerSocket s1=new ServerSocket(0);  
		int port_client_listenning = s1.getLocalPort();
		
		
    	//SEEDER - TRACKER PART
		System.out.println("		HTTP SEEDER");
    	// HHTP : The seeder communicate with the tracker : saying he got the file ! 
    	Seeder seeder = new Seeder(); 
    	seeder.contactTracker(trackerAdress, trackerPort, to.getInfo_hash(), to.getFileLength(), port_client_listenning);

		System.out.println("port_client_listenning = " + port_client_listenning);
        Socket ss=s1.accept();   
		
		Scanner sc=new Scanner(ss.getInputStream());  
	    String s=ss.toString(); 
	    System.out.println(s);
	    InputStream in = ss.getInputStream();
    	OutputStream out = ss.getOutputStream();
	    BittorrentConnection.blockingSendHandshake(to.getInfo_hash_hex(), in, out);
	    BittorrentConnection.blockingSendBitfield(in, out, to.getNumberOfPieces());
	    BittorrentConnection.sendUnchoke(in, out);
	    
        ss.close();
        s1.close();
				
    }

}
