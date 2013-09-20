from math import *
from PIL import Image
import random

def drop(t):
    t = abs(t)
    return 1.0 - t*t*t*(t*(t*6-15)+10)

def Q(u, v):
    return drop(u) * drop(v)

def dot(A, B):
    return A[0]*B[0] + A[1]*B[1] 

class Noise2d(object):

    def noise(self, x, y):

        cell = (floor(x), floor(y))

        sum = 0.0
        for n in ((0,0), (0,1), (1,0), (1,1)):
            i,j = (cell[0] + n[0], cell[1] + n[1])
            u,v = (x-i, y-j)
	    grad = [0,0]
	    if not int(i) > (len(self.P) - 1):
            	index = self.P[int(i)]
            	index = self.P[(index + int(j))%len(self.P)] 
            	grad = self.G[index % len(self.G)]
            sum += Q(u, v) * dot(grad, (u, v)) 

        return max(min(sum, 1.0),-1.0)

    def drawIt(self, fileName, scale=100.0, size=None):
        size = size if size else (256, 256)
        im = Image.new('L', size)
        for x in range(size[0]):
            for y in range(size[1]):
                im.putpixel((x,y), int((self.noise(x/scale,y/scale)+1)*128))
                
	im.save(fileName)

    def __init__(self, seed):
        
	random.seed(seed)
        self.G = []
        length = None
        while len(self.G) < 256:
            self.G.append(None)
            while True:
                self.G[-1] = [random.random()*2-1, random.random()*2-1]
                length = sqrt(self.G[-1][0]**2 + self.G[-1][1]**2)
                if length < 1: break
            # Normalize
            self.G[-1][0] /= length
            self.G[-1][1] /= length

        self.P = range(256)
        random.shuffle(self.P)

if __name__ == "__main__":
    n = Noise2d(3)
    n.drawIt("pnoise.png",size=(200,200))
