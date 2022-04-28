package bittorrent;

public class SendMessage {
	
	private boolean isInterested;
	private boolean isNotInterested;
	private boolean isChoke;
	private boolean isUnchoke;
	private boolean isHave;
	private boolean isBitfield;
	private boolean isRequest;
	private boolean isPiece;
	private boolean isKeepAlive;
	
	private int totalPieces;

	public SendMessage(boolean isInterested, boolean isNotInterested, boolean isChoke, boolean isUnchoke,
			boolean isHave, boolean isBitfield, boolean isRequest, boolean isPiece, boolean isKeepAlive,
			int totalPieces) {
		super();
		this.isInterested = isInterested;
		this.isNotInterested = isNotInterested;
		this.isChoke = isChoke;
		this.isUnchoke = isUnchoke;
		this.isHave = isHave;
		this.isBitfield = isBitfield;
		this.isRequest = isRequest;
		this.isPiece = isPiece;
		this.isKeepAlive = isKeepAlive;
		this.totalPieces = totalPieces;
	}

	public boolean isInterested() {
		return isInterested;
	}

	public void setInterested(boolean isInterested) {
		this.isInterested = isInterested;
	}

	public boolean isNotInterested() {
		return isNotInterested;
	}

	public void setNotInterested(boolean isNotInterested) {
		this.isNotInterested = isNotInterested;
	}

	public boolean isChoke() {
		return isChoke;
	}

	public void setChoke(boolean isChoke) {
		this.isChoke = isChoke;
	}

	public boolean isUnchoke() {
		return isUnchoke;
	}

	public void setUnchoke(boolean isUnchoke) {
		this.isUnchoke = isUnchoke;
	}

	public boolean isHave() {
		return isHave;
	}

	public void setHave(boolean isHave) {
		this.isHave = isHave;
	}

	public boolean isBitfield() {
		return isBitfield;
	}

	public void setBitfield(boolean isBitfield) {
		this.isBitfield = isBitfield;
	}

	public boolean isRequest() {
		return isRequest;
	}

	public void setRequest(boolean isRequest) {
		this.isRequest = isRequest;
	}

	public boolean isPiece() {
		return isPiece;
	}

	public void setPiece(boolean isPiece) {
		this.isPiece = isPiece;
	}

	public boolean isKeepAlive() {
		return isKeepAlive;
	}

	public void setKeepAlive(boolean isKeepAlive) {
		this.isKeepAlive = isKeepAlive;
	}

	public int getTotalPieces() {
		return totalPieces;
	}

	public void setTotalPieces(int totalPieces) {
		this.totalPieces = totalPieces;
	}
	
	
	

}
