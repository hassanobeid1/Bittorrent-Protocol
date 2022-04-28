package bittorrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BittorentSeedingConnection {
	
	/*public static void blockingSendHandshake(String hashInfo, InputStream in,OutputStream out)  throws IOException {
		String peerId = BittorentLeechingConnection.createID();
		
		byte[] handshakeMessage = Utils.hexStringToByteArray("13" + BittorrentMessages.handshakeProtocolName + "0000000000000000" + hashInfo + peerId);
		System.out.println("\nSending handshake...");

		// Send the encoded string to the server
		out.write(handshakeMessage);

		// Receive handshake response and Bitfield from the server	 
		BittorrentConnection.blockingReceiveMessageFromServer(BittorrentMessages.responseHandshakeMessage, in);
		//blockingReceiveMessageFromServer(BittorrentMessages.bitfieldMessage, in);

	}*/
	
	 
	
	public static void blockingSendBitfield(InputStream in, OutputStream out, int nbPieces) throws IOException {
		//byte[] bitfieldMessage = Utils.hexStringToByteArray("00000000000305ffe0"); 
		//[11111111,11100000]
		int size = nbPieces / 8 + 1;
		byte[] bitfield = new byte[size];
		for (int i = 0; i < nbPieces; i++)
			bitfield[i/8] |= 1 << (7 - i% 8); //2^(7-i%8) to turn a bit

	
		ByteBuffer buffer = null;

		//bitfield: <len=0001+X><id=5><bitfield>
		int x = bitfield.length;
		buffer = ByteBuffer.allocate(4+1+bitfield.length);
		buffer.putInt(1+x);    //size of int = 4
		buffer.put((byte) 5);  //1
		buffer.put(bitfield, 0, bitfield.length); //size of bitfield

		byte[] btfldmessage = buffer.array();
		
		out.write(btfldmessage); 
		BittorrentConnection.blockingReceiveMessageFromServer(btfldmessage, in);
		
		System.out.println("Inerested received\n");

	}
	
	///////////////////////////
	
	public static void nonBlockingSendHandshake(String hashInfo, SocketChannel clntChan) throws IOException {
		byte[] handshakeMessage = Utils.hexStringToByteArray("13" + BittorrentMessages.handshakeProtocolName + "0000000000000000" + hashInfo + "2d4245303030312d6e9ab4402c622e2e7a715d9d");
		ByteBuffer writeBuf = ByteBuffer.wrap(handshakeMessage);
		clntChan.write(writeBuf);
		System.out.println("\nSending handshake...");
	}
	
	
	public static void nonBlockingSendUnchoke(SocketChannel clntChan) {
		byte[] unchokeMessage = Utils.hexStringToByteArray("0000000101");
		System.out.println("\nSending Unchoke...");
		ByteBuffer writeBuf = ByteBuffer.wrap(unchokeMessage);
		try {
			clntChan.write(writeBuf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void nonBlockingSendBitfield(int nbPieces, SocketChannel clntChan) {
		byte[] bitfield = Bitfield.generateSeederBitfield(nbPieces);
	
	
		ByteBuffer writeBuf = ByteBuffer.wrap(Bitfield.generateBitfieldMessage(bitfield));
		try {
			clntChan.write(writeBuf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void nonBlockingSendPiece( Torrent to, int index, int offset, int size, SocketChannel clntChan) throws IOException {
		
		// récupération de la data from file
		
	
		byte[] piece = new byte[size];
		
		Path currentPath = Paths.get(".");
		Path fullPath = currentPath.toAbsolutePath();
		String s = fullPath.toString();
		String [] t = s.split("/");
		s = t[0];
		for (int i = 1; i < t.length -1; i++) {
			s += "/" + t[i];
		}
		s += "/src/resources/PiecesFile.jpg";
		RandomAccessFile file = new RandomAccessFile(s, "r");
		file.seek((long) (index) * to.getPieceLength() + offset);
		file.read(piece);
		System.err.println(Utils.bytesToHex(piece));
		
		ByteBuffer buffer = ByteBuffer.allocate(4+1+4+4+ size);
		buffer.putInt(9+size);
		buffer.put((byte)7);
		buffer.putInt(index);
		buffer.putInt(offset);
		buffer.put(piece);
		
		//String length= Integer.toHexString(9 + size);
//		while(length.length() != 8)
//			length = "0" + length;
		
		//String pieceStr = Utils.bytesToHex(piece);
		
		// Creation du msg Piece
		//byte[] pieceMessage = Utils.hexStringToByteArray(length + "07" + index +offset + pieceStr);
		//ByteBuffer writeBuf = ByteBuffer.wrap(pieceMessage);
		buffer.flip();
		clntChan.write(buffer);
		System.out.println("\nSending piece...");
		
	}


}
