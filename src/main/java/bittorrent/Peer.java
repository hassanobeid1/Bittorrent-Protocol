package bittorrent;

import java.net.InetAddress;

public class Peer {
	private InetAddress inetAdress;
	private int port ; 
	private String id;
	
	public boolean am_choked = true;   //ce peer nous a choked
	public boolean am_interested = false;
	public boolean peer_choked = true;         //nous avons choked ce peer
	public boolean peer_interested = false;
	public boolean am_handshaked = false;
	public boolean peer_handshaked = false;
	
	public Bitfield bitfield;
	
	public Peer(InetAddress peer_adress, int peer_port, String peer_id) {
		inetAdress= peer_adress; 
		port =peer_port; 
		id = peer_id; 
	}
	
	public Peer(InetAddress peer_adress, int peer_port) {
		inetAdress= peer_adress; 
		port =peer_port; 
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Peer) {
			Peer p = (Peer) other; 
			if(this.inetAdress.equals(p.inetAdress) && this.port==p.port && this.id.equals(p.id)) 
				return (true);  
		}
		return (false); 
	}

	public InetAddress getAdress() {
		return inetAdress;
	}

	public int getPort() {
		return port;
	}

	public String getId() {
		return id;
	}
}
