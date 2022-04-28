package bittorrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

/// Bittorents messages used in a leeching context
public class BittorentLeechingConnection {
	/**
	 * Function to send the "handshake" message
	 * It reads handshake's response at the end
	 * Note : this isn't a blocking function
	 * @param hashInfo
	 * @param clntChan
	 * @param writeBuf
	 * @param readBuf
	 * @throws IOException
	 */
	public static void nonBlockingSendHandshake(String hashInfo, SocketChannel clntChan) throws IOException {
		byte[] handshakeMessage = Utils.hexStringToByteArray("13" + BittorrentMessages.handshakeProtocolName + "0000000000000000" + hashInfo + "2d4245303030312d6e9ab4402c622e2e7a715d9d");
		ByteBuffer writeBuf = ByteBuffer.wrap(handshakeMessage);
		clntChan.write(writeBuf);
		System.out.println("\nSending handshake...");
	}

	public static void nonBlockingSendInterstedAndBitfield(SocketChannel clntChan) throws IOException {
		byte[] interestedAndBitfieldMessage = Utils.hexStringToByteArray("000000010200000003050000");
		System.out.println("\nSending Intersted and Bitfield...");
		ByteBuffer writeBuf = ByteBuffer.wrap(interestedAndBitfieldMessage);
		clntChan.write(writeBuf);
	}

	public static void nonBlockingSendHave(int index_have, SocketChannel clntChan) throws IOException {
		String indexString  = Integer.toString(index_have); 
		String haveMessage = "0000000504"+   String.format("%0"+ (9 - indexString.length() )+"d%s",0 , indexString).substring(1,9);
		//String haveMessage = "00000005040000000" + Integer.toString(index_have); // We should add the id at the end of this message
		System.out.println("Going to send have : " + haveMessage);
		byte[] haveBytes = Utils.hexStringToByteArray(haveMessage);
		ByteBuffer haveMessageBuffer = ByteBuffer.wrap(haveBytes);
		clntChan.write(haveMessageBuffer);
	}

	public static void nonBlockingSendRequest( Torrent to, SocketChannel clntChan ) throws IOException {

		int index = TCPServerSelector.personnalBitfield.getFirstMissingPiece();
		if(index ==-1) {
			System.out.println("on a déjà demandé toutes les pièces : TO DO : verify the hash is ok");
			//on regénère le bitifield pour être surs
			System.out.println("on regénère le bitifield pour être surs");
			TCPServerSelector.personnalBitfield = TCPServerSelector.fileManager.getBitfieldUponFile();
			index = TCPServerSelector.personnalBitfield.getFirstMissingPiece();
		}
		if(index != -1) {
			String id = Integer.toString(index / 16); 			// id : piece index

			switch(index % 16) {
			case 10: id+= "a"; break;
			case 11: id+= "b"; break;
			case 12: id+= "c"; break;
			case 13: id+= "d"; break;
			case 14: id+= "e"; break;
			case 15: id+= "f"; break;
			default: id+= Integer.toString(index);
			}

			//String pieceLength = "00000004000";
			String messageToSend = "";
			String part1 = "0000000d06000000"; 			//message lenght + type : request
			String piece_length_1; 
			String piece_length_2 = null; 			// piece lentgh : to.getPieceLength /2 si pas la dernière
			String begin_offset_1;  			// begin offset =  0000 ou 4000
			String begin_offset_2 = null; 
			String full_piece_length = Integer.toHexString(to.getPieceLength()/2); 

			if(index == (to.getNumberOfPieces()-1)) {// dernière piece
				System.out.println("derniere piece : taille totale " + (to.getFileLength() - to.getPieceLength() * index) );
				System.out.println("to.getPieceLength()/2" + to.getPieceLength()/2);
				String size_last_piece ; 
				if( (to.getFileLength() - to.getPieceLength() * index)> to.getPieceLength()/2) { // la première demi piece est récupérable
					System.out.println(" la première demi piece est récupérable sur full_piece_length");
					size_last_piece = Integer.toHexString(to.getFileLength() - to.getPieceLength() * index - to.getPieceLength()/2); 	
					piece_length_1 = String.format("%0"+ (9 - full_piece_length.length() )+"d%s",0 , full_piece_length).substring(1,9);
					piece_length_2 = String.format("%0"+ (9 - size_last_piece.length() )+"d%s",0 , size_last_piece).substring(1,9);
					begin_offset_1 = String.format("%0"+ 8 + "d" , 0);
					begin_offset_2 = String.format("%0"+ (9 - full_piece_length.length() )+"d%s",0 , full_piece_length).substring(1,9);
				}
				else {
					size_last_piece = Integer.toHexString(to.getFileLength() - to.getPieceLength() * index ); 
					piece_length_1 =  String.format("%0"+ (9 - size_last_piece.length() )+"d%s",0 , size_last_piece).substring(1,9);
					begin_offset_1 =  String.format("%0"+ 8 + "d" , 0);
				}
			}
			else {			
				piece_length_1 = String.format("%0"+ (9 - full_piece_length.length() )+"d%s",0 , full_piece_length).substring(1,9);
				piece_length_2 = String.format("%0"+ (9 - full_piece_length.length() )+"d%s",0 , full_piece_length).substring(1,9);
				begin_offset_1 = String.format("%0"+ 8 + "d" , 0);
				begin_offset_2 = String.format("%0"+ (9 - full_piece_length.length() )+"d%s",0 , full_piece_length).substring(1,9);
			}
			TCPServerSelector.personnalBitfield.set(index);
			//System.out.println("the bitfield with the new piece now is " + Utils.bytesToHex(TCPServerSelector.personnalBitfield.getBitfield()));
			//we are going to ask the piece, thus no one should do the same : 
			// Starting to send requests
			System.out.println("Requesting the piece number " + index );
			messageToSend =  part1 + id + begin_offset_1 +  piece_length_1;
			//System.out.println("\nSending  " + messageToSend);
			ByteBuffer writeBuf = ByteBuffer.wrap(Utils.hexStringToByteArray(messageToSend)); 
			clntChan.write(writeBuf);

			messageToSend = part1 + id + begin_offset_2 + piece_length_2;
			//System.out.println("\nSending  " + messageToSend);
			writeBuf = ByteBuffer.wrap(Utils.hexStringToByteArray(messageToSend)); 
			clntChan.write(writeBuf);
		}
	}
}

