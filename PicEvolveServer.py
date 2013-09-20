# This server maintains a pickled dictionary that maps the population of s-expressions
# to the number of times they have been voted on. The clients may request
# a number of s-expressions or vote on an s-expression by sending it back to the server.
# When the number of votes have met VOTE_THRESHOLD, the population is regenerated
# with mutations of the most popular image.
#
# Amar Dinsa 2010

import socket, SocketServer
SocketServer.TCPServer.allow_reuse_address = True
import pickle
import PicEvolve
import pyparsing
from sexpParser import sexp
import random

HOST, PORT = "localhost", 9999
POPULATION = 10
VOTE_THRESHOLD = 5
DB_FILE = "./.picevolvedb"

class PicEvolveServerHelper:

	"""This class encapsulates access to DB_FILE, which contains the population,
	   as well as an instance of PicEvolve for initializing and mutating expressions."""

	def __init__(self):
		
		self.picEvolver = PicEvolve.PicEvolve(0, (0,0), False)

	def initialize(self, populationSize):
		"""Initialize a random population of given size."""

		self.popSize = populationSize
		for i in range(self.popSize):
			self.picEvolver.pop.append(self.picEvolver.randomSExpr())

		# Save population list to database file as a dictionary
		# mapping expressions to vote counts.
		popDict = {}
		for member in self.picEvolver.pop:
			popDict[member.strip()] = 0
		f = open(DB_FILE, "w")
		pickle.dump(popDict, f)
		f.close()

	def getMember(self, member):
		"""Try to return the given member of the population"""

		popDict = None
		try:
			f = open(DB_FILE, "r")
			popDict = pickle.load(f)
			f.close()
		except:
			print "Error reading " + DB_FILE

		return popDict.keys[member]
	
	def getMutation(self, pSexpr):
		"""Return a mutation of parent s-expression pSexpr"""

		while (True):

			try:
				mutation = self.picEvolver.mutate(pSexpr)
				mList = sexp.parseString(mutation)[0]
				if type(mList) == pyparsing.ParseResults:
					break
				else:
					continue
			except pyparsing.ParseException:
				pass

		return mutation

class PicEvolveTCPHandler(SocketServer.BaseRequestHandler):
	"""
	The RequestHandler class for our server.
	
	It is instantiated once per connection to the server, and must
	override the handle() method to implement communication to the
	client.
	"""

	def handle(self):
    		# self.request is the TCP socket connected to the client
    		self.data = self.request.recv(1024).strip()

		self.popDict = {}
		try:
			f = open(DB_FILE, "r")
			self.popDict = pickle.load(f)
			f.close()
		except:
			print "Error opening " + DB_FILE

		# First case: The user sends the number of images they want.
		if self.data.isdigit():
			random.seed()
			popSize = int(self.data)
        		print "%s requested %d images" % (self.client_address[0], popSize)
			popString = ""
			pop = self.popDict.keys()
			for i in range(popSize):
				randMember = random.choice(pop)
				if not i == (popSize-1):
					popString += randMember + "\n"
				else:
					popString += randMember
			
    			self.request.send(popString)

		# Second case: The user votes on an image by sending its expression back
		else:
			if self.data.strip() in self.popDict.keys():
				self.popDict[self.data.strip()] += 1

				try:
					f = open(DB_FILE, "w")
					pickle.dump(self.popDict, f)
					f.close()
				except:
					print "Cannot write to " + DB_FILE

				print "%s voted for %s" % (self.client_address[0], self.data)

		# Check if we are ready to regenerate the population
		maxVotes = 0
		maxMember = None
		if sum(self.popDict.values()) >= VOTE_THRESHOLD:
			for member, vote in self.popDict.items():
				if vote > maxVotes:
					maxVotes = vote
					maxMember = member

			picHelper = PicEvolveServerHelper()
			newPop = {}
			for i in range(POPULATION):
				newPop[picHelper.getMutation(maxMember).strip()] = 0

			print "Population regenerated"
			try:
				f = open(DB_FILE, "w")
				pickle.dump(newPop, f)
				f.close()
			except:
				print "Error writing to " + DB_FILE


if __name__ == "__main__":

	pServer = PicEvolveServerHelper()
	pServer.initialize(POPULATION)

    	# Create the server, binding to localhost on port 9999
    	server = SocketServer.TCPServer((HOST, PORT), PicEvolveTCPHandler)

    	# Activate the server; this will keep running until you
    	# interrupt the program with Ctrl-C
    	server.serve_forever()
	
