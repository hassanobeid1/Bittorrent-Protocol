package bittorrent;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import be.adaxisoft.bencode.InvalidBEncodingException;

public class InfoHashTest {
	
//	@Test
//	public void test() {
//		fail("Not yet implemented");
//	}
	
	@Test
	public void testFile1()  {
		Torrent t;
		try {
			t = new Torrent(new File("src/resources/torrents/hello_world.txt.torrent"));
			assertEquals("285DCBB0DC5AE2ECB78F363AD1295A321C8EBFAF", t.getInfo_hash_hex());
			assertEquals(14, t.getFileLength());
			assertEquals(16384, t.getPieceLength());
			assertEquals(1, t.getNumberOfPieces());
		} catch (InvalidBEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFile2()  {
		try {
		Torrent t = new Torrent(new File("src/resources/torrents/iceberg.jpg.torrent"));
		assertEquals("067133ACE5DD0C5027B99DE5D4BA512828208D5B", t.getInfo_hash_hex());
		assertEquals(356639, t.getFileLength());
		assertEquals(32768, t.getPieceLength());
		assertEquals(11, t.getNumberOfPieces());
		} catch (InvalidBEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFile3()  {
		try {
			Torrent t = new Torrent(new File("src/resources/torrents/test.torrent"));
			assertEquals("e5f669898038384d240a0f6356582b35ca3181a6".toUpperCase(), t.getInfo_hash_hex());
			assertEquals(99399, t.getFileLength());
			assertEquals(32768, t.getPieceLength());
			assertEquals(4, t.getNumberOfPieces());
			} catch (InvalidBEncodingException e) {
				e.printStackTrace();
			}
		}

}
