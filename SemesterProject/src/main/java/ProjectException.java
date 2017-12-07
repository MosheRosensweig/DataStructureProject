/**
 * This is my generic project exception
 * Any exception I throw will be one of these so I can 
 * distinguish when catching them
 * @author mosherosensweig
 *
 */
public class ProjectException extends Exception{
	public ProjectException(String str)
	{
		super(str);
	}
}
