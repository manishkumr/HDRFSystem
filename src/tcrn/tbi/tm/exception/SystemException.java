package tcrn.tbi.tm.exception;

/**
 * @author Manish
 *
 */
public class SystemException extends Exception{
	
	private static final long serialVersionUID = -8472870625017696744L;
	
	/**
	 * Default constructor
	 */
	public SystemException(){}
	
	/**
	 * @param message error message to throw
	 */
	public  SystemException(String message) {
		super(message);
	}

}
