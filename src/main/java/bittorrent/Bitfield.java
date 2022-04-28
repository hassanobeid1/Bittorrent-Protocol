package bittorrent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Bitfield {
	private byte[] bitfield = null;
	private int nb_pieces = -1; 
	
	public Bitfield(int nbPieces) {
		int size = nbPieces / 8 + 1;
		bitfield = new byte[size];
		nb_pieces = nbPieces; 
	}
	
	/*
	 * @return the index of the first missing piece, 
	 * according the bitfield
	 * */
	public int getFirstMissingPiece() {
//		for(int j =0; j< this.getBitfield().length; j++) { //parcours des octets
//			for(int k = 0; k<8; k++) {
//				System.out.println("bitfield["+j+"]["+k+"] ="+  (bitfield[j] & (1 << (7 - k)) )) ;
//			}
//		}
		int i = 0; 
		while(get(i)!=0 && (i<(8*bitfield.length))) {
				i++; 
//				System.out.println("get("+i+") = " + get(i));
		}
		if(i==nb_pieces) {
			System.out.println("bitfield : no missing piece"); 
			return (-1); 
		}
		System.out.println("index of first missing piece is " + i);
		return i; 
	}
	
	
	public Bitfield(byte[] bitfield) {
		this.bitfield = bitfield;
	}
	
	/**
	 * Génère le Bitfield du seeder 
	 * @param nbPieces: nombre de pièces du torrent
	 * @return le bitfield
	 */
	public static byte[] generateSeederBitfield(int nbPieces) {
		Bitfield bitfield = new Bitfield(nbPieces);
		for (int i = 0; i < nbPieces; i++)
			bitfield.set(i);
		return bitfield.bitfield;
	}
	
	/**
	 * Génère le bitfield du leecher
	 * @param nbPieces du torrent
	 * @return le bitfield
	 */
	public static byte[] generateLeecherBitfield(int nbPieces) {
		Bitfield bitfield = new Bitfield(nbPieces);
		for (int i = 0; i < nbPieces; i++)
			bitfield.getBitfield()[i] = 0;
		
		return bitfield.bitfield;
	}
	
	/**
	 * transforme le bitfield en un message byte[] afin de pouvoir l'envoyer aux socketchan 
	 */
	
	public static byte[] generateBitfieldMessage(byte[] bitfield) {
		ByteBuffer buffer = null;
		
		int x = bitfield.length;
		buffer = ByteBuffer.allocate(4+1+bitfield.length);
		buffer.putInt(1+x);    //size of int = 4
		buffer.put((byte) 5);  //1
		buffer.put(bitfield, 0, bitfield.length); //size of bitfield

		return buffer.array();		
	}
	
	public void setBitfield(byte[] bitfield) {
		this.bitfield= bitfield;
	}
	
	public byte[] getBitfield() {
		return bitfield;
	}
	

	/**
	 * Change le bit de la pièce correspondante en 1 
	 * @param i : numéro de la pièce
	 */
	public void set(int i) { // le cas de la reception d'une pièce, 1
		bitfield[i/8] = (byte) ( bitfield[i/8] | 1 << (7-(i%8)));
	}
	
	/**
	 * get the bit of index i 
	 * @param i : numéro de la pièce
	 */
	public int get(int i) { // le cas de la reception d'une pièce, 1
		return (bitfield[i/8] & (1 << (7 - (i%8))));
	}
	
	public int[] getCorrespondingIntArray() {
		int [] intArray = new int[nb_pieces]; 
		for(int i=0; i<nb_pieces; i++) {
			if( get(i) > 0 ) intArray[i] = 1; 
			else intArray[i] = 0 ; 
		}
		return(intArray); 
	}
	
	/**
	 * Change le bit de la pièce correspondante en 0 
	 * @param i: numéro de la pièce
	 */
	public void unset(int i) { // tourne le bit de la piece dans le bitfield en 0
		bitfield[i/8] &= ~1 << (7 - i% 8);
	}
	
	/**
	 * Retourne le statut de la pièce: si notre client la contient ou pas
	 * @param pieceIndex : index de la pièce
	 * @return
	 */
	public boolean pieceStatus(int pieceIndex) { //retourne true si la piece existe dans le bitfield
		if( ((bitfield[pieceIndex/8]>>(7-(pieceIndex%8))) & 1) != 0)
			return true;
		else
			return false;
	}
	
	public static void getPiecesFromFile(Torrent to,int size,  int index, int offset) throws IOException {
		System.out.println("This is the size" +size);
		
		byte[] sousPiece = new byte[size];		
		//or this one 
		//byte[] piece = new byte[size];		
		Path currentPath = Paths.get(".");
		Path fullPath = currentPath.toAbsolutePath();
		String s = fullPath.toString();
		String [] t = s.split("/");
		s = t[0];
		for (int i = 1; i < t.length -1; i++) {
			s += "/" + t[i];
		}
		s += "/src/resources/PiecesFile";
		RandomAccessFile file = new RandomAccessFile(s, "r");
		file.seek((long) (0) * size);
		file.read(sousPiece);
		
		System.err.println(Utils.bytesToHex(sousPiece));
		System.err.println("THis message lenght is  " + sousPiece.length);
	}
	
}
