# Contains image processing functions used in PicEvolve.
# Each function should be able to take as arguments
# scalars, x,y or r,a coordinates, or other images as lists
# of scalars, and return an image as a list of scalars.
# These scalar lists are converted to pixel color values
# in PicEvolve using GimpGradient.

import math
import random
import time
from PIL import Image, ImageFilter
from GimpGradient import GimpGradient

class ImgProcessing:

	# Domain of X and Y values 
	Domain = [(-1,1),(-1,1)]

	def __init__(self, imgWidth, imgHeight, gradient):

		self.width = imgWidth
		self.height = imgHeight
		self.colorMap = gradient

	def add(self, a, b):

		imgData = []
		aImg, bImg = self.argsToImgs(a, b)

		for i in range(self.width*self.height):
			imgData.append(aImg[i] + bImg[i])

		return self.scaleImg(imgData)

	def sub(self, a, b):

		imgData = []
		aImg, bImg = self.argsToImgs(a, b)

		for i in range(self.width*self.height):
			imgData.append(aImg[i] - bImg[i])

		return self.scaleImg(imgData)
	
	def mul(self, a, b):
		
		imgData = []
		aImg,bImg = self.argsToImgs(a, b)

		for i in range(self.width*self.height):
			imgData.append(aImg[i] * bImg[i])

		return self.scaleImg(imgData)

	def div(self, a, b):

		imgData = []
		aImg, bImg = self.argsToImgs(a, b)

		for i in range(self.width*self.height):
			if not bImg[i] == 0:
				imgData.append(float(aImg[i])/float(bImg[i]))
			else:
				imgData.append(0)

		return self.scaleImg(imgData)

	def mod(self, a, b):

		imgData = []
		aImg, bImg = self.argsToImgs(a, b)

		for i in range(self.width*self.height):
			if not bImg[i] == 0:
				imgData.append(float(aImg[i]) % float(bImg[i]))
			else:
				imgData.append(0)

		return self.scaleImg(imgData)

	def rnd(self, a, b):

		imgData = []
		aImg, bImg = self.argsToImgs(a, b)

		for i in range(self.width*self.height):
			imgData.append(round(aImg[i],int(bImg[i])))

		return self.scaleImg(imgData)

	def minimum(self, a, b):

		imgData = []
		aImg, bImg = self.argsToImgs(a, b)

		for i in range(self.width*self.height):
			imgData.append(min(aImg[i], bImg[i]))

		return self.scaleImg(imgData)

	def maximum(self, a, b):

		imgData = []
		aImg, bImg = self.argsToImgs(a, b)

		for i in range(self.width*self.height):
			imgData.append(max(aImg[i], bImg[i]))

		return self.scaleImg(imgData)

	def absolute(self, a):

		imgData = []
		aImg, bImg = self.argsToImgs(a, None)

		for i in range(self.width*self.height):
			imgData.append(abs(aImg[i]))

		return self.scaleImg(imgData)

	def expt(self, a, b):

		imgData = []

		aImg, bImg = self.argsToImgs(a,b)
		for i in range(self.width*self.height):
			# Negative number cannot be raised to fractional power
			# Zero cannot be raised to negative power.
			if not (aImg[i] == 0 and bImg[i] < 0):
				if aImg[i] < 0:
					bImg[i] = int(bImg[i])
				imgData.append(pow(aImg[i], bImg[i]))
			else:
				imgData.append(0)

		return self.scaleImg(imgData)

	def logarithm(self, a, b):

		imgData = []

		aImg, bImg = self.argsToImgs(a,b)
		for i in range(self.width*self.height):
			# Neither argument can be negative and b can't be 1
			if aImg[i] > 0 and bImg[i] > 0 and not bImg[i] == 1:
				imgData.append(math.log(aImg[i], bImg[i]))
			else:
				imgData.append(0)

		return self.scaleImg(imgData)

	def sine(self, a):

		imgData = []

		aImg, bImg = self.argsToImgs(a, None)
		for i in range(self.width*self.height):
			imgData.append(math.sin(aImg[i]))

		return self.scaleImg(imgData)

	def asine(self, a):

		imgData = []
		aImg, bImg = self.argsToImgs(a, None)

		for i in range(self.width*self.height):
			if aImg[i] < -1 or aImg[i] > 1:
				imgData.append(0)
			else:
				imgData.append(math.asin(aImg[i]))

		return self.scaleImg(imgData)

	def cosine(self, a):

		imgData = []

		aImg, bImg = self.argsToImgs(a, None)
		for i in range(self.width*self.height):
			imgData.append(math.cos(aImg[i]))

		return self.scaleImg(imgData)

	def acosine(self, a):

		imgData =[]

		aImg,bImg = self.argsToImgs(a, None)
		for i in range(self.width*self.height):
			if aImg[i] < -1 or aImg[i] > 1:
				imgData.append(0)
			else:
				imgData.append(math.acos(aImg[i]))

		return self.scaleImg(imgData)

	def tangent(self, a):

		imgData = []
		aImg,bImg = self.argsToImgs(a, None)
		for i in range(self.width*self.height):
			imgData.append(math.tan(aImg[i]))

		return self.scaleImg(imgData)

	def atangent(self, a):

		imgData = []
		aImg,bImg = self.argsToImgs(a, None)
		for i in range(self.width*self.height):
			imgData.append(math.atan(aImg[i]))

		return self.scaleImg(imgData)

	def And(self, a, b):

		imgData = []

		aImg, bImg = self.argsToImgs(a,b)
		for i in range(self.width*self.height):
			aBin = bin(int(aImg[i]*100))[2:]
			bBin = bin(int(bImg[i]*100))[2:]
			bitSum = ""
			for i in range(min(len(aBin),len(bBin))):
				if aBin[i] == bBin[i]:
					bitSum = bitSum + "1"
				else:
					bitSum = bitSum + "0"
			finalSum = 0
			if not bitSum == "":
				finalSum = eval("0b"+ bitSum)
			imgData.append(finalSum)

		return self.scaleImg(imgData)


	def Or(self, a, b):

		imgData = []

		aImg, bImg = self.argsToImgs(a,b)
		for i in range(self.width*self.height):
			aBin = bin(int(aImg[i]*100))[2:]
			bBin = bin(int(bImg[i]*100))[2:]
			bitSum = ""
			for i in range(min(len(aBin),len(bBin))):
				if aBin[i] == "1" or bBin[i] == "1":
					bitSum = bitSum + "1"
				else:
					bitSum = bitSum + "0"
			finalSum = 0
			if not bitSum == "":
				finalSum = eval("0b"+ bitSum)
			imgData.append(finalSum)

		return self.scaleImg(imgData)

	def Xor(self, a, b):
		imgData = []

		aImg, bImg = self.argsToImgs(a,b)
		for i in range(self.width*self.height):
			aBin = bin(int(aImg[i]*100))[2:]
			bBin = bin(int(bImg[i]*100))[2:]
			bitSum = ""
			for i in range(min(len(aBin),len(bBin))):
				if (aBin[i] == "1" and bBin[i] == "0") or (aBin[i] == "0" and bBin[i] == "1"):
					bitSum = bitSum + "1"
				else:
					bitSum = bitSum + "0"
			finalSum = 0
			if not bitSum == "":
				finalSum = eval("0b"+ bitSum)
			imgData.append(finalSum)

		return self.scaleImg(imgData)

	def noise(self, a, b):
		"""Perlin noise generator with a passed as seed and b
		   as scale."""

		imgData = []

		# First we need to convert a and b into scalars.
		a = self.argToScalar(a)
		b = self.argToScalar(b)
		a = a*10
		b = b*10

		# Now we can seed the PRN generator with argument
		from perlin import Noise2d
		n = Noise2d(a)
		scale = b 
		for x in range(self.width):
			for y in range (self.height):
				val = 0
				if not b == 0.0:
					val = int((n.noise(x/b,y/b)+1)*128)
				imgData.append(val)

		random.seed(time.time())
		return self.scaleImg(imgData)

	def blur(self, a, b):
		"""Blur image a b * 10 times."""

		aImg, bImg = self.argsToImgs(a, b)
		bNum = self.imgToScalar(bImg)

		img = Image.new("RGB",(self.width, self.height))
		pix = img.load()

		count = 0
		for y in range(self.height):
			for x in range(self.width):
				pix[x, y] = self.colorMap.colorRGB(aImg[count])
				count += 1

		for i in range(int(bNum*10)):
			img = img.filter(ImageFilter.BLUR)

		imgData = img.getdata()
		newImg = []
		for r,g,b in imgData:
			newImg.append((r+g+b)/3.0)

		return self.scaleImg(newImg)

	def edgeEnhance(self, a, b):

		aImg, bImg = self.argsToImgs(a, b)
		bNum = self.imgToScalar(bImg)

		img = Image.new("RGB",(self.width, self.height))
		pix = img.load()

		count = 0
		for y in range(self.height):
			for x in range(self.width):
				pix[x, y] = self.colorMap.colorRGB(aImg[count])
				count += 1

		for i in range(int(bNum*10)):
			img = img.filter(ImageFilter.EDGE_ENHANCE)

		imgData = img.getdata()
		newImg = []
		for r,g,b in imgData:
			newImg.append((r+g+b)/3.0)

		return self.scaleImg(newImg)

	def emboss(self, a, b):

		aImg, bImg = self.argsToImgs(a, b)
		bNum = self.imgToScalar(bImg)

		img = Image.new("RGB",(self.width, self.height))
		pix = img.load()

		count = 0
		for y in range(self.height):
			for x in range(self.width):
				pix[x, y] = self.colorMap.colorRGB(aImg[count])
				count += 1

		for i in range(int(bNum*10)):
			img = img.filter(ImageFilter.EMBOSS)

		imgData = img.getdata()
		newImg = []
		for r,g,b in imgData:
			newImg.append((r+g+b)/3.0)

		return self.scaleImg(newImg)

	def mandel(self, a, b):
		"""Mandelbrot fractal with a and b being
		   real and imaginary components."""
		
		aImg, bImg = self.argsToImgs(a, b)
		aNum = self.imgToScalar(aImg)
		bNum = self.imgToScalar(bImg)

		botLeft = complex(aNum,bNum)
		topRight = complex(botLeft.real+3.0,botLeft.imag+3.0)
		xStep = (topRight.real-botLeft.real)/float(self.width)
		yStep = (topRight.imag-botLeft.imag)/float(self.height)

		rawImg = []
		xCur = botLeft.real
		yCur = botLeft.imag
		for y in range(self.height):
			for x in range(self.width):

				color = (0,0,0)
				c = complex(xCur, yCur)
				z = c
				for i in range(50):
					z = z*z + c
					if (z.real*z.real + z.imag*z.imag) > 4:
						color = (i,i,i)
						break
				xCur += xStep
				rawImg.append(color[0])

			yCur += yStep
			xCur = botLeft.real

		return self.scaleImg(rawImg)

	def lerp(self, a, b, c):
		"""Do a linear interpolation with a and b
		   using c as the interpolation factor."""
	
		aImg, bImg = self.argsToImgs(a, b)
		cImg, dummy = self.argsToImgs(c, None)
		cNum = self.imgToScalar(cImg)

		imgData = []
		for i in range(self.width*self.height):
			l = cNum*aImg[i] + (1.0 - cNum)*bImg[i]
			imgData.append(l)

		return self.scaleImg(imgData)


#############################################################################
#                             Uility functions                              #
#############################################################################

	def imgToScalar(self, img):
		"""Return the average value of scalars in img."""
	
		imgSum = 0
		for pixel in img:
			imgSum += pixel
	
		if len(img) == 0:
			return 0
		else:
			return float(imgSum) / float(len(img))

	def varToImg(self, a):
		"""Take "x" or "y" as cartesian coordinates or "r" or "a"
		   as polar coordinates and return image data with values
		   set to given coordinate variable (gradient)."""

		imgData = []
	
		xCur = self.Domain[0][0]
		yCur = self.Domain[1][1]
		xInter = float(self.Domain[0][1]-self.Domain[0][0]) / float(self.width)
		yInter = float(self.Domain[1][1]-self.Domain[1][0]) / float(self.height)
	
		if a == 'x':
			for i in range(self.height):
				for j in range(self.width):
					imgData.append(xCur)
					xCur += xInter
				xCur = self.Domain[0][0]

		elif a == 'y':
			for i in range(self.height):
				for j in range(self.width):
					imgData.append(yCur)
				yCur -= yInter
		elif a == 'r':
			for i in range(self.height):
				for j in range(self.width):
					dist = math.sqrt((xCur*xCur)+(yCur*yCur))
					imgData.append(dist)
					xCur += xInter
				yCur -= yInter
				xCur = self.Domain[0][0]
		elif a == 'a':
			for i in range(self.height):
				for j in range(self.width):
					angle = math.atan(float(yCur)/float(xCur))
					imgData.append(angle)
					xCur += xInter
				yCur -= yInter
				xCur = self.Domain[0][0]

		return imgData

	def scaleImg(self, imgData):
		"""Return a new image where all the values in
		   imgData have been scaled between 0 and 1.0."""
	
		imin = min(imgData)
		imax = max(imgData)

		newImg = []
		for pixel in imgData:
			x = 0
			if not (imax-imin) == 0:
				x = (pixel - imin)/float((imax - imin))
			newImg.append(x)

		return newImg
		

	def argToScalar(self, a):
		"""Convert the given argument, which can be a float,
		   an x,y or r,a coordinate, or an image, into a scalar."""

		if type(a) == list:
			a = self.imgToScalar(a)
		if type(a) == str and a[-1].isdigit():
			a = float(a)
		if type(a) == str and (a == 'x' or a =='y' or a == 'r' or a == 'a'):
			if a == 'x':
				a = 0
			elif a == 'y':
				a = 1
			elif a == 'r':
				a = 2
			elif a == 'a':
				a = 3

		return a

	def scalarToImg(self, a):
		"""Create an image from scalar value a."""
		
		imgData = []

		for i in range(self.width*self.height):
			imgData.append(a)

		return imgData

	def argsToImgs(self, a, b):
		"""Return tuple of images created from a and b,
		   which can be scalars, x,y or r,a coordinates, or images."""

		aImg = []
		bImg = []

		if type(a) == list:
			aImg = a
		if type(b) == list:
			bImg = b

		if type(a) == str and a[-1].isdigit():
			aImg = self.scalarToImg(float(a))
		if type(b) == str and b[-1].isdigit():
			bImg = self.scalarToImg(float(b))
	
		if type(a) == str and (a == 'x' or a =='y' or a == 'r' or a == 'a'):
			aImg = self.varToImg(a)
		if type(b) == str and (b == 'x' or b == 'y' or b == 'r' or b == 'a'):
			bImg = self.varToImg(b)

		return aImg, bImg



if __name__ == "__main__":

	img = Image.new("RGB",(300,300),(255,255,255))
	g = GimpGradient("./gradients/Incandescent.ggr")
