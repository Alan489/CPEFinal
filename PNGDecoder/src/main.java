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
		
		ColorTriple ct = new ColorTriple(0,0,0);
		ColorTriple ts = new ColorTriple(ct.toInt());
		System.out.println(ct.toInt() + " " + ts.toInt());
		//new PNG(new File("C:\\Users\\Alan\\Desktop\\Fluid_Mosaic_Model.png"));
		new PNG(new File("C:\\Users\\Alan\\Desktop\\test1.png"));
		System.exit(0);
		
		// TODO Auto-generated method stub
		System.out.println(Signature);
		if (args.length == 0)
		{
			System.out.println("No command line arguments detected. Type \"Help\"/\"?\" for interface help or type \"Options\" for command line options. Type \"Exit\" to exit.");
			Scanner in = new Scanner(System.in);
			boolean acceptInput = true;

			
			while (acceptInput)
			{
				switch(getInput(in))
				{
					case "?":
					case "help":
						System.out.println("Help");
						System.out.println("\tDisplay - Begins prompt to open a file.");
						System.out.println("\tValidate - Begins prompt to validate a PNG file.");
						System.out.println("\tX - Exits the program.");
						break;
						
					case "display":
						
						break;
					
					case "validate":
						
						File f = getFile(in);
						if (f == null)
							System.out.println("Invalid file.");
						else
						{
							new PNG(f);
						}
						break;
						
					case "x":
					case "exit":
						System.out.println("Goodbye!");
						in.close();
						System.exit(0);
						break;
						
						
					default:
						System.out.println("Command not recognized. Type \"Help\"/\"?\"");
				}
				
			}
			
		}
	}

}