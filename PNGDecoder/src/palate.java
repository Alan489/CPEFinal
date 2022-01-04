import java.util.ArrayList;

public class palate 
{
	private ArrayList<ColorTriple> paletteList;
	public palate()
	{
		paletteList =  new ArrayList<>();
	}
	
	public void addColor(ColorTriple ct)
	{
		paletteList.add(ct);
	}
	
	public ColorTriple getColor(int a)
	{
		//System.out.println("Request: " + a);
		//if (a >= paletteList.size())
			//a = paletteList.size()-1;
		return paletteList.get(a);
	}
	
	public int size()
	{
		return paletteList.size();
	}
	
}
