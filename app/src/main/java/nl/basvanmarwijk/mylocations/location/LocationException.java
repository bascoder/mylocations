/**
 * 
 */
package nl.basvanmarwijk.mylocations.location;

/**
 * Exception that occurs within LocationBridge
 * @author Bas van Marwijk
 * @since revision 2
 * @version 1
 * @see LocationBridge
 *
 */
public class LocationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8151395371906410243L;

	/**
	 * 
	 */
	public LocationException() {
		super();
	}

	/**
	 * @param detailMessage
	 */
	public LocationException(String detailMessage) {
		super(detailMessage);
		
	}

	/**
	 * @param throwable
	 */
	public LocationException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public LocationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
