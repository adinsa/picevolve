# PicEvolve- Use genetic programming to evolve a population
# of s-expressions that contain procedural information for
# image synthesis. Based on Karl Sims' paper:
# http://www.karlsims.com/papers/siggraph91.html
#
# Images are represented in ImgProcessing as lists of scalars,
# which are be mapped to color values using GimpGradient.
#
# Currently functions with arities of one, two, and three
# are supported.

import sys
import random
import pyparsing
from pyparsing import ParseException
from types import NoneType
from ImgProcessing import *
from GimpGradient import GimpGradient
from sexpParser import sexp
from Node import Node
from PIL import Image

class PicEvolve:

	# Map of all image processing functions to the # of arguments they use.
	FuncSet = {"add":2,"sub":2,"mul":2,"div":2,"mod":2,"rnd":2,
		   "minimum":2,"maximum":2,"absolute":1,"expt":2,"logarithm":2,
		   "sine":1,"cosine":1,"tangent":1,"asine":1,"acosine":1,"atangent":1,
		   "And":2,"Or":2,"Xor":2,"noise":2, "blur":2, "edgeEnhance":2,
		   "emboss":2,"mandel":2,"lerp":3}

	# Default mutation rates used unless others are specified in config file through
	# PicEvolveGui's preferences.
	# The order of the rates are: global mutation rate, 5 relative rates for function node
	# mutation types, 4 relative rates for scalar node mutation types, and 3 for variable node
	# mutation types.
	DefaultMutationRates = [0.4, 0.2, 0.2, 0.2, 0.2, 0.2, 0.25, 0.25, 0.25, 0.25, 0.3, 0.3, 0.3]

	DEBUG = 0 

	def __init__(self, populationSize, (picWidth, picHeight), shouldInit, gradientFile="./gradients/Sunrise.ggr",
			mutRates = DefaultMutationRates):
		
		self.width = picWidth
		self.height = picHeight

		self.mutRates = mutRates

		# Initialize color map
		self.colorMap = GimpGradient(gradientFile)

		# Initialize the population with a list
		# of random s-expression strings.
		self.pop = []
		if shouldInit == False:
			for i in range(populationSize):
				self.pop.append("")
		else:
			print "Initial population is being generated..."
			for i in range(populationSize):
				self.pop.append(self.randomSExpr())
				print "Image " + str(i) + ": " + self.pop[i]
				self.createImage(self.pop[i], "img" + str(i) + ".png", (self.width,self.height))

	def randomSExpr(self):
		"""Return a randomly generated S-expression using the
		   provided function set."""
		
		# Expression is created by choosing a random function,
		# and then randomly generating required arguments.
		sexpStr = "("
		func = random.choice(self.FuncSet.keys())
		sexpStr += func + " "
		args = self.FuncSet[func]
		# Each argument is randomly chosen to be either
		# 1.) A random scalar value
		# 2.) Cartesian x,y or polar r,a pixel coordinates
		# 3.) Another s-expression
		for i in range(args):
			argchoice = random.randint(1,3)
			if argchoice == 1:
				sexpStr += str(random.random()) + " "
			elif argchoice == 2:
				varChoice = random.randint(1,4)
				if varChoice == 1:
					sexpStr += "x "
				elif varChoice == 2:
					sexpStr += "y "
				elif varChoice == 3:
					sexpStr += "r "
				elif varChoice == 4:
					sexpStr += "a "
			elif argchoice == 3:
				sexpStr += self.randomSExpr()
				
		sexpStr += ") "
		return sexpStr

	def mutate(self, parent):
		"""Return a mutation of parent s-expression."""

		# Use pyparsing to parse the s-expression into
		# a nested list.
		sList = sexp.parseString(parent)[0] 

		#print "Parent: ", parent

		# Create a parse tree of expression
		# and mutate it.
		root = self.createParseTree(sList)
		self.mutateTree(root)
		
		# Return the s-expression represented in
		# root's tree.
		sexpr = self.treeToSexpr(root)
		#print "Mutation: ", sexpr
		return sexpr

	def mutateTree(self, root):
		"""Traverse the parse tree and perform
		   mutations with the given probability."""
	
		rand = random.random()

		if rand < self.mutRates[0]:
			# If current node is a function
			if type(root.value) == str and self.FuncSet.has_key(root.value):

				mChoice = random.random()
				ranges = []
				ranges.append((0, self.mutRates[1]))
				ranges.append((ranges[0][1],ranges[0][1]+self.mutRates[2]))
				ranges.append((ranges[1][1],ranges[1][1]+self.mutRates[3]))
				ranges.append((ranges[2][1],ranges[2][1]+self.mutRates[4]))
				ranges.append((ranges[3][1],ranges[3][1]+self.mutRates[5]))

				if mChoice > ranges[0][0] and mChoice < ranges[0][1]:
					# Random expression
					sList = sexp.parseString(self.randomSExpr())[0]
					newExpr = self.createParseTree(sList)
					root.value = newExpr.value
					root.left = newExpr.left
					root.right = newExpr.right
					root.lFactor = newExpr.lFactor

					if self.DEBUG == 1:
						print "Func Node- random mutation"

				elif mChoice > ranges[1][0] and mChoice < ranges[1][1]:
					# Diff function
					functions = self.FuncSet.keys()
					functions.remove(root.value)
					func = random.choice(functions)
					# If the node and our new function take the same number of arguments
					if self.FuncSet[root.value] == self.FuncSet[func]:
						root.value = func
					# If the node takes one argument and our new function takes two
					elif self.FuncSet[root.value] == 1 and self.FuncSet[func] == 2:
						root.value = func
						root.right = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.right.parent = root
					# If the node takes one argument and our new function takes three
					elif self.FuncSet[root.value] == 1 and self.FuncSet[func] == 3:
						root.value = func
						root.right = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.right.parent = root
						root.lFactor = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.lFactor.parent = root
					# If the node takes two arguments and our new function takes one
					elif self.FuncSet[root.value] == 2 and self.FuncSet[func] == 1:
						root.value = func
						LorR = random.randint(0,1)
						if LorR == 0:
							root.right = None
						else:
							root.left = root.right
							root.right = None
					# If the node takes two arguments and our new function takes three
					elif self.FuncSet[root.value] == 2 and self.FuncSet[func] == 3:
						root.value = func
						root.lFactor = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.lFactor.parent = root
					# If the node takes three arguments and our new function takes one
					elif self.FuncSet[root.value] == 3 and self.FuncSet[func] == 1:
						root.value = func
						c = random.randint(1,3)
						if c == 1: # Use left child
							root.right = None
							root.lFactor = None
						elif c == 2: # Use right child
							root.left = root.right
							root.right = None
							root.lFactor = None
						elif c == 3: # Use lFactor
							root.left = root.lFactor
							root.right = None
							root.lFactor = None
					# If the node takes three arguments and our new function takes two
					elif self.FuncSet[root.value] == 3 and self.FuncSet[func] == 2:
						root.value = func
						c = random.randint(1,3)
						if c == 1: # Use left and right
							root.lFactor = None
						elif c == 2: # Use left and lFactor
							root.right = root.lFactor
							root.lFactor = None
						elif c == 3: # Use right and lFactor
							root.left = root.lFactor
							root.lFactor = None

					if self.DEBUG == 1:
						print "Func Node- change function mutation"

				elif mChoice > ranges[2][0] and mChoice < ranges[2][1]:
					# become arg to new func
					newFunc = random.choice(self.FuncSet.keys())
					if self.FuncSet[newFunc] == 1:
						root.left = Node(root.value, root, root.left, root.right, lerp=root.lFactor)
						root.right = None
						root.lFactor = None
						root.value = newFunc
					elif self.FuncSet[newFunc] == 2:
						root.left = Node(root.value,root,root.left,root.right, lerp=root.lFactor)
						root.right = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.right.parent = root
						root.lFactor = None
						root.value = newFunc
					elif self.FuncSet[newFunc] == 3:
						root.left = Node(root.value,root,root.left,root.right, lerp=root.lFactor)
						root.right = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.lFactor = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.right.parent = root
						root.lFactor.parent = root
						root.value = newFunc

					if self.DEBUG == 1:
						print "Func Node- become arg of new func mutation"

				elif mChoice > ranges[3][0] and mChoice < ranges[3][1]:
					# become value of arg
					if self.FuncSet[root.value] == 1:
						root.value = root.left.value
						leftTemp = root.left
						root.left = root.left.left
						if not type(root.left) == NoneType:
							root.left.parent = root
						root.right = leftTemp.right
						if not type(root.right) == NoneType:
							root.right.parent = root
						root.lFactor = leftTemp.lFactor
						if not type(root.lFactor) == NoneType:
							root.lFactor.parent = root
					elif self.FuncSet[root.value] == 2:
						LorR = random.randint(0,1)
						if LorR == 0:
							root.value = root.left.value
							leftTemp = root.left
							root.left = root.left.left
							if not type(root.left) == NoneType:
								root.left.parent = root
							root.right = leftTemp.right
							if not type(root.right) == NoneType:
								root.right.parent = root
							root.lFactor = leftTemp.lFactor
							if not type(root.lFactor) == NoneType:
								root.lFactor.parent = root
						else:
							root.value = root.right.value
							rightTemp = root.right
							root.right = root.right.right
							if not type(root.right) == NoneType:
								root.right.parent = root
							root.left = rightTemp.left
							if not type(root.left) == NoneType:
								root.left.parent = root
							root.lFactor = rightTemp.lFactor
							if not type(root.lFactor) == NoneType:
								root.lFactor.parent = root

					elif self.FuncSet[root.value] == 3:
						c = random.randint(1,3)
						if c == 1:
							root.value = root.left.value
							leftTemp = root.left
							root.left = root.left.left
							if not type(root.left) == NoneType:
								root.left.parent = root
							root.right = leftTemp.right
							if not type(root.right) == NoneType:
								root.right.parent = root
							root.lFactor = leftTemp.lFactor
							if not type(root.lFactor) == NoneType:
								root.lFactor.parent = root
						elif c == 2:
							root.value = root.right.value
							rightTemp = root.right
							root.right = root.right.right
							if not type(root.right) == NoneType:
								root.right.parent = root
							root.left = rightTemp.left
							if not type(root.left) == NoneType:
								root.left.parent = root
							root.lFactor = rightTemp.lFactor
							if not type(root.lFactor) == NoneType:
								root.lFactor.parent = root
						elif c == 3:
							root.value = root.lFactor.value
							lFactorTemp = root.lFactor
							root.left = lFactorTemp.left
							if not type(root.left) == NoneType:
								root.left.parent = root
							root.right = lFactorTemp.right
							if not type(root.right) == NoneType:
								root.right.parent = root
							root.lFactor = lFactorTemp.lFactor
							if not type(root.lFactor) == NoneType:
								root.lFactor.parent = root

					if self.DEBUG == 1:
						print "Func Node- become value of arg mutation."

				elif mChoice > ranges[4][0] and mChoice < ranges[4][1]:
					# become copy of another node
					cur = root
					while not type(cur.parent) == NoneType:
						cur = cur.parent

					randomNode = self.randomNode(cur)
					curPar = pos = 0
					if not type(root.parent) == NoneType:
						curPar = root.parent
						if curPar.left == root:
							pos = 0
						elif curPar.right == root:
							pos = 1
						elif curPar.lFactor == root:
							pos = 2
					root = Node(randomNode.value,root.parent,self.copyTree(randomNode.left),self.copyTree(randomNode.right),lerp=self.copyTree(randomNode.lFactor))

					if not type(root.parent) == NoneType:
						if pos == 0:
							curPar.left = root
						elif pos == 1:
							curPar.right = root
						elif pos == 2:
							curPar.lFactor = root

					if not type(root.left) == NoneType:
						root.left.parent = root
					if not type(root.right) == NoneType:
						root.right.parent = root
					if not type(root.lFactor) == NoneType:
						root.lFactor.parent = root

					if self.DEBUG == 1:
						print "Func Node- become copy of another node"

			# If current node is a scalar
			elif type(root.value) == float:

				mChoice = random.random()
				ranges = []
				ranges.append((0, self.mutRates[6]))
				ranges.append((ranges[0][1], ranges[0][1]+self.mutRates[7]))
				ranges.append((ranges[1][1], ranges[1][1]+self.mutRates[8]))
				ranges.append((ranges[2][1], ranges[2][1]+self.mutRates[9]))

				if mChoice > ranges[0][0] and mChoice < ranges[0][1]:
					# Random expression
					sList = sexp.parseString(self.randomSExpr())[0]
					newExpr = self.createParseTree(sList)
					root.value = newExpr.value
					root.left = newExpr.left
					root.right = newExpr.right
					root.lFactor = newExpr.lFactor

					if self.DEBUG == 1:
						print "Scalar Node- random mutation"

				elif mChoice > ranges[1][0] and mChoice < ranges[1][1]:
					# adjust by random amount
					root.value += random.random()

					if self.DEBUG == 1:
						print "Scalar node- adjust by rand amount mutation."

				elif mChoice > ranges[2][0] and mChoice < ranges[2][1]:
					# become arg to new func
					newFunc = random.choice(self.FuncSet.keys())
					if self.FuncSet[newFunc] == 1:
						root.left = Node(root.value, root, root.left, root.right, lerp=root.lFactor)
						root.right = None
						root.lFactor = None
						root.value = newFunc
					elif self.FuncSet[newFunc] == 2:
						root.left = Node(root.value,root,root.left,root.right, lerp=root.lFactor)
						root.right = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.right.parent = root
						root.lFactor = None
						root.value = newFunc
					elif self.FuncSet[newFunc] == 3:
						root.left = Node(root.value,root,root.left,root.right, lerp=root.lFactor)
						root.right = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.lFactor = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.right.parent = root
						root.lFactor.parent = root
						root.value = newFunc

					if self.DEBUG == 1:
						print "Scalar Node- become arg of new func mutation"

				elif mChoice > ranges[3][0] and mChoice < ranges[3][1]:
					# become copy of another node
					cur = root
					while not type(cur.parent) == NoneType:
						cur = cur.parent

					randomNode = self.randomNode(cur)
					curPar = pos = 0
					if not type(root.parent) == NoneType:
						curPar = root.parent
						if curPar.left == root:
							pos = 0
						elif curPar.right == root:
							pos = 1
						elif curPar.lFactor == root:
							pos = 2
					root = Node(randomNode.value,root.parent,self.copyTree(randomNode.left),self.copyTree(randomNode.right),lerp=self.copyTree(randomNode.lFactor))

					if not type(root.parent) == NoneType:
						if pos == 0:
							curPar.left = root
						elif pos == 1:
							curPar.right = root
						elif pos == 2:
							curPar.lFactor = root

					if not type(root.left) == NoneType:
						root.left.parent = root
					if not type(root.right) == NoneType:
						root.right.parent = root
					if not type(root.lFactor) == NoneType:
						root.lFactor.parent = root

					if self.DEBUG == 1:
						print "Scalar Node- become copy of another node"

			# If current node is x,y or r,a variable
			elif type(root.value) == str and (root.value == 'x' or root.value == 'y' or root.value == 'r' or root.value == 'a'):
				
				mChoice = random.random()
				ranges = []
				ranges.append((0, self.mutRates[10]))
				ranges.append((ranges[0][1],ranges[0][1]+self.mutRates[11]))
				ranges.append((ranges[1][1],ranges[1][1]+self.mutRates[12]))

				if mChoice > ranges[0][0] and mChoice < ranges[0][1]:
					# Random expression
					sList = sexp.parseString(self.randomSExpr())[0]
					newExpr = self.createParseTree(sList)
					root.value = newExpr.value
					root.left = newExpr.left
					root.right = newExpr.right
					root.lFactor = newExpr.lFactor

					if self.DEBUG == 1:
						print "Var Node- random mutation"

				elif mChoice > ranges[1][0] and mChoice < ranges[1][1]:
					# become arg to new func
					newFunc = random.choice(self.FuncSet.keys())
					if self.FuncSet[newFunc] == 1:
						root.left = Node(root.value, root, root.left, root.right, lerp=root.lFactor)
						root.right = None
						root.lFactor = None
						root.value = newFunc
					elif self.FuncSet[newFunc] == 2:
						root.left = Node(root.value,root,root.left,root.right, lerp=root.lFactor)
						root.right = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.right.parent = root
						root.lFactor = None
						root.value = newFunc
					elif self.FuncSet[newFunc] == 3:
						root.left = Node(root.value,root,root.left,root.right, lerp=root.lFactor)
						root.right = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.lFactor = self.createParseTree(sexp.parseString(self.randomSExpr())[0])
						root.right.parent = root
						root.lFactor.parent = root
						root.value = newFunc

					if self.DEBUG == 1:
						print "Var Node- become arg of new func mutation"

				elif mChoice > ranges[2][0] and mChoice < ranges[2][1]:
					# become copy of another node
					cur = root
					while not type(cur.parent) == NoneType:
						cur = cur.parent

					randomNode = self.randomNode(cur)
					curPar = pos = 0
					if not type(root.parent) == NoneType:
						curPar = root.parent
						if curPar.left == root:
							pos = 0
						elif curPar.right == root:
							pos = 1
						elif curPar.lFactor == root:
							pos = 2
					root = Node(randomNode.value,root.parent,self.copyTree(randomNode.left),self.copyTree(randomNode.right),lerp=self.copyTree(randomNode.lFactor))

					if not type(root.parent) == NoneType:
						if pos == 0:
							curPar.left = root
						elif pos == 1:
							curPar.right = root
						elif pos == 2:
							curPar.lFactor = root

					if not type(root.left) == NoneType:
						root.left.parent = root
					if not type(root.right) == NoneType:
						root.right.parent = root
					if not type(root.lFactor) == NoneType:
						root.lFactor.parent = root

					if self.DEBUG == 1:
						print "Var Node- become copy of another node"

			return

		if not type(root.left) == NoneType:
			self.mutateTree(root.left)
		if not type(root.right) == NoneType:
			self.mutateTree(root.right)
		if not type(root.lFactor) == NoneType:
			self.mutateTree(root.lFactor)

	def copyTree(self, root):
		"""Make a copy of root's tree and return new tree's root."""

		if type(root) == NoneType:
			return None

		root = Node(root.value, None, self.copyTree(root.left), self.copyTree(root.right), lerp=self.copyTree(root.lFactor))
		if not type(root.left) == NoneType:
			root.left.parent = root
		if not type(root.right) == NoneType:
			root.right.parent = root
		if not type(root.lFactor) == NoneType:
			root.lFactor.parent = root

		return root

	def randomNode(self, root):
		"""Return a random node from root's parse tree."""

		treeList = self.treeToList(root)
		return random.choice(treeList)
	
	def treeToList(self, root):
		"""Return a list of all the nodes in tree."""
	
		tList = []
		tList.append(root)

		if not type(root.left) == NoneType:
			tList = tList + self.treeToList(root.left)
		if not type(root.right) == NoneType:
			tList = tList + self.treeToList(root.right)
		if not type(root.lFactor) == NoneType:
			tList = tList + self.treeToList(root.lFactor)

		return tList

	def treeToSexpr(self, root):
		"""Return the s-expression represented by tree."""
	
		if type(root) == NoneType:
			return
		# Base cases: root is a leaf (scalar,x/y/r/a value)
		if type(root.value) == float:
			return str(root.value)
		elif type(root.value) == str and (root.value == "x" or root.value == "y" or root.value == "r" or root.value == "a"):
			return root.value + " "

		# Recursive case: root is a function node
		sexpr = ""
		if type(root.value) == str and self.FuncSet.has_key(root.value):
			sexpr = "(" + root.value + " "
			if self.FuncSet[root.value] == 1:
				sexpr = sexpr + self.treeToSexpr(root.left) + ") "
			elif self.FuncSet[root.value] == 2:
				sexpr = sexpr + self.treeToSexpr(root.left) + " " + self.treeToSexpr(root.right) + ") " 
			elif self.FuncSet[root.value] == 3:
				sexpr = sexpr + self.treeToSexpr(root.left) + " " + self.treeToSexpr(root.right) + " " + self.treeToSexpr(root.lFactor)+ ") "

		return sexpr
		
	def createParseTree(self, sList):
		"""Create a parse tree from the parsed s-expression
		   represented in sList and return its root node."""
	
		# Base cases: sList is a scalar or variable
		if type(sList) == float:
			return Node(sList,None,None,None)
		elif type(sList) == str and (sList == 'x' or sList == 'y' or sList == 'r' or sList == 'a'):
			return Node(sList,None,None,None)

		root = Node(sList[0],None,None,None)

		# Recursive cases: sList is an s-expression
		if self.FuncSet[sList[0]] == 1:
			root.left = self.createParseTree(sList[1])
			root.left.parent = root
			root.right = None
		elif self.FuncSet[sList[0]] == 2:
			root.left = self.createParseTree(sList[1])
			root.left.parent = root
			root.right = self.createParseTree(sList[2])
			root.right.parent = root
		elif self.FuncSet[sList[0]] == 3:
			root.left = self.createParseTree(sList[1])
			root.left.parent = root
			root.right = self.createParseTree(sList[2])
			root.right.parent = root
			root.lFactor = self.createParseTree(sList[3])
			root.lFactor.parent = root

		return root

	def crossOver(self, sexpr1, sexpr2):
		"""Mate the two symbolic expression by performing
		   crossover."""
	
		sList1 = sexp.parseString(sexpr1)[0]
		sList2 = sexp.parseString(sexpr2)[0]

		tree1 = self.createParseTree(sList1)
		tree2 = self.createParseTree(sList2)

		rand1 = self.randomNode(tree1)
		rand2 = self.randomNode(tree2)

		r1Val = rand1.value
		rand1.value = rand2.value
		rand2.value = r1Val

		r1Left = rand1.left
		r1Right = rand1.right
		r1lFac = rand1.lFactor
		rand1.left = self.copyTree(rand2.left)
		rand1.right = self.copyTree(rand2.right)
		rand1.lFactor = self.copyTree(rand2.lFactor)
		if not type(rand1.left) == NoneType:
			rand1.left.parent = rand1
		if not type(rand1.right) == NoneType:
			rand1.right.parent = rand1
		if not type(rand1.lFactor) == NoneType:
			rand1.lFactor.parent = rand1

		rand2.left = self.copyTree(r1Left)
		rand2.right = self.copyTree(r1Right)
		rand2.lFactor = self.copyTree(r1lFac)
		if not type(rand2.left) == NoneType:
			rand2.left.parent = rand2
		if not type(rand2.right) == NoneType:
			rand2.right.parent = rand2
		if not type(rand2.lFactor) == NoneType:
			rand2.lFactor.parent = rand2

		return self.treeToSexpr(tree1), self.treeToSexpr(tree2)
	
	def createImage(self, sexpr, filename, (picWidth, picHeight)):
		"""Use the image processing functions in ImgProcessing
		   to create an image from the procedural information
		   contained in the s-expression."""
		
		img = Image.new("RGB",(picWidth,picHeight),(255,255,255))
		ip = ImgProcessing(picWidth,picHeight,self.colorMap)

		# Split s-expression into list of tokens
		sList = sexpr.replace("(","").replace(")","").split()

		sList.reverse()

		while len(sList) > 1:
			
			for index,token in enumerate(sList):
				if type(token) == str and self.FuncSet.has_key(token):
					if self.FuncSet[token] == 1:
						rawImage = getattr(ip,token)(sList[index-1])
						sList = sList[:index-1] + [rawImage] + sList[index+1:]
						break
					elif self.FuncSet[token] == 2:
						rawImage = getattr(ip,token)(sList[index-1],sList[index-2])
						sList = sList[:index-2] + [rawImage] + sList[index+1:] 
						break
					elif self.FuncSet[token] == 3:
						rawImage = getattr(ip,token)(sList[index-1],sList[index-2],sList[index-3])
						sList = sList[:index-3] + [rawImage] + sList[index+1:]
						break

		rawImage = sList[0]
		imgData = []
		for scalar in rawImage:
			imgData.append(self.colorMap.colorRGB(scalar))
		img.putdata(imgData)
		img.save(filename)


if __name__ == "__main__":

	POPULATION = 100
	WIDTH = 20
	HEIGHT = 20
	random.seed()

	clsexpr = ""
	pEvolver = None
	if len(sys.argv) == 2:
		clsexpr = sys.argv[1]
		pEvolver = PicEvolve(POPULATION,(WIDTH,HEIGHT),False)
	else:
		pEvolver = PicEvolve(POPULATION,(WIDTH,HEIGHT),True)

	while (True):
		parent = raw_input("Enter parent, 'q' (quit), or 'cl' (command line): ")
		if parent == 'q':
			break
		else:
			pSexpr = ""
			if parent == "cl":
				pSexpr = clsexpr
			else:
				pSexpr = pEvolver.pop[int(parent)]
			for i in range(len(pEvolver.pop)):
				mutation = None
				while True:
					try:
						mutation = pEvolver.mutate(pSexpr)
						mList = sexp.parseString(mutation)[0]
						if type(mList) == pyparsing.ParseResults:
							break
						else:
							continue
					except pyparsing.ParseException:
						pass
				pEvolver.pop[i] = mutation
				print "Creating image " + str(i) + "..."
				pEvolver.createImage(pEvolver.pop[i], str(i) + ".png", (WIDTH,HEIGHT))
