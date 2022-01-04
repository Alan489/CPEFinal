
public class ColorTriple 
{
	public long R,G,B;
	private double sR, sG, sB;
	private int Depth = 8;
	protected int[] rgb;
	public ColorTriple(int r, int g, int b)
	{
		R = r;
		G = g;
		B = b;
		Depth = 8;
		process();
	}
	
	public ColorTriple(int r, int g, int b, int depth)
	{
		//System.out.println("MAN DEPTH");
		R = Integer.toUnsignedLong(r);
		G = Integer.toUnsignedLong(g);
		B = Integer.toUnsignedLong(b);
		Depth = depth;
		process();
	}
	
	public ColorTriple(int fromValue)
	{
		long t = Integer.toUnsignedLong(fromValue);
		B = (long) (t % 256);
		t /= 256;
		G = (long) (t % 256);
		t /= 256;
		R = (long) t;
		//if (B >= 100)
		{
			//System.out.println("Here we are. " + R + " " + G + " " + B);
		}
	}
	
	public String set(int r, int g, int b)
	{
		R = r;
		G = g;
		B = b;
		Depth = 8;
		process();
		return "" + R + " " + G + " " + B;
	}
	
	public String set(long r, long g, long b)
	{
		R = r;
		G = g;
		B = b;
		Depth = 8;
		process();
		return "" + R + " " + G + " " + B;
	}
	public String set(long r, long g, long b, int depth)
	{
		R = r;
		G = g;
		B = b;
		Depth = depth;
		process();
		//if (sR != 0.0)
		//System.out.println(sR);
		return "" + R + " " + G + " " + B;
	}
	
	public String set(int r, int g, int b, int depth)
	{
		R = r;
		G = g;
		B = b;
		Depth = depth;
		process();
		return "" + R + " " + G + " " + B;
	}
	
	public String toString()
	{
		return "" + Long.toUnsignedString(R) + " " + Long.toUnsignedString(G) + " " + Long.toUnsignedString(B);
	}
	
	private void process()
	{
		//Get fractions for later use.
		sR = R/(Math.pow(2, Depth)-1);
		sG = (double)G/(Math.pow(2, Depth)-1);
		sB = B/(Math.pow(2, Depth)-1);
		//System.out.println(R + " " + G + " " + B);
	}
	
	public int getR()
	{
		return (int)R;
	}
	public int getG()
	{
		return (int)G;
	}
	public int getB()
	{
		return (int)B;
	}
	public int[] getRGB()
	{
		return rgb;
	}
	public int toInt()
	{
		return (int) (255.0 * sR* (int) Math.pow(2, 16) + 255.0 * sG* (int) Math.pow(2, 8) + 255.0 * sB);
	}
	public void gammaCorrect(double gamma)
	{
		//Not implemented.
		double decodingExpo = 10.0 / (gamma * 100);
		
		//System.out.print(sR + " " + sG + " " + sB + "\t");
		int a;
		a = (int) (255.0 * Math.pow(sR, decodingExpo));
		a = (int) (255.0 * Math.pow(sG, decodingExpo));
		a = (int) (255.0 * Math.pow(sB, decodingExpo));
		//System.out.println(R + " " + G + " " + B);
		process();
	}
	
	public void add(ColorTriple ct)
	{
		R += ct.R;
		R %= (long) Math.pow(2, Depth);
		G += ct.G;
		G %= (long) Math.pow(2, Depth);
		B += ct.B;
		B %= (long) Math.pow(2, Depth);
		process();
	}
	
	public boolean equals(ColorTriple ct)
	{
		return ct.R == R & ct.G == G & ct.B == B & ct.Depth == Depth;
	}
	
	public ColorTriple average(ColorTriple ct)
	{
		return new ColorTriple(((int) ((R + ct.R)/2.0))%256, ((int) ((G + ct.G)/2.0))%256, ((int) ((B + ct.B)/2.0))%256 ); 
	}
	
	public ColorTriple clone()
	{
		return new ColorTriple((int)R,(int)G,(int)B,Depth);
	}
	
	
	
}