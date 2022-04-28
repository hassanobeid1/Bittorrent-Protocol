package bittorrent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import be.adaxisoft.bencode.BDecoder;
import be.adaxisoft.bencode.BEncodedValue;
import be.adaxisoft.bencode.BEncoder;
import be.adaxisoft.bencode.InvalidBEncodingException;

//This class is just an example to bootstrap but you may change everything in it even the name
public class Torrent {

	/** The info hash of the torrent file */
	private byte[] info_hash = new byte[20];
	
	/** The length of a torrent file */
	private String fileLength = "";
	
	/** The length of pieces */
	private String pieceLength = "";

	public Object getFileLenght;
	
	/**
	 * Constructor of the class that creates a torrent object from a torrent file, and initialize 
	 * info_hash, the size of the file, the size of packets and the number of packets
	 * @param filepath which is the torrent file
	 * @throws InvalidBEncodingException
	 */
	public Torrent(File filepath) throws InvalidBEncodingException {
		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
			//read and decode the torrent
			FileInputStream inputStream = new FileInputStream(filepath);
			BDecoder reader = new BDecoder(inputStream);
			Map<String, BEncodedValue> document = reader.decodeMap().getMap();
			//extracting the info dictionary
			Map<String, BEncodedValue> infoMap = document.get("info").getMap(); // Maps
			System.out.println(infoMap);
			
			//encoding it if and only if the bdecoder fully validated the input
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BEncoder.encode(infoMap, baos);
			System.out.println(baos);
			
			this.info_hash = sha1.digest(baos.toByteArray());		

			///To get the length of the file
			String aux = baos.toString();
			
			System.out.println(aux);
			
			int debut  = aux.indexOf("lengthi") + 7;
			
			while(aux.charAt(debut) != 'e')
				{
					this.fileLength += aux.charAt(debut);
					debut ++;
				}
			
			
			///To get the length of packets
			debut  = aux.lastIndexOf("lengthi") + 7;
			
			while(aux.charAt(debut) != 'e')
				{
					this.pieceLength += aux.charAt(debut);
					debut ++;
				}
			System.out.println(this.fileLength);
			System.out.println(this.pieceLength);
			
		}
		
		catch(FileNotFoundException e){
			System.out.println("Wrong url"); 
		}
		catch(InvalidBEncodingException e){
			System.out.println("Invalid Bencoding of file "+ filepath.toString()); 
			throw e; 
		}
		catch(IOException e){
			System.out.println("InvalidIOException"); 
		}
		catch(NoSuchAlgorithmException e) {
			System.out.println("Iso non trouvé"); 
		}
		
	}

	/**
	 * @return the info_hash of a torrent file
	 */
	public byte[] getInfo_hash() {
		return info_hash;
	}
	
	/**
	 * @return the info_hash of a torrent file
	 */
	public String getInfo_hash_hex() {
		return Utils.bytesToHex(info_hash);
	}
	
	/**
	 * @return the length of a torrent file
	 */
	public int getFileLength() {
		return Integer.parseInt(this.fileLength);
	}
	
	/**
	 * @return the length of pieces in a torrent file
	 */
	public int getPieceLength() {
		return Integer.parseInt(this.pieceLength);
	}
	
	/**
	 * @return the number of pieces of a torrent file
	 */
	public int getNumberOfPieces(){
		return (int) (Math.ceil(this.getFileLength() / (double)this.getPieceLength()));
	}

}
