package bittorrent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class BittorrentMessages {
	/*To comment in non blocking case*/ public static int completePieceLength = App.to.getPieceLength() / 2 + 13;
	/*To uncomment in non blocking case*///public static int completePieceLength = TCPServerSelector.getTorrent().getPieceLength();
	public static int bittorentMessageLength =  completePieceLength + 26; // TO DO : TCP NON BLOCKING ? 
	/*To uncomment in non blocking case*///public static int bittorentLastMessageLength =  (TCPServerSelector.getTorrent().getFileLength() % TCPServerSelector.getTorrent().getPieceLength()) + 26; // TO DO : TCP NON BLOCKING ? 
	public static int responseHandshakeMessageSize = 68; 
	public static byte[] responseHandshakeMessage = new byte[responseHandshakeMessageSize];
	public static byte[] bitfieldMessage = new byte[7];
	public static byte[] unchokeMessage = new byte[5];
	public static byte[] piece = new byte[completePieceLength];
	public static String handshakeProtocolName = "426974546F7272656E742070726F746F636F6C";
	public static String BitfieldMessageType = "05";
	public static String UnchokeMessageType = "01";
	public static String InterestedMessageType = "02";
	//	public static final int LastPieceBlocSize = ( completePieceLength % TCPServerSelector.BUFSIZE); 

	/**
	 * State machine
	 * Called every time we read a message
	 * @param dataToRead : the received message
	 * @throws IOException
	 */
	public static void blockingCheckMessage(byte[] dataToRead) throws IOException {
		String receivedMessage = Utils.bytesToHex(dataToRead);
		switch(dataToRead.length) {
		case 5: //Unchoke
			if(receivedMessage.substring(8, 10).equals(UnchokeMessageType))
			{
				System.out.println("Unchoke received\n");
				System.arraycopy(dataToRead, 0, unchokeMessage, 0, unchokeMessage.length);
				BittorrentConnection.blockingSendPiecesRequests(App.to, App.in,  App.out, App.lf);
			}
			else 
				System.out.println("Unknown message received " + receivedMessage);

			break;

		case 7: //Bitfield
			if(receivedMessage.substring(8, 10).equals(BitfieldMessageType))
			{
				System.out.println("Handshake with Bitfield received");
				System.arraycopy(dataToRead, 0, bitfieldMessage, 0, bitfieldMessage.length); //useful data
				BittorrentConnection.blockingSendInterstedAndBitfield(App.in, App.out);
			}
			else
				System.out.println("Unknown message received "+ receivedMessage);

			break;

		case 68: //Handshake
			if(receivedMessage.substring(0, 2).equals("13") && receivedMessage.substring(2, 40).equals(handshakeProtocolName))
			{
				System.out.println("responseHandshakeMessage.length" + responseHandshakeMessage.length);
				System.out.println("Handshake response received");
				System.arraycopy(dataToRead, 0, responseHandshakeMessage, 0, responseHandshakeMessage.length);
			}

			else
				System.out.println("Unknown message received");
			break;


		case 16397:
			System.out.println("A piece is received");
			System.arraycopy(dataToRead, 0, piece, 0, completePieceLength);
			break;

		default:
			System.out.println("Unknown message received");
			System.out.println(Utils.bytesToHex(dataToRead));
			break;


		}
	}

	public static Peer findPeerBySocketChannel(SocketChannel clntChan) {
		int port = clntChan.socket().getPort();
		System.out.println("ports TCPServerSelector.peers " + TCPServerSelector.peers + "Connection.nb_peers" + Connection.nb_peers);

		for (int i=0; i < Connection.nb_peers; i++) {
			if ( TCPServerSelector.peers[i].getPort() == port)
				return TCPServerSelector.peers[i];
		}
		System.err.println("erreur pas de peer ayant ce port trouvé");
		return null;
	}

	/**
	 * State machine
	 * Called every time we read a message
	 * @param dataToRead : the received message
	 * @param bytesRead : size 
	 * @throws IOException
	 */
	public static void nonBlockingCheckMessage(byte[] dataToRead, long bytesRead,  SocketChannel clntChan ) throws IOException {
		String receivedMessage = Utils.bytesToHex(dataToRead);
		Peer peer = findPeerBySocketChannel(clntChan); // TO DO 

		switch((int)bytesRead) {
		case 12: //bitfield and interested
			System.err.println("Message recieved is " + receivedMessage);
			System.out.println("receivedMessage.substring(8, 10) == " + receivedMessage.substring(8, 10));
			if(receivedMessage.substring(8, 10).equals(BitfieldMessageType))
			{
				System.out.println("Bitfield and interested received\n");
				peer.peer_interested = true;
				if (peer.peer_choked) {
					BittorentSeedingConnection.nonBlockingSendUnchoke(clntChan);
					peer.peer_choked = false;
				}
			}

			else 
				System.out.println("Unknown message received " + receivedMessage);

			break;
		case 5: //Unchoke and interested
			System.out.println("receivedMessage.substring(8, 10) == " + receivedMessage.substring(8, 10));
			if(receivedMessage.substring(8, 10).equals(UnchokeMessageType))
			{
				System.out.println("Unchoke received\n"); // we need start asking for missing pieces 
				// getFirstMissingPiece checkBitfield
				System.arraycopy(dataToRead, 0, unchokeMessage, 0, unchokeMessage.length);
				peer.am_choked = false;
				//ask for a first Message : using the bitfield : getFirstMissingPiece
				BittorentLeechingConnection.nonBlockingSendRequest( TCPServerSelector.to, clntChan); 
			}
			else if(receivedMessage.substring(8, 10).equals(InterestedMessageType)) {
				peer.peer_interested = true;
				if (peer.peer_choked) {
					BittorentSeedingConnection.nonBlockingSendUnchoke(clntChan);
					peer.peer_choked = false;
				}
			}
			else 
				System.out.println("Unknown message received " + receivedMessage);
			break;
		case 7: //Handshake + Bitfield  // TO DO 
			if(receivedMessage.substring(8, 10).equals(BitfieldMessageType))
			{
				System.out.println("Handshake with Bitfield received");
				System.arraycopy(dataToRead, 0, bitfieldMessage, 0, bitfieldMessage.length); //useful data
				//BittorentLeechingConnection.nonBlockingSendInterstedAndBitfield(clntChan);						);
			}
			else
				System.out.println("Unknown message received "+ receivedMessage);

			break;

		case 75: //Handshake
		case 68:
			//			System.out.println("receivedMessage" + receivedMessage);
			System.out.println("Waiting for "+ handshakeProtocolName );
			if(receivedMessage.substring(0, 2).equals("13") && receivedMessage.substring(2, 40).equals(handshakeProtocolName))
			{
				System.out.println("Handshake response received");
				System.arraycopy(dataToRead, 0, responseHandshakeMessage, 0, responseHandshakeMessage.length);
				peer.am_handshaked = true;
				if (peer.peer_handshaked) {  //si on a envoyé le handshake au peer
					BittorentLeechingConnection.nonBlockingSendInterstedAndBitfield(clntChan);
				}
				else {
					BittorentSeedingConnection.nonBlockingSendHandshake(TCPServerSelector.getTorrent().getInfo_hash_hex(), clntChan );
					peer.peer_handshaked = true;
					BittorentSeedingConnection.nonBlockingSendBitfield(TCPServerSelector.to.getNumberOfPieces(), clntChan);

				}
				break;
			}





		case 13:

			break;

			/// receiving the last bloc of this piece
		default:
			/// receiving a piece block 
			if( (int)bytesRead == bittorentMessageLength || (int)bytesRead == bittorentLastMessageLength) { 
				System.out.println("A piece of size " + completePieceLength + "is received, of index " + Integer.parseInt(receivedMessage.substring(10, 18), 16)); 
				//System.out.println("value" + receivedMessage );
				//BittorentLeechingConnection.addingBlockToPiece(dataToRead, clntChan); 
				//add piece at the right index 
				try {
					FileManager.writePieceFile(Integer.parseInt(receivedMessage.substring(10, 18), 16), dataToRead, TCPServerSelector.fileManager.getFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
				//responding with have
				int indexReceived = Integer.parseInt(receivedMessage.substring(10, 18), 16); 
				//we received a piece -> 
				TCPServerSelector.personnalBitfield.set(indexReceived);
				BittorentLeechingConnection.nonBlockingSendHave(indexReceived, clntChan); 
				BittorentLeechingConnection.nonBlockingSendRequest( TCPServerSelector.to, clntChan); 
			}


			// Reception du msg Request du client Bittorent

			else if((int)bytesRead%17 == 0) {System.err.println("Message >100 and lenght is : "+ bytesRead); 
			LengthFramer lf = new LengthFramer(ByteBuffer.wrap(dataToRead));
			while (lf.buffer.hasRemaining()) {
				System.err.println("framer");
				byte[] msg = lf.nioNextMsg();
				String rcvmsg = Utils.bytesToHex(msg);
				System.out.println(rcvmsg);
				if (rcvmsg.length() != 34)
					break;
				String index = rcvmsg.substring(10, 18);
				String sizeOfPiece= rcvmsg.substring(26, 34);
				String offset= rcvmsg.substring(18, 26);

				System.err.println("L'index est : " + index);
				System.err.println("L'offset est : " + offset);
				System.err.println("Le piece length est : " + sizeOfPiece);

				BittorentSeedingConnection.nonBlockingSendPiece(TCPServerSelector.to, Integer.parseInt(index, 16), Integer.parseInt(offset, 16), Integer.parseInt(sizeOfPiece, 16), clntChan);

			}
			}
			else {
				System.out.println("Unknown message of size "+ dataToRead.length + "received");
				//System.out.println(Utils.bytesToHex(dataToRead));
			}
			break;

		}
	}
}

