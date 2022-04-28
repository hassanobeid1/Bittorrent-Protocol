package bittorrent;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import org.junit.Test;

import be.adaxisoft.bencode.InvalidBEncodingException;


//
public class AnnounceTest {
	@Test
	public void announce1() throws Exception  {
		try {
			//Torrent
			 //Socket socket = new Socket("127.0.0.1", 6969);
			//int tracker_port = 6888; 
			int tracker_port = 0; 
			Torrent t = new Torrent(new File("src/resources/torrents/hello_world.txt.torrent"));
			
			//lancement du tracker avec ./opentracker
			
			System.out.println("lancement du thread de reception des sockets au port "+tracker_port);
			//							    ip_client,  port_client ip_peer port_peer     peer_id
			Connection con = new Connection("127.0.0.1",61190 ,"127.0.0.1", tracker_port,"-AZ5760-Suuay04hIXmq");
		
			//Envoi d'un get au tracker en lui indiquant port_listening
			//                  info_hash       port_listening, uploaded, downloaded, left, nb_peers_wanted) 
			con.sendGETAnnounce(t.getInfo_hash(), 61190, 0, 0, 0, 60);	
			
			/*
			int port_listening2 = 0; 
			//Création d'un leacher qui envoie un GET/announce en demandant le fichier au tracker
			Connection con2 = new Connection("127.0.0.1",61190 ,"127.0.0.1", tracker_port, random_ip);
			con2.sendGETAnnounce(t.getInfo_hash(), port_listening2, 
					0, 0,
					t.getFileLength(),  
					60);
			*/
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
