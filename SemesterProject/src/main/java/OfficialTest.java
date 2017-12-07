import java.util.ArrayList;

/**
 * This is My official Test. To Understand My code First read the READ_ME, then read the code
 * 
 * There are two ways to test it - the verbose and non-verbose way - your choice - set the 'Verbose' flag
 * 
 * 
 * 
 * @author mosherosensweig
 *
 */
public class OfficialTest {

	private static boolean verbose = false;
	
	public static void main(String[] args) 
	{
		ResultSet rs;
		DataBase db = new DataBase();
		//I've added in a verbose feature
		if (verbose) db.execute("verbose");
		
		/*
		 * Note about exceptions: Since this rapidFires tests, the exceptions sometimes popup a bit later in the printout than they should
		 */
		
		//--------------//
		// Create Table //
		//--------------//
		rs = db.execute(createTable());
		System.out.println(rs);
		//---------------------------------//
		// Add Rows that do and don't work //
		//---------------------------------//
		/* Note: Sometimes, when adding rows so quickly, the error pops up removed from the row that caused the error*/
		for(String str :insertRow()){
			rs = db.execute(str);
			System.out.println(rs);
			System.out.println("\n\n");
		}
		System.out.println("\n\n\n\n\n");
		//--------//
		// Update //
		//--------//
		/* I'm only doing some of the queries */
		for(String str :updateQ()){
			rs = db.execute(str);
			System.out.println(rs);
			System.out.println("\n\n");
		}
		System.out.println("\n\n\n\n\n");
		//--------//
		// Delete //
		//--------//
		/* I have to clear and reset the table for this... The printout will reflect that, sorry */
		for(String str :deleteQ()){
			if(str.toLowerCase().contains("delete")) System.out.println("\n");
			rs = db.execute(str);
			//I put this in red so it should stand out
			if(str.toLowerCase().contains("delete")) System.err.println(rs);
		}
		System.out.println("\n\n\n\n\n");
		//--------//
		// Select //
		//--------//
		for(String str :selectQ()){
			rs = db.execute(str);
			//I put this in red so it should stand out
			System.out.println(rs);
			System.out.println("\n\n");
			
		}
		
	}
	
	private static String createTable()
	{
		//--------//
		// Create //
		//--------//

		 String query = "CREATE TABLE YCStudent"
						 + "("
						 + " BannerID int,"
						 + " SSNum int UNIQUE,"
						 + " FirstName varchar(255),"
						 + " LastName varchar(255)  NOT NULL,"
						 + " Class Varchar(255)  DEFAULT 'Freshman',"
						 + " GPA decimal(1,2)  DEFAULT 0.00,"
						 + " CurrentStudent boolean default false,"
						 + " PRIMARY KEY (BannerID)"
						 + ");";
		 return query;
	}
	
	private static  ArrayList<String> insertRow()
	{
		ArrayList<String> qrys = new ArrayList<String>();
		qrys.add("INSERT INTO YCStudent (FirstName) VALUES ('Tim');");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA, currentstudent) VALUES ('Shimon', 'Ferez' , 'Senior', 0, 1678354, 9.2, false);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA, currentstudent) VALUES ('Yehuda', 'Gale' , 'Senior', 1, 0678354, 9.0, false);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA, currentstudent) VALUES ('Noah', 'Frankel' , 'Junior', 2, 3423798, 3.0, true);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Nir', 'Frankel' , 'Freshman', 3, 4322435, 6.0);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Avi', 'Greenman' , 'null', 4, 43243, 4.0);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Meir', 'Alneck' , 'null', 5, 345465, 4.0);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Steve', 'Jobs' , 'Senior', 6, 45437545, 4.0);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, BannerID, SSNum, GPA) VALUES ('Micah', 'Shippel' , 7, 83723247, 4.0);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Yehuda', 'Bigowski' , 'Sophmore', 8, 432474383, 4.0);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA, currentstudent) VALUES ('Rick', 'Scott' , 'Senior', 9, 9335078, 4.0, true);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA, currentstudent) VALUES ('John', 'Appleseed' , 'Sophmore', 10, 9475743, 4.0, false);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Jamie', 'Benson' , 'null', 11, 8347947, 4.0);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Shalom', 'Mamon' , 'null', 12, 9957349, 4.0);");

		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Meir', 'Shemesh' , 'null', 13, 904575, 4.0);");
		qrys.add("INSERT INTO YCStudent (FirstName) VALUES ('Tim');");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Aaron','Shakibpanah', 'Sophmore',14, 800454, 3.8);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Yitzie', 'Schienman' , 'Sophmore', 15, 43543, 4.0);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Yakov', 'Stern' , 'Senior', 16, 25343425, 2.6);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Barack', 'Obama' , 'Sophmore', 17, 545665422, 4.0);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Moladoris', 'Frankel' , 'Freshman', 26, 4322436, 6.2);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Donald', 'Trump' , 'Junior', 18, 2345234, 3.7);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('George', 'Bush' , 'Senior',19, 2543525, 3.0);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Yair', 'Lapid' , 'null', 20, 4334534, 2.8);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Matan', 'Nomdar' , 'Freshman', 21, 24352345, 4.0);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Samuel', 'Tafara' , 'null', 22, 565465, 2.9);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Jonathan', 'Singer' , 'Junior', 23, 354234255, 3.4);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Yudi', 'Meltzer' , 'Junior', 24, 7654723, 1.6);");
		qrys.add("INSERT INTO YCStudent (FirstName, LastName, Class, BannerID, SSNum, GPA) VALUES ('Yuudi', 'Meltzer' , 'Senior', 25, 7654724, 1.6);");
		return qrys;
	}

	private static ArrayList<String> updateQ()
	{
		ArrayList<String> result = new ArrayList<>();
		//--------//
		// Update //
		//--------//
		/* I don't need all of these and extra ones could mess up the table too much */
		String update2 = "UPDATE YCStudent SET GPA=3.0,Class='Super Senior';";
		String update2p2 = "UPDATE YCStudent SET GPA=3.0,Class='Super Senior' where gpa > 20;";
		//String update3 = "UPDATE YCStudent SET GPA=3.0;";//works
		String update4 = "UPDATE YCStudent SET GPA=3.333;";//works - i.e. throws expected exception
		String update5 = "UPDATE YCStudent SET GPA=33.33;";//works - i.e. throws expected exception
		String update6 = "UPDATE YCStudent SET Currentstudent = false;";//works
		String update7 = "UPDATE YCStudent SET Currentstudent = null;";//works - i.e. throws expected exception
		//String update8 = "UPDATE YCStudent SET ClasS = 'Senior';";//works
		String update9 = "UPDATE YCStudent SET BannerID = 1;";//works - i.e. throws expected exception
		String update10 = "UPDATE YCStudent SET BannerID = 45 Where BannerID = 1;";//works
		String update11 = "UPDATE YCStudent SET ClasS = 'Wacko Senior' Where BannerID = 3;";//works
		String update12 = "UPDATE YCStudent SET GPA = 5.75 Where BannerID = 17;";//works
		String update1 = "UPDATE YCStudent SET GPA=3.91,Class='Final Senior' WHERE BannerID > 20;";
		//String update13 = "UPDATE YCStudent SET GPA=3.91,Class='Super Senior', currentstudent=true, lastname='winnerChenDiner', ssnum=4 WHERE BannerID=1;";
		//update14 - throws the correct error
		//String update14 = "UPDATE YCStudent SET GPA=3.91,Class='Super Senior', currentstudent=true, lastname='winnerChenDiner', ssnum=4 WHERE currentstudent=true;";
		//String update15 = "UPDATE YCStudent SET GPA=3.91,Class='Super Senior', currentstudent=true, lastname='winnerChenDiner' WHERE currentstudent=true;";
	
		result.add(update2);
		result.add(update2p2);
		//result.add(update3);
		result.add(update4);
		result.add(update5);
		result.add(update6);
		result.add(update7);
		//result.add(update8);
		result.add(update9);
		result.add(update10);
		result.add(update11);
		result.add(update12);
		result.add(update1);
		//result.add(update13);
		//result.add(update14);
		//result.add(update15);
		return result;
	}

	private static ArrayList<String> deleteQ()
	{
		//--------//
		// Delete //
		//--------//
		/* Since Delete alters the table too much, I make a new table sometimes*/
		ArrayList<String> result = new ArrayList<>();
		//--------//
		// Delete //
		//--------//
		String deleteQ1 = "DELETE FROM YCStudent;";//works
		String deleteQ2 = "DELETE FROM YCStudent WHERE Class !='Senior' AND GPA > 4.0;";
		String deleteQ2ps = "DELETE FROM YCStudent WHERE Class !='Senior' AND GPA > 24.0;";
		String deleteQ4 = "DELETE FROM YCStudent WHERE ((Class ='Senior' or GPA < 3.0) and  CurrentStudent = false) or BannerID != 2;";
		String deleteQ5 = "DELETE FROM YCStudent WHERE GPA > 4.0;";
		String deleteQ6 = "DELETE FROM YCStudent WHERE GPA <= 4.0;";
		String deleteQ7 = "DELETE FROM YCStudent WHERE CurrentStudent = fAlse;";
		//String deleteQ8 = "DELETE FROM YCStudent WHERE Currentstudent = fAlse;";
		result.add(deleteQ1);
		result.addAll(insertRow());
		result.add(deleteQ2);
		
		//clear the table
		result.add(deleteQ1);
		result.addAll(insertRow());
		result.add(deleteQ4);
		
		//clear the table
		result.add(deleteQ1);
		result.addAll(insertRow());
		result.add(deleteQ2ps);
		result.add(deleteQ5);
		
		/* Doesn't need the update*/
		result.add(deleteQ6);
		
		result.addAll(insertRow());
		result.add(deleteQ7);
		
		//clear the table
		result.add(deleteQ1);
		result.addAll(insertRow());
		return result;
	}
	private static ArrayList<String> selectQ()
	{
		ArrayList<String> result = new ArrayList<>();
		String selQuery3 = "SELECT * FROM YCStudent;";
		String selQuery3p3 = "SELECT * FROM YCStudent where class = 'baby';";
		result.add(selQuery3);
		result.add(selQuery3p3);
		String selQuery4 = "SELECT * FROM YCStudent where GPA > 4;";
		result.add(selQuery4);
		String selQuery5 = "SELECT * FROM YCStudent where Class = 'senior';";
		result.add(selQuery5);
		String selQuery6 = "SELECT * FROM YCStudent where currentstudent = true;";
		result.add(selQuery6);
		/*
		String selQuery7 = "SELECT * FROM YCStudent where currentstudent = true or GPA > 4;";
		String selQuery8 = "SELECT GPA FROM YCStudent where currentstudent = true or GPA > 4;";
		String selQuery9 = "SELECT class FROM YCStudent where currentstudent = true or GPA > 4;";
		*/
		String selQuery10 ="SELECT currentstudent FROM YCStudent where currentstudent = true or GPA > 4;";
		result.add(selQuery10);
		//-----------------//
		// Distinct checks //
		//-----------------//
		String selQuery11 ="SELECT Distinct currentstudent FROM YCStudent where currentstudent = true or GPA > 4;";
		String selQuery12 ="SELECT Distinct lastname FROM YCStudent where currentstudent = true or GPA > 4;";
		String selQuery13 ="SELECT Distinct GPa FROM YCStudent where GPA <= 4;";
		result.add(selQuery11);
		result.add(selQuery12);
		result.add(selQuery13);
		//---------//
		// OrderBY //
		//---------//
		String selQuery14 ="SELECT Distinct GPA FROM YCStudent where GPA <= 4 Order by GPA;";
		String selQuery15 ="SELECT Distinct GPA FROM YCStudent where GPA <= 4 Order by GPA desc;";
		String selQuery16 ="SELECT Distinct Bannerid FROM YCStudent where GPA <= 4 Order by Bannerid;";
		String selQuery17 ="SELECT Distinct Bannerid FROM YCStudent where GPA <= 4 Order by Bannerid desc;";
		String selQuery18 ="SELECT Distinct firstname FROM YCStudent where GPA <= 4 Order by firstname;";
		String selQuery19 ="SELECT Distinct firstname FROM YCStudent where GPA <= 4 Order by firstname desc;";
		result.add(selQuery14);
		result.add(selQuery15);
		result.add(selQuery16);
		result.add(selQuery17);
		result.add(selQuery18);
		result.add(selQuery19);
		//--------------------//
		// Multiple Order Bys //
		//--------------------//
		String selQuery40 ="SELECT * FROM YCStudent where GPA < 4 Order by lastName desc, firstname desc;";
		String selQuery41 ="SELECT * FROM YCStudent where GPA < 4 Order by lastName, firstname desc;";//worked
		String selQuery42 ="SELECT * FROM YCStudent where GPA < 4 Order by lastName, firstname;";//worked
		String selQuery43 ="SELECT * FROM YCStudent where GPA < 4 Order by lastName desc, firstname;";//worked
		result.add(selQuery40);
		result.add(selQuery41);
		result.add(selQuery42);
		result.add(selQuery43);
		//-------------------//
		// Null by orderBy's //
		//-------------------//
		String selQuery44 ="SELECT * FROM YCStudent where GPA < 4 Order by class, firstname;";//worked
		String selQuery45 ="SELECT * FROM YCStudent where GPA < 4 Order by lastname, class;";//worked
		String selQuery46 ="SELECT SUM(Currentstudent) FROM YCStudent where GPA < 4;";//I think it's good
		String selQuery47 ="SELECT SUM(gpa) FROM YCStudent where GPA < 4 Order by class;";//I think it's good
		//-----------//
		// Functions //
		//-----------//
		/** Count */
		String selQuery20 ="SELECT COUNT(firstname) FROM YCStudent;";
		String selQuery21 ="SELECT COUNT(class) FROM YCStudent;";
		result.add(selQuery20);
		result.add(selQuery21);
		/** Max */
		String selQuery22 ="SELECT MAX(ssnum) FROM YCStudent;";
		String selQuery23 ="SELECT MAX(BannerID) FROM YCStudent;";
		String selQuery24 ="SELECT MAX(GPA) FROM YCStudent;";
		result.add(selQuery22);
		result.add(selQuery23);
		result.add(selQuery24);
		/** Min */
		String selQuery25 ="SELECT MIN(ssnum) FROM YCStudent;";
		String selQuery26 ="SELECT MIN(BannerID) FROM YCStudent;";
		String selQuery27 ="SELECT MIN(GPA) FROM YCStudent;";
		result.add(selQuery25);
		result.add(selQuery26);
		result.add(selQuery27);
		/** AVG */
		String selQuery28 ="SELECT AVG(ssnum) FROM YCStudent;";
		String selQuery29 ="SELECT AVG(BannerID) FROM YCStudent;";//Check - worked great 
		String selQuery30 ="SELECT AVG(GPA) FROM YCStudent;";//Check - worked great 
		result.add(selQuery28);
		result.add(selQuery29);
		result.add(selQuery30);
		/** Sum */
		String selQuery31 ="SELECT SUM(ssnum) FROM YCStudent;";
		String selQuery32 ="SELECT SUM(BannerID) FROM YCStudent;";//Check - worked great 
		String selQuery33 ="SELECT SUM(GPA) FROM YCStudent;";//Check - worked great 
		result.add(selQuery31);
		result.add(selQuery32);
		result.add(selQuery33);
		//,,,,,,,,,,,,,,,,,,,,//
		// Combined Functions //
		//''''''''''''''''''''//
		String selQuery34 ="SELECT SUM(GPA), AVG(BannerID) FROM YCStudent;";//Check - worked great 
		result.add(selQuery34);
		//,,,,,,,,,,,,,,,,,,,,//
		// Distinct Functions //
		//''''''''''''''''''''//
		String selQuery35 ="SELECT SUM(Distinct GPA), AVG(BannerID) FROM YCStudent;";//Check - worked great 
		String selQuery36 ="SELECT COUNT(Distinct GPA) FROM YCStudent;";//Check - worked great 
		result.add(selQuery35);
		result.add(selQuery36);
		//------------------//
		// Function + Where //
		//------------------//
		String selQuery37 ="SELECT COUNT(currentstudent) FROM YCStudent Where GPA > 4;";//Check - worked great 
		String selQuery38 ="SELECT COUNT(Distinct currentstudent) FROM YCStudent Where GPA > 4;";//Check - worked great 
		String selQuery39 ="SELECT COUNT(Distinct currentstudent) FROM YCStudent Where GPA >= 4;";//Check - worked great 
		result.add(selQuery37);
		result.add(selQuery38);
		result.add(selQuery39);
		return result;
	}

}
