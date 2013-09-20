/* 
 * Java port of ImgProcessing.py.
 * Each image processing function should be able to take as arguments
 * scalars, x, y or r,a coordinates, or images as arrays of doubles,
 * and return an image as an array of doubles.
 *
 * Amar Dinsa 2010
 */

import java.awt.image.*;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;

public class ImgProcessing
{

	// Domain of X and Y values (used in scalarToImg)
	private static final int[][] domain = {{-1,1},{-1,1}};

	private static int width, height;

	public ImgProcessing(int w, int h)
	{
		this.width = w;
		this.height = h;
	}

	public double[] add(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = aImg[i] + bImg[i];
		}

		return this.scaleImg(imgData);
	}

	public double[] sub(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = aImg[i] - bImg[i];
		}

		return this.scaleImg(imgData);
	}

	public double[] mul(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = aImg[i] * bImg[i];
		}

		return this.scaleImg(imgData);
	}

	public double[] div(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			double result = 0.0;
			if (bImg[i] != 0) 
				result = (double)aImg[i] / (double)bImg[i];
			imgData[i] = result;
		}

		return this.scaleImg(imgData);
	}

	public double[] mod(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			double result = 0.0;
			if (bImg[i] != 0) 
				result = (double)aImg[i] % (double)bImg[i];
			imgData[i] = result;
		}

		return this.scaleImg(imgData);
	}

	public double[] rnd(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = (double)Math.round(aImg[i]*Math.pow(10,(int)bImg[i])) / (Math.pow(10,(int)bImg[i]));
		}

		return this.scaleImg(imgData);
	}

	public double[] minimum(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = Math.min(aImg[i], bImg[i]);
		}

		return this.scaleImg(imgData);
	}


	public double[] maximum(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = Math.max(aImg[i], bImg[i]);
		}

		return this.scaleImg(imgData);
	}


	public double[] absolute(Object a)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = (double)Math.abs(aImg[i]);
		}

		return this.scaleImg(imgData);
	}

	public double[] expt(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			// Negative number cannot be raised to fractional power
			// Zero cannot be raised to negative power.
			if (!(aImg[i] == 0 && bImg[i] < 0))
			{
				if (aImg[i] < 0)
					bImg[i] = (int)bImg[i];
				imgData[i] = (double)Math.pow(aImg[i],bImg[i]);
			}
			else
				imgData[i] = 0.0;
		}

		return this.scaleImg(imgData);
	}

	public double[] logarithm(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			// Neither argument can be negative and b can't be 1
			if (aImg[i] > 0 && bImg[i] > 0 && bImg[i] != 1 && bImg[i] != 0)
				imgData[i] = (double)Math.log(aImg[i]) /
					     (double)Math.log(bImg[i]);
			else
				imgData[i] = 0.0;

		}

		return this.scaleImg(imgData);
	}

	public double[] sine(Object a)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = Math.sin(aImg[i]);
		}

		return this.scaleImg(imgData);
	}

	public double[] asine(Object a)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			if (aImg[i] < 1 || aImg[i] > 1)
				imgData[i] = 0.0;
			else
				imgData[i] = Math.asin(aImg[i]);
		}

		return this.scaleImg(imgData);
	}

	public double[] cosine(Object a)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = Math.cos(aImg[i]);
		}

		return this.scaleImg(imgData);
	}

	public double[] acosine(Object a)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			if (aImg[i] < -1 || aImg[i] > 1)
				imgData[i] = 0.0;
			else
				imgData[i] = Math.acos(aImg[i]);
		}

		return this.scaleImg(imgData);
	}

	public double[] tangent(Object a)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = Math.tan(aImg[i]);
		}

		return this.scaleImg(imgData);
	}

	public double[] atangent(Object a)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			imgData[i] = Math.atan(aImg[i]);
		}

		return this.scaleImg(imgData);
	}

	public double[] And(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			String aBin = Integer.toBinaryString(Math.abs((int)(aImg[i]*100)));
			String bBin = Integer.toBinaryString(Math.abs((int)(bImg[i]*100)));
			StringBuffer bitSum = new StringBuffer();
			for (int j = 0; j < Math.min(aBin.length(),bBin.length()); j++)
			{
				if (aBin.charAt(j) == bBin.charAt(j))
					bitSum.append('1');
				else
					bitSum.append('0');
			}
			int finalSum = 0;
			if (!(bitSum.toString().equals("")))
				finalSum = Integer.parseInt(bitSum.toString(), 2);
			imgData[i] = finalSum;
		}

		return this.scaleImg(imgData);
	}

	public double[] Or(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			String aBin = Integer.toBinaryString(Math.abs((int)(aImg[i]*100)));
			String bBin = Integer.toBinaryString(Math.abs((int)(bImg[i]*100)));
			StringBuffer bitSum = new StringBuffer();
			for (int j = 0; j < Math.min(aBin.length(),bBin.length()); j++)
			{
				if (aBin.charAt(j) == '1' || bBin.charAt(j) == '1')
					bitSum.append('1');
				else
					bitSum.append('0');
			}
			int finalSum = 0;
			if (!(bitSum.toString().equals("")))
				finalSum = Integer.parseInt(bitSum.toString(), 2);
			imgData[i] = finalSum;
		}

		return this.scaleImg(imgData);
	}

	public double[] Xor(Object a, Object b)
	{
		double imgData[] = new double[this.width*this.height];
		double aImg[] = this.argToImg(a);
		double bImg[] = this.argToImg(b);

		for (int i = 0; i < (this.width*this.height); i++)
		{
			String aBin = Integer.toBinaryString(Math.abs((int)(aImg[i]*100)));
			String bBin = Integer.toBinaryString(Math.abs((int)(bImg[i]*100)));
			StringBuffer bitSum = new StringBuffer();
			for (int j = 0; j < Math.min(aBin.length(),bBin.length()); j++)
			{
				if ((aBin.charAt(j) == '1' && bBin.charAt(j) == '0') || (aBin.charAt(j) == '0' && bBin.charAt(j) == '1'))
					bitSum.append('1');
				else
					bitSum.append('0');
			}
			int finalSum = 0;
			if (!(bitSum.toString().equals("")))
				finalSum = Integer.parseInt(bitSum.toString(), 2);
			imgData[i] = finalSum;
		}

		return this.scaleImg(imgData);
	}

	/**** Utility Functions ****/

	/* Convert object in, which may by a scalar,
	 * x,y or r,a coordinate, or other image,
	 * into image and return it. */
	private static double[] argToImg(Object in)
	{
		double newImg[] = new double[ImgProcessing.width*ImgProcessing.height];

		if (in.getClass().getName().equals("java.lang.String") && Character.isDigit(((String)in).toCharArray()[((String)in).length()-1]))
			newImg = ImgProcessing.scalarToImg(Double.parseDouble((String)in));

		if (in.getClass().getName().equals("java.lang.String") && 
			(((String)in).trim().equals("x") || ((String)in).trim().equals("y") ||
		         ((String)in).trim().equals("r") || ((String)in).trim().equals("a")))
			newImg = ImgProcessing.varToImg((String)in);

		if (in.getClass().getName().toCharArray()[0] == '[')
			newImg = (double[])in;

		return newImg;
	}

	/* Create an image from scalar value scalar */
	private static double[] scalarToImg(double scalar)
	{
		double newImg[] = new double[ImgProcessing.width*ImgProcessing.height];
		for (int i = 0; i < newImg.length; i++)
			newImg[i] = scalar;

		return newImg;
	}

	/* Take "x" or "y" as cartesian coordinates or "r" or "a"
	 * as polar coordinates and return image data with values
	 * set to given coordinate variable (gradient).
	 */
	private static double[] varToImg(String var)
	{
		double newImg[] = new double[ImgProcessing.width*ImgProcessing.height];
		
		double xCur = (double)ImgProcessing.domain[0][0];
		double yCur = (double)ImgProcessing.domain[1][1];
		double xInter = ((double)ImgProcessing.domain[0][1]-ImgProcessing.domain[0][0])/
				(double)ImgProcessing.width;
		double yInter = ((double)ImgProcessing.domain[1][1]-ImgProcessing.domain[1][0])/(double)ImgProcessing.height;

		if (var.equals("x"))
		{
			int index = 0;
			for (int i = 0; i < ImgProcessing.height; i++)
			{
				for (int j = 0; j < ImgProcessing.width; j++)
				{
					newImg[index++] = xCur;
					xCur += xInter;
				}
				xCur = ImgProcessing.domain[0][0];
			}
		}
		else if (var.equals("y"))
		{
			int index = 0;
			for (int i = 0; i < ImgProcessing.height; i++)
			{
				for (int j = 0; j < ImgProcessing.width; j++)
				{
					newImg[index++] = yCur;
				}

				yCur -= yInter;
			}
		}
		else if (var.equals("r"))
		{
			int index = 0;
			for (int i = 0; i < ImgProcessing.height; i++)
			{
				for (int j = 0; j < ImgProcessing.width; j++)
				{
					double dist = Math.sqrt((xCur*xCur)+(yCur*yCur));
					newImg[index++] = dist;
					xCur += xInter;
				}
				yCur -= yInter;
				xCur = ImgProcessing.domain[0][0];
			}
		}
		else if (var.equals("a"))
		{
			int index = 0;
			for (int i = 0; i < ImgProcessing.height; i++)
			{
				for (int j = 0; j < ImgProcessing.width; j++)
				{
					double angle = Math.atan((double)yCur)/(double)xCur;
					newImg[index++] = angle;
					xCur += xInter;
				}
				yCur -= yInter;
				xCur = ImgProcessing.domain[0][0];
			}
		}

		return newImg;

	}

 	/* Return a new image where all the values have
	 * been scaled between 0 and 1.0. */
	private static double[] scaleImg(double[] imgData)
	{
		double min = imgData[0];
		double max = imgData[0];

		for (int i = 1; i < imgData.length; i++)
		{
			if (imgData[i] < min)
				min = imgData[i];
			if (imgData[i] > max)
				max = imgData[i];
		}

		double newImg[] = new double[imgData.length];
		for (int i = 0; i < imgData.length; i++)
		{
			double x = 0.0;
			if (!((max-min) == 0))
				x = (double)(imgData[i] - min) / (max-min);
			newImg[i] = x;
		}

		return newImg;
	}

	public static void main(String[] args)
	{
		int width = 400;
		int height = 400;

		ImgProcessing ip = new ImgProcessing(width, height);
		double image[] = ip.varToImg("r");
		image = ip.scaleImg(image);

		for (int i = 0; i < width*height; i++)
		{
			image[i] = Math.ceil(image[i]*255);
		}

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();

		g.setBackground(Color.WHITE);
		g.clearRect(0,0, bi.getWidth(), bi.getHeight());
	
		int index = 0;
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				g.setColor(new Color((int)image[index], (int)image[index],(int) image[index]));
				g.drawRect(i,j, 1,1);
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
