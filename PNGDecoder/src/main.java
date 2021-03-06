import java.io.File;
import java.util.Scanner;


//Currenty supports the following:

//
//NO Interlace
//Color mode 3: Bit depths 4 and 8
//Color mode 6: Bit depths 8
//
//
//
//

public class main {

	final public static String Signature = 
			  "*****************\n"
			+ "**Alan Decowski**\n"
			+ "**Final Project**\n"
			+ "*****CPE-462*****\n"
			+ "******2021F******\n"
			+ "*****************\n";
	
	//getInput(Scanner in, [boolean exact], [String prompt])
	//A simple overloaded method to get various types of input from the user.
	//Exact flag returns the exact user input, NOT a lower case copy, which the default method returns.
	//Prompt argument adds text before the scanner line, giving a user an indication what the program is expecting.
	
	private static String getInput(Scanner in)
	{
		System.out.print(">> ");
		
		return in.nextLine().toLowerCase();
	}
	
	private static String getInput(Scanner in, String prompt)
	{
		System.out.print(prompt + " >> ");
		
		return in.nextLine().toLowerCase();
	}
	
	private static String getInput(Scanner in, boolean exact)
	{
		System.out.print(">> ");
		
		if (exact)
			return in.nextLine();
		else
			return in.nextLine().toLowerCase();
	}
	
	private static String getInput(Scanner in, boolean exact, String prompt)
	{
		System.out.print(prompt + " >> ");
		
		if (exact)
			return in.nextLine();
		else
			return in.nextLine().toLowerCase();
	}
	
	
	//getFile(Scanner in)
	//A method to get the user to input a valid file. Will also check to see if the file is flagged as a PNG file.
	
	//  C:\Users\Alan\Desktop\Untitled.png
	//  C:\Users\Alan\Desktop\Fluid_Mosaic_Model.png
	
	private static File getFile(Scanner in)
	{
		String input = getInput(in, true, "Enter exact path to file");
		File f = new File(input);
		
		if (f.exists() & !f.isDirectory() & f.isFile())
			return f;
		else
			return null;
	}
	
	public static void main(String[] args) throws Exception {
		
		//ColorTriple ct = new ColorTriple(0,0,0);
		//ColorTriple ts = new ColorTriple(ct.toInt());
		//System.out.println(ct.toInt() + " " + ts.toInt());
		//new PNG(new File("C:\\Users\\Alan\\Desktop\\Fluid_Mosaic_Model.png"));
		long t1 = 65244;
		long t2 = 65210;
		//System.out.println(t1+t2);
		//System.exit(0);
		
		//new PNG(new File("C:\\Users\\Alan\\Desktop\\test2.png"));
		//System.exit(0);
		
		// TODO Auto-generated method stub
		System.out.println(Signature);
		if (args.length == 0)
		{
			System.out.println("No command line arguments detected. Enter file you wish to display.");
			Scanner in = new Scanner(System.in);
			File f = getFile(in);
			while (f == null)
			{
				System.out.println("Invalid file.");
				f = getFile(in);
			}
			in.close();
			if (f == null)
				System.out.println("Invalid file.");
			else
			{
				new PNG(f);
			}
			
		}
	}

}
