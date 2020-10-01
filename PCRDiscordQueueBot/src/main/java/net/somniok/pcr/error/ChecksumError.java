package net.somniok.pcr.error;

public class ChecksumError extends Throwable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3940483321498396816L;
	
	public ChecksumError() {
		super();
	}
	
	public ChecksumError(String msg) {
		super(msg);
	}
}
