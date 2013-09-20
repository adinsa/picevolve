# Node class used to build parse tree in PicEvolve

class Node:

	def __init__(self, val, par, leftChild, rightChild, lerp=None):

		self.value = val
		self.parent = par
		self.left = leftChild
		self.right = rightChild
		self.lFactor = lerp

