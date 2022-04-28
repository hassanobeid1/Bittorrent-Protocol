/**
 * This class contains all functions used to establish a connection between leechers and seeders
 * @author Hassan
 */

package bittorrent;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class BittorrentConnection {
	
	

	/**
	 * Function to receive a message from the server
	 * Used in Handshake, interested & Bitfield
	 * Note : this is a blocking function
	 * @param data : to read data
	 * @param in : InputStream
	 * @throws SocketException
	 * @throws IOException
	 */
	public static void blockingReceiveMessageFromServer(byte[] data, InputStream in) throws SocketException, IOException {

		byte[] dataToRead = new byte[data.length]; 
		int totalBytesRcvd = 0; 		  			// Total bytes received so far
		int bytesRcvd;        			   	        // Bytes received in last read

		while (totalBytesRcvd < dataToRead.length) {

			if ((bytesRcvd = in.read(dataToRead, totalBytesRcvd, dataToRead.length - totalBytesRcvd)) == -1)
				throw new SocketException("Connection closed prematurely");

			totalBytesRcvd += bytesRcvd;
		}

		System.out.println("Received message : " + Utils.bytesToHex(dataToRead));
		System.out.println("Received message length = " + dataToRead.length);

		BittorrentMessages.blockingCheckMessage(dataToRead);
		//BittorrentMessages.blockingCheckMessage(dataToRead);
	}

	//TO DO : DELETE : unuseful
	/**
	 * Function to receive a message from the server
	 * Used in Handshake, interested & Bitfield
	 * Note : this isn't a blocking function
	 * @param data : to read data
	 * @param clntChan : the channel used to read data
	 * @param writeBuf : the buffer used to send data
	 * @param readBuf : the buffer used to receive data
	 * @throws IOException
	 */

	//	public static void nonBlockingReceiveMessageFromServer(byte[] data, SocketChannel clntChan, ByteBuffer writeBuf, ByteBuffer readBuf) throws IOException {
//
//		int totalBytesRcvd = 0; // Total bytes received so far
//		int bytesRcvd; 			// Bytes received in last read
//		while (totalBytesRcvd < data.length) {
//			if (writeBuf.hasRemaining()) {
//				clntChan.write(writeBuf);
//			}
//
//			if ((bytesRcvd = clntChan.read(readBuf)) == -1) {
//				throw new SocketException("Connection closed prematurely");
//			}
//
//			totalBytesRcvd += bytesRcvd;
//			//System.out.println("."); // Do something else
//		}
//
//		// convert to String per default charset
//		System.out.println("Received message : " + Utils.bytesToHex(readBuf.array()));
//		System.out.println("Received message length = " + readBuf.array().length);
//		BittorrentMessages.blockingCheckMessage(readBuf.array());
//	}

	/**
	 * Function to send the "handshake" message
	 * It reads handshake's response at the end
	 * Note : this is a blocking function
	 * @param hashInfo : the id of torrent file we want to download
	 * @param in : InputStream
	 * @param out : OutputStram
	 * @throws IOException
	 */
	public static void blockingSendHandshake(String hashInfo, InputStream in,OutputStream out) throws IOException {
		byte[] handshakeMessage = Utils.hexStringToByteArray("13" + BittorrentMessages.handshakeProtocolName + "0000000000000000" + hashInfo + "2d4245303030312d6e9ab4402c622e2e7a715d9d");
		System.out.println("\nSending handshake...");
		// Send the encoded string to the server
		out.write(handshakeMessage);
		// Receive handshake response and Bitfield from the server	 
		blockingReceiveMessageFromServer(BittorrentMessages.responseHandshakeMessage, in);
		blockingReceiveMessageFromServer(BittorrentMessages.bitfieldMessage, in);
	}


	/**
	 * Function to send "Interested" and "Bitfield" message
	 * It reads the "Unchoke" message at the end
	 * Note : this is a blocking function
	 * @param in : InputStream
	 * @param out : OutputStream
	 * @throws IOException
	 */
	public static void blockingSendInterstedAndBitfield(InputStream in, OutputStream out) throws IOException {
		byte[] interestedAndBitfieldMessage = Utils.hexStringToByteArray("000000010200000003050000");

		System.out.println("\nSending Intersted and Bitfield...");

		// Send data to Vuze
		out.write(interestedAndBitfieldMessage); 
		// Receive the unchoke message from the server
		blockingReceiveMessageFromServer(BittorrentMessages.unchokeMessage, in);
	}

	/**
	 * Function to send "Interested" and "Bitfield" message
	 * It reads the "Unchoke" message at the end
	 * Note : this isn't a blocking function
	 * @param clntChan
	 * @param writeBuf
	 * @param readBuf
	 * @throws IOException
	 */
	public static void nonBlockingSendInterstedAndBitfield(SocketChannel clntChan, ByteBuffer writeBuf, ByteBuffer readBuf) throws IOException {
		byte[] interestedAndBitfieldMessage = Utils.hexStringToByteArray("000000010200000003050000");

		System.out.println("\nSending Intersted and Bitfield...");

		// Send data to Vuze
		writeBuf = ByteBuffer.wrap(interestedAndBitfieldMessage);

		// Receive the unchoke message from the server
		//nonBlockingReceiveMessageFromServer(BittorrentMessages.unchokeMessage, clntChan, writeBuf, readBuf);
	}


	/**
	 * Function to send requests for pieces and to receive them
	 * @param lf : LengthFramer to construct the message
	 * @param to : Torrent file
	 * @param out : OutputStream
	 * @throws IOException
	 */

	public static void blockingSendPiecesRequests(Torrent to, InputStream in, OutputStream out, LengthFramer lf) throws IOException {

		String part1 = "0000000d06000000"; 
		String id = ""; 
		String part3 = "0000";
		String offset = "4";
		String pieceLength = "00000004000";
		String messageToSend = "";
		String haveMessage = "0000000504000000"; // We should add the id at the end of this message
		int k = 0;
		String s = "";

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

		//Creating a file to write pieces inside it
		RandomAccessFile file = new RandomAccessFile(s, "rw");

		// Starting to send requests
		System.out.println("Requesting and receiving pieces...");

		int i;
		for(i = 0; i < to.getNumberOfPieces(); i++) 
		{

			haveMessage = "0000000504000000";

			id = Integer.toString(i / 16);

			switch(i % 16) {
			case 10: id+= "a"; break;
			case 11: id+= "b"; break;
			case 12: id+= "c"; break;
			case 13: id+= "d"; break;
			case 14: id+= "e"; break;
			case 15: id+= "f"; break;
			default: id+= Integer.toString(i);
			}

			// Switching offset
			for(int j = 0; j < 2; j++) {

				if(offset.equals("4"))
					offset = "0";
				else
					offset = "4";

				// Build the initial message
				messageToSend =  part1 + id + part3 + offset + pieceLength;
				System.out.println("\nSending " + messageToSend);

				// Sending the request
				out.write(Utils.hexStringToByteArray(messageToSend));

				// Reading pieces and writing them in a file
				if((to.getFileLength() - to.getPieceLength() * i >= to.getPieceLength()) || ((to.getFileLength() - to.getPieceLength() * i < to.getPieceLength() && offset.equals("0"))))
				{
					blockingReceiveMessageFromServer(BittorrentMessages.piece, in);

					file.write(BittorrentMessages.piece, 13, BittorrentMessages.piece.length - 13);

					System.out.println("Piece written in the file located in " + s);
				}
				else
				{
					System.out.println("No piece to receive");		
					file.close();
				}

			}

			/// Have Message
			haveMessage += id; 
			out.write(Utils.hexStringToByteArray(haveMessage));
		}
		BittorrentConnection.sendNotInterested(lf, out);
	}


	/**
	 * Function to send "Not Interested" message
	 * @param lf : LengthFramer to construct the message
	 * @param out : OutputStream
	 * @throws IOException
	 */
	public static void sendNotInterested(LengthFramer lf, OutputStream out) throws IOException {
		String notInterestedMessage = "0000000103";
		lf.frameMsg(Utils.hexStringToByteArray(notInterestedMessage), out);
	}
	
	/**
	 * Function to send "Not Interested" message
	 * @param lf : LengthFramer to construct the message
	 * @param out : OutputStream
	 * @throws IOException
	 */
	public static void nonBlockingSendNotInterested(LengthFramer lf, OutputStream out) throws IOException {
		String notInterestedMessage = "0000000103";
		lf.frameMsg(Utils.hexStringToByteArray(notInterestedMessage), out);
	}



	// Messages sent by the seeder
	

	/**
	 * Function of the seeder to send "Bitfield" message
	 * It reads the "Interested" message at the end
	 * @param in : InputStream
	 * @param out : OutputStream
	 * @throws IOException
	 */
	
	public static void sendBitfield(InputStream in, OutputStream out) throws IOException {
		byte[] bitfieldMessage = Utils.hexStringToByteArray("00000000000305ffe0\n");
		System.out.println("\nSending Intersted and Bitfield...");
		System.out.println(bitfieldMessage.length + " is the same length that we see in wireshark (Bitfield)");
		// Send data to Vuze
		out.write(bitfieldMessage); 
		// Receive the same string back from the server
		//receiveSameMessageFromServer(bitfieldMessage, in);
		blockingReceiveMessageFromServer(bitfieldMessage, in);					
		System.out.println("Interested received\n");
	}
	
	/**
	 * Function to send "Unchoke" message
	 * It reads the "Request" message at the end
	 * @param in : InputStream
	 * @param out : OutputStream
	 * @throws IOException
	 */
	
	public static void sendUnchoke(InputStream in, OutputStream out) throws IOException {
		byte[] unchokeMessage = Utils.hexStringToByteArray("00000000000101\n");
		
		System.out.println("\nSending Unshoke...");
		System.out.println(unchokeMessage.length + " is the same length that we see in wireshark (Unchoke)");
		// Send data to Vuze
		out.write(unchokeMessage); 
		// Receive the same string back from the server
		//receiveSameMessageFromServer(unchokeMessage, in);
		blockingReceiveMessageFromServer(unchokeMessage, in);					
		System.out.println("Request received\n");
	}
	
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
		blockingReceiveMessageFromServer(btfldmessage, in);
		System.out.println("Inerested received\n");
	}
	
}
