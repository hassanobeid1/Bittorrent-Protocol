package bittorrent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {

	private String s; 
	private RandomAccessFile f; 
	private int fileLength ; 
	private int pieces_number;
	private int pieces_size; 

	public String getPath() {
		return s; 
	}
	public RandomAccessFile getFile() {
		return f; 
	}
	/**
	 * Bitfield 
	 * @return a bitfield corresponding to the file's state
	 */

	public  FileManager(Torrent to) {
		fileLength = to.getFileLength(); 
		pieces_number = to.getNumberOfPieces(); 
		pieces_size = to.getPieceLength(); 
		s = "";
		// Constructing the path of the file
		Path currentPath = Paths.get(".");
		Path fullPath = currentPath.toAbsolutePath();
		s = fullPath.toString();
		String [] t = s.split("/");
		s = t[0];
		for (int i = 1; i < t.length -1; i++) {
			s += "/" + t[i];
		}
		s += "/src/resources/PiecesFile.jpg";
		try {
			f = new RandomAccessFile(s,"rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 

	}

	public Bitfield getBitfieldUponFile() {
		Bitfield b = new Bitfield(this.pieces_number); 
		byte[] bytes = new byte[this.pieces_size]; 
		for(int i=0; i< this.pieces_number ; i++) {//looking at each piece
			try {
				this.f.seek(i*this.pieces_size);
				f.read(bytes); //getting a piece
				int j = -1;
				//comparing piece to a initialized one
				do {
					j++;
				//for(int k=0; k<8; k++) {
					//System.out.println(Byte.toUnsignedInt(bytes[j]));	
				//}
				if(Byte.toUnsignedInt(bytes[j]) != 0) {	//setting b : the piece exists
					b.set(i); // we find something else than a zero at the piece place
				}
				
				} while(j<this.pieces_size-1 && (Byte.toUnsignedInt(bytes[j]) == 0)) ; //while we see nothing in piece, we seek
			
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
			System.out.println(
					"the corresponding bitfield is "+ Utils.bytesToHex(b.getBitfield())
							);
		return b;
	}
	/**
	 * Writes the piece in the file
	 * @param index : index of the piece
	 * @param piece 
	 * @param file : in which to write
	 * @throws IOException
	 */
	public static void writePieceFile(long index ,  byte[] piece, RandomAccessFile file) throws IOException {
		System.out.println("writing the piece of index : " + index);
		
		int pieceLength = TCPServerSelector.getTorrent().getPieceLength() ;
		int size_last_piece ; 

		file.seek(index * pieceLength); //position the pointer at the right index
//		if(index == 7 | index == 4) {
//			return; 
//		}
		if(index == (TCPServerSelector.getTorrent().getNumberOfPieces()-1)) {// dernière piece
			if(- (TCPServerSelector.getTorrent().getFileLength() - pieceLength * index)> pieceLength/2) { // la première demi piece est récupérable
				//on regarde la taille du dernier message : 
				size_last_piece = (int)
						( TCPServerSelector.getTorrent().getFileLength()
						- (TCPServerSelector.getTorrent().getPieceLength() * index) -
						(int) (TCPServerSelector.getTorrent().getPieceLength()/2) ); 
				}
			else { // only bloc to write
				size_last_piece = (int)
						( TCPServerSelector.getTorrent().getFileLength()
						- (TCPServerSelector.getTorrent().getPieceLength() * index)
						);
			}
			file.write(piece, 13, (pieceLength/2)); 
			file.seek(index * pieceLength + pieceLength/2); //position the pointer at the right index
			file.write(piece, (pieceLength/2)+ 26, size_last_piece); 
		}
		else {
			file.write(piece, 13, (pieceLength/2)); 
			file.seek(index * pieceLength + pieceLength/2); //position the pointer at the right index
			file.write(piece, (pieceLength/2)+ 26, (piece.length/2)); 
		}

	}

}
