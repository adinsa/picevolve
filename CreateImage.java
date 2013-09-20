/**
  * Demonstration of how to create images using ImgProcessing class.
  * Uses the same algorithm used in createImage method in PicEvolve.py.
  *
  * Amar Dinsa 2010
  */

import java.awt.image.*;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
import java.lang.reflect.Method;

public class CreateImage
{
	// funcSet maps the available image processing functions in ImgProcessing class
	// to the number of arguments each function takes.
	private static final Map<String, Integer> funcSet = new HashMap<String, Integer>();
	static {
		funcSet.put("add", 2);
		funcSet.put("sub", 2);
		funcSet.put("mul", 2);
		funcSet.put("div", 2);
		funcSet.put("mod", 2);
		funcSet.put("rnd", 2);
		funcSet.put("minimum", 2);
		funcSet.put("maximum", 2);
		funcSet.put("absolute", 1);
		funcSet.put("expt", 2);
		funcSet.put("logarithm", 2);
		funcSet.put("And", 2);
		funcSet.put("Or", 2);
		funcSet.put("Xor", 2);
		funcSet.put("sine", 1);
		funcSet.put("asine", 1);
		funcSet.put("cosine", 1);
		funcSet.put("acosine", 1);
		funcSet.put("tangent", 1);
		funcSet.put("atangent", 1);
	}
	 
	public static void main(String[] args)
	{
		int width = 400;
		int height = 400;

		// Put the tokens of the command line s-expression into an arraylist backwards
		ImgProcessing ip = new ImgProcessing(width, height);
		String[] sList = args[0].replace("(","").replace(")","").split(" ");	
		Collections.reverse(Arrays.asList(sList));

		ArrayList<Object> iList = new ArrayList<Object>();
		for (int i = 0; i < sList.length; i++)
		{
			if (sList[i].length() > 0)
				iList.add(sList[i]);
		}
		// Process every function, replacing it and its arguments with the rendered image
		// until one image is left.
		while (iList.size() > 1)
		{
			for (int i = 0; i < iList.size(); i++)
			 {
				 if (CreateImage.funcSet.containsKey(iList.get(i)))
				 {
					 if (CreateImage.funcSet.get(iList.get(i)) == 1)
					 {
						 try
						 {
							 Method func = ip.getClass().getMethod((String)iList.get(i), Object.class);
							 double rawImage[] = (double[])func.invoke(ip, iList.get(i-1));
							 iList.remove(i);
							 iList.add(i, rawImage);
							 iList.remove(i-1);
							 break;
						} catch (Exception e)
						{
							e.printStackTrace();
							return;
						}
					 }
					 else if (CreateImage.funcSet.get(iList.get(i)) == 2)
					 {
						try
						{
							Method func = ip.getClass().getMethod((String)iList.get(i), Object.class, Object.class);
							double rawImage[] = (double[])func.invoke(ip, iList.get(i-1), iList.get(i-2));
							iList.remove(i);
							iList.add(i, rawImage);
							iList.remove(i-1);
							iList.remove(i-2);
							break;
						} catch (Exception e)
						{
							e.printStackTrace();
							return;
						}

					 }
				 }
			 }
		}
		
		// Now draw the image to a buffered image and save it to a file.
		double rawImage[] = (double[])iList.get(0);

                BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D gr = bi.createGraphics();

                gr.setBackground(Color.WHITE);
                gr.clearRect(0,0, bi.getWidth(), bi.getHeight());

                int index = 0;
                for (int i = 0; i < width; i++)
                {
                        for (int j = 0; j < height; j++)
                        {
				int colorVal = (int)(rawImage[i] * 255);
                                gr.setColor(new Color(colorVal,colorVal,colorVal));
                                gr.drawRect(i,j, 1,1);
                                index++;
                        }
                }

                try {
                        ImageIO.write(bi, "png", new File("image.png"));
                } catch (IOException e)
                {
                        System.out.println("Error!\n");
                }

	}
}
