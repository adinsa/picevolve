import socket
import sys
from PicEvolve import PicEvolve

HOST, PORT = "localhost", 9999

class PicEvolveClient:

	def __init__(self, hostName, portNum):

		self.host = hostName
		self.port = portNum

	def getImages(self, numImages):
		"""Try to connect to the server and get image expressions."""

		# Create a socket (SOCK_STREAM means a TCP socket)
		sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) 

		# Connect to server and send data
		sock.connect((self.host, self.port))
		sock.send(str(numImages))

		recv = "" 
		lines = []
		while True:

			try:
				recv = sock.recv(1024)
				lines.append(recv)
			except:
				pass

			if recv == "":
				break

		lines = lines[0].split("\n")
		sock.close()

		return lines
	
	def voteFor(self, imageExpression):
		"""Vote for the image imageExpression in the
		   server's population. This is done by simply sending
		   the given image epxression back to the server."""

		# Create a socket (SOCK_STREAM means a TCP socket)
		sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) 

		# Connect to server and send data
		sock.connect((self.host, self.port))
		sock.send(imageExpression)

		sock.close()

if __name__ == "__main__":

	pclient = PicEvolveClient(HOST, PORT)
	pEvolve = PicEvolve(0, (200,200), False)
	
	while True:

		pop = pclient.getImages(5)
	
		for member in pop:
			print member

		favorite = raw_input(">>> ")
		if favorite == 'q':
			break
	
		pclient.voteFor(pop[int(favorite)])
