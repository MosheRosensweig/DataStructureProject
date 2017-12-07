import java.util.ArrayList;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SQLParser;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SQLQuery;
import net.sf.jsqlparser.JSQLParserException;

/**
 * This is the public class. This has the Execute method
 * where you type in a SQL query and get a result set.
 * (See ResultSet for what it returns)
 * 
 * @author mosherosensweig
 * @version 5/8/17 3:53pm
 *
 */
public class DataBase {

	private SQLParser parser = new SQLParser();
	private ArrayList<TablePackage> tablePackages = new ArrayList<TablePackage>();
	//for printing out errors
	private boolean verbose = false;
	
	/**
	 * Take user input and manage it
	 * @param SQL
	 * @return
	 */
	public ResultSet execute(String SQL)
	{
		if(SQL.equals("verbose")){
			verbose = !verbose;
			System.out.println("\n Verbose flag is now set to " + verbose + "\n");
			return null;
		}
		System.out.println("Type in SQL query:\t" + SQL);
		//System.out.println(SQL);
		SQLParserControl sqlPC = new SQLParserControl(tablePackages);
		ResultSet result = null;
		try {
			SQLQuery parseResult = parser.parse(SQL);
			result = sqlPC.typeCheck(parseResult);
			if(!(result == null)) result.setVerbose(verbose);
			
		} catch (JSQLParserException e) {
			if(verbose) e.printStackTrace();
			else System.out.println("\n" + e + "\n");
		}
		catch(ProjectException f){
			if(verbose) f.printStackTrace();
			else System.err.println("\n" + f + "\n");
			
		}
		catch(Exception ee){
			if(verbose) ee.printStackTrace();
			else System.out.println(ee);
		}
		return result;
	}
	
	
	//testing
	public ArrayList<TablePackage> getTPs()
	{
		return tablePackages;
	}
}
