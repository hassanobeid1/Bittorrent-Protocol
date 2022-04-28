package bittorrent;

public class Seeder {
	
	private int port_listening ;
	private String ip_seeder; 
	private int port_client; 
	private String id ; 
	private int uploaded; 
	private int downloaded; 
	private int left;
	private int nb_peers_wanted; 
	
	
	public Seeder() {
		ip_seeder = "127.0.0.1"; 
		port_listening = 61190; 
		port_client = 61245 ; 
		//id = Utils.randomPeerId();
		id = "-AZ5760-Suuay04hIXmq"; 
		uploaded = 0; 
		downloaded = 0; 
		left = 0;  
		nb_peers_wanted = 60; 
	}
	
	public int getPortlistening() {
		return(port_listening); 
	}
	
	public void contactTracker(String trackerAdress, int trackerPort, byte[] info_hash, int file_lentgh) throws Exception {
		Connection con2 = new Connection(ip_seeder, port_listening,trackerAdress, trackerPort, id);
		con2.sendGETAnnounce(info_hash, port_listening, uploaded, downloaded, left, nb_peers_wanted);
	}
	
	public void contactTracker(String trackerAdress, int trackerPort, byte[] info_hash, int file_lentgh, int port_listening1) throws Exception {
		Connection con2 = new Connection(ip_seeder, port_listening1,trackerAdress, trackerPort, id);
		con2.sendGETAnnounce(info_hash, port_listening1, uploaded, downloaded, left, nb_peers_wanted);
	}
	}