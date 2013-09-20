import PicEvolve
import pyparsing
from pyparsing import ParseException
from types import NoneType
from sexpParser import sexp
from GimpGradient import GimpGradient
import threading
import functools
import math
import cPickle
import wx

class PicEvolveFrame(wx.Frame):

	CONFIG_FILE = ".config"
	LIB_FILE = ".library.dat"


	def __init__(self, parent, id=-1,title="",pos=wx.DefaultPosition,
		     size=wx.DefaultSize, style=wx.DEFAULT_FRAME_STYLE,
		     name="frame"):

		wx.Frame.__init__(self,parent,id,title,pos,size,style,name)

		icon = wx.Icon(".icon.ico",wx.BITMAP_TYPE_ICO)
		self.SetIcon(icon)

		self.gradientFile = None
		self.libFrame = None

		self.splitter = wx.SplitterWindow(self)

		bSizer = wx.BoxSizer()
		self.textPanel = wx.Panel(self.splitter)
		self.textCtrl = wx.TextCtrl(self.textPanel,wx.ID_ANY,style=wx.TE_MULTILINE)
		bSizer.Add(self.textCtrl,1,wx.EXPAND)
		self.textPanel.SetSizerAndFit(bSizer)

		self.panel = wx.ScrolledWindow(self.splitter,wx.ID_ANY)
		self.panel.SetScrollbars(1,1,1,1)

		self.splitter.SplitHorizontally(self.textPanel,self.panel,50)
		
		statusBar = self.CreateStatusBar()

		menuBar = wx.MenuBar()
		menu1 = wx.Menu()
		menu2 = wx.Menu()
		menu3 = wx.Menu()
		m = menu1.Append(wx.NewId(), "&Initialize", "Initialize population with random images")
		m2 = menu1.Append(wx.NewId(), "&Add", "Add s-expression in text box to current population")
		m3 = menu1.Append(wx.NewId(), "&View", "View selected image in a new window")
		m4 = menu1.Append(wx.NewId(), "&Save", "Save selected image")
		m5 = menu1.Append(wx.NewId(), "&Exit", "Exit")
		menuBar.Append(menu1,"&File")
		m6 = menu2.Append(wx.NewId(), "&Mutate", "Create mutations of s-expression in text box.")
		m7 = menu2.Append(wx.NewId(), "&Crossover", "Create offspring between two images")
		m8 = menu2.Append(wx.NewId(), "&Preferences", "Configure preferences")
		menuBar.Append(menu2,"&Tools")
		m9 = menu3.Append(wx.NewId(), "&Add", "Add selected image to genetic library")
		m10 = menu3.Append(wx.NewId(), "&Remove", "Remove selected image from genetic library")
		m11 = menu3.Append(wx.NewId(), "&Load", "Load images from genetic library")
		menuBar.Append(menu3,"&Library")
		self.Bind(wx.EVT_MENU,self.OnInit,m)
		self.Bind(wx.EVT_MENU,self.OnAdd,m2)
		self.Bind(wx.EVT_MENU,self.OnView,m3)
		self.Bind(wx.EVT_MENU,self.OnSave,m4)
		self.Bind(wx.EVT_MENU,self.OnExit,m5)
		self.Bind(wx.EVT_MENU,self.OnMutate,m6)
		self.Bind(wx.EVT_MENU,self.OnCrossover,m7)
		self.Bind(wx.EVT_MENU,self.OnPreferences,m8)
		self.Bind(wx.EVT_MENU,self.OnAddLib,m9)
		self.Bind(wx.EVT_MENU,self.OnRemove,m10)
		self.Bind(wx.EVT_MENU,self.OnLoadLib,m11)

		self.SetMenuBar(menuBar)

		# Initialize some settings with info in CONFIG_FILE
		try:
			f = open(self.CONFIG_FILE)
			lines = f.readlines()
			config = {}
			for line in lines:
				tmp = line.strip().split("=")
				if tmp[0] == "gradient":
					config[tmp[0]] = str(tmp[1])
				elif tmp[0] == "width" or tmp[0] == "height" or tmp[0] == "cols":
					config[tmp[0]] = int(tmp[1])
				else:
					config[tmp[0]] = float(tmp[1])

			self.picWidth = config["width"]
			self.picHeight = config["height"]
			self.displayCols = config["cols"]
			self.gradientFile = config["gradient"]
			self.globMut = config["globMut"]
			self.funcRandMut= config["funcRandMut"]
			self.funcDiffMut = config["funcDiffMut"]
			self.funcBArgMut= config["funcBArgMut"]
			self.funcBValMut= config["funcBValMut"]
			self.funcCopyMut= config["funcCopyMut"]
			self.scalRandMut= config["scalRandMut"]
			self.scalAdjMut= config["scalAdjMut"]
			self.scalBArgMut= config["scalBArgMut"]
			self.scalCopyMut= config["scalCopyMut"]
			self.varRandMut= config["varRandMut"]
			self.varBArgMut= config["varBArgMut"]
			self.varCopyMut= config["varCopyMut"]
			f.close()
		except:
			print "Error opening configuration file."

	def OnInit(self, event):

		dlg = wx.TextEntryDialog(None,"Enter Population Size:","Population Size")
		self.popSize = 0
		if dlg.ShowModal() == wx.ID_OK:
			self.popSize = int(dlg.GetValue())
		dlg.Destroy()

		progress = wx.ProgressDialog("Initializing","Creating Images",self.popSize)

		try:
			self.pEvolver.pop = [""]*self.popSize
		except AttributeError:
			if not type(self.gradientFile) == NoneType:
				self.pEvolver = PicEvolve.PicEvolve(self.popSize,(self.picWidth,self.picHeight),False, "./gradients/"+ self.gradientFile, mutRates=self.getMutRates())	
			else:
				self.pEvolver = PicEvolve.PicEvolve(self.popSize,(self.picWidth,self.picHeight),False, mutRates=self.getMutRates())	

		count = 0
		for i in range(self.popSize):
			self.pEvolver.pop[i] = self.pEvolver.randomSExpr()
			self.pEvolver.createImage(self.pEvolver.pop[i],"img"+str(i)+".png",(self.picWidth,self.picHeight))
			count += 1
			progress.Update(count)

		progress.Destroy()
		self.DisplayImages(self.popSize)
	
	def DisplayImages(self, num):
		"""Display num images named img[n].png
		   for 0 <= n <= num in a grid sizer."""

		rows = int(math.ceil(num / float(self.displayCols)))

		if not type(self.panel.GetSizer()) == NoneType:
			self.panel.GetSizer().Clear(deleteWindows=True)
		sizer = wx.GridSizer(rows=rows,cols=self.displayCols,vgap=5)

		filenames = []
		for i in range(num):
			filenames.append("img"+str(i)+".png")
		imgId = 0
		for fn in filenames:
			p = wx.Panel(self.panel)
			img = wx.Image(fn,wx.BITMAP_TYPE_ANY)
			img2 = wx.StaticBitmap(p,imgId,wx.BitmapFromImage(img))
			imgId += 1
			img2.Bind(wx.EVT_LEFT_DCLICK,functools.partial(self.OnDClick,widget=img2))
			img2.Bind(wx.EVT_LEFT_DOWN,functools.partial(self.OnClick,widget=img2))

			pSizer = wx.BoxSizer()
			pSizer.Add(img2,0,wx.ALL,border=10)
			p.SetSizerAndFit(pSizer)
			sizer.Add(p)

		self.panel.SetSizer(sizer)
		self.Fit()

		h, w = self.panel.GetSize()
		self.panel.SetSize(((h+1),w))
		self.panel.SetSize((h,w))

	def UpdateProgress(self, count):

		self.progress.Update(count)

	def OnDClick(self, event, widget):
		"""Run a thread that creates mutations
		   upon a double click."""

		self.progress = wx.ProgressDialog("Mutating","Creating Images",self.popSize)
		thread = MutateThread(self.pEvolver.pop[widget.GetId()], self.popSize, self)
		thread.start()
	
	def OnClick(self, event, widget):
		"""Highlight the selected image and display it's
		   expression on left click."""
	
		panel = widget.GetParent()
		if panel.GetBackgroundColour() == (0,255,0):
			panel.SetBackgroundColour((230,221,213))
			return

		otherPanels = panel.GetParent().GetChildren()
		for p in otherPanels:
			p.SetBackgroundColour((230,221,213))
		panel.SetBackgroundColour("Green")

		self.textCtrl.SetValue(self.pEvolver.pop[widget.GetId()])
	
	def OnAdd(self, event):
		"""Add s-expression in text box to current population."""

		sexpr = str(self.textCtrl.GetValue())
		if len(sexpr) == 0:
			return

		try:
			self.popSize = self.popSize + 1
		except AttributeError:
			self.popSize = 1

		try:
			self.pEvolver.pop.append(sexpr)
		except AttributeError:
			if not type(self.gradientFile) == NoneType:
				self.pEvolver = PicEvolve.PicEvolve(self.popSize, (self.picWidth,self.picHeight), False, "./gradients/" + self.gradientFile, mutRates = self.getMutRates())
			else:
				self.pEvolver = PicEvolve.PicEvolve(self.popSize, (self.picWidth,self.picHeight), False, mutRates=self.getMutRates())
			self.pEvolver.pop[0] = sexpr

		if self.popSize == 1:

			self.pEvolver.createImage(sexpr, "img0.png", (self.picWidth,self.picHeight))

		else:
			for i in range(len(self.pEvolver.pop)):
				self.pEvolver.createImage(self.pEvolver.pop[i], "img" + str(i) + ".png", (self.picWidth,self.picHeight))

		self.DisplayImages(len(self.pEvolver.pop))
	
	def OnView(self, event):
		"""View the selected image in a new window."""
	
		sexpr = str(self.textCtrl.GetValue())
		if len(sexpr) == 0:
			return

		dlg = wx.TextEntryDialog(None,"Enter Image Size (separate dimensions with comma):","Dimensions")
		x = y = 0
		if dlg.ShowModal() == wx.ID_OK:
			l = dlg.GetValue().split(",")
			x, y = int(l[0]),int(l[1])
		dlg.Destroy()

		thread = ViewImageThread(sexpr, (x,y), self)
		thread.run()
	
	def OnMutate(self, event):
		"""Create mutations of the s-expression in text box."""

		sexpr = str(self.textCtrl.GetValue())
		if len(sexpr) == 0:
			return

		dlg = wx.TextEntryDialog(None,"Enter population size:","Population Size")
		if dlg.ShowModal() == wx.ID_OK:
			self.popSize = int(str(dlg.GetValue()))
		dlg.Destroy()

		self.progress = wx.ProgressDialog("Mutating","Creating Images",self.popSize)
		thread = MutateThread(sexpr,self.popSize,self)
		thread.start()

	def OnSave(self, event):
		"""Save the currently selected image."""
	
		sexpr = str(self.textCtrl.GetValue())
		if len(sexpr) == 0:
			return

		dlg = wx.TextEntryDialog(None,"Enter Image Size (separate dimensions with comma):","Dimensions")
		x = y = 0
		if dlg.ShowModal() == wx.ID_OK:
			l = dlg.GetValue().split(",")
			x, y = int(l[0]),int(l[1])
		dlg.Destroy()

		dlg = wx.FileDialog(None, "Save Image", style=wx.SAVE)
		filename = ""
		if dlg.ShowModal() == wx.ID_OK:
			filename = dlg.GetFilename()
		dlg.Destroy()

		thread = SaveImageThread(sexpr,filename,(x,y),self)
		thread.start()
	
	def OnExit(self, event):

		self.Destroy()
	
	def OnPreferences(self, event):
		"""Allow the user to change program preferences."""
	
		pFrame = wx.Frame(None,wx.ID_ANY,"Preferences",size=(450,500))
		pPanel = wx.ScrolledWindow(pFrame)
		pPanel.SetScrollbars(1,1,1,1)
		
		displayTitle = wx.StaticText(pPanel, wx.ID_ANY, "Display Settings")
		displayTitle.SetFont(wx.Font(13,wx.SWISS,wx.NORMAL,wx.BOLD))

		widthLbl = wx.StaticText(pPanel, wx.ID_ANY, "Width of preview pictures:\t\t\t")
		widthTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "", size=(100,-1))

		heightLbl = wx.StaticText(pPanel, wx.ID_ANY, "Height of preview pictures:\t\t\t")
		heightTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "", size=(100,-1))

		colLbl = wx.StaticText(pPanel, wx.ID_ANY, "Columns in picture display:\t\t\t")
		colTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "", size=(100,-1))

		fileLbl = wx.StaticText(pPanel, wx.ID_ANY, "GIMP gradient file\n for color mapping:\t\t\t\t")
		fileTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "", size=(100,-1))
		fileBtn = wx.Button(pPanel, wx.ID_ANY, "...", size=(30,30))
		fileBtn.Bind(wx.EVT_BUTTON, functools.partial(self.OnPrefOpenFile, widget=fileTxt))

		mutateTitle = wx.StaticText(pPanel, wx.ID_ANY, "Mutation Settings")
		mutateTitle.SetFont(wx.Font(13,wx.SWISS,wx.NORMAL,wx.BOLD))

		mLabel = wx.StaticText(pPanel,wx.ID_ANY, "Global mutation rate:\t\t\t\t ")
		mTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "")
		
		funcMutTitle = wx.StaticText(pPanel, wx.ID_ANY, "Function node mutation rates: ")
		funcMutTitle.SetFont(wx.Font(10,wx.SWISS,wx.NORMAL,wx.BOLD))

		fRandLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tRandom expression mutation:\t\t     ")
		fRandTxt = wx.TextCtrl(pPanel,wx.ID_ANY,"")
		fDiffLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tBecome different function mutation:\t     ")
		fDiffTxt = wx.TextCtrl(pPanel,wx.ID_ANY,"")
		fBArgLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tBecome argument to new function mutation: ")
		fBArgTxt = wx.TextCtrl(pPanel,wx.ID_ANY,"")
		fBValLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tBecome value of argument mutation:\t     ")
		fBValTxt = wx.TextCtrl(pPanel,wx.ID_ANY,"")
		fCopyLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tBecome copy of another node mutation:\t     ")
		fCopyTxt = wx.TextCtrl(pPanel,wx.ID_ANY,"")

		scalMutTitle = wx.StaticText(pPanel, wx.ID_ANY, "Scalar node mutation rates: ")
		scalMutTitle.SetFont(wx.Font(10,wx.SWISS,wx.NORMAL,wx.BOLD))

		sRandLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tRandom expression mutation:\t\t     ")
		sRandTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "")
		sAdjLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tAdjust by a random amount mutation:\t     ")
		sAdjTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "")
		sBArgLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tBecome argument to new function mutation: ")
		sBArgTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "")
		sCopyLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tBecome copy of another node mutation:\t     ")
		sCopyTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "")

		varMutTitle = wx.StaticText(pPanel, wx.ID_ANY, "Variable node mutation rates: ")
		varMutTitle.SetFont(wx.Font(10, wx.SWISS,wx.NORMAL,wx.BOLD))

		vRandLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tRandom expression mutation:\t\t     ")
		vRandTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "")
		vBArgLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tBecome argument to new function mutation: ")
		vBArgTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "")
		vCopyLbl = wx.StaticText(pPanel, wx.ID_ANY, "\tBecome copy of another node mutation:\t     ")
		vCopyTxt = wx.TextCtrl(pPanel, wx.ID_ANY, "")

		saveBtn = wx.Button(pPanel, wx.ID_ANY, "Save")
		cancelBtn = wx.Button(pPanel, wx.ID_ANY, "Cancel")
		pFrame.Bind(wx.EVT_BUTTON,self.OnPrefSave, saveBtn)
		pFrame.Bind(wx.EVT_BUTTON,self.OnPrefCancel, cancelBtn)

		mainSizer = wx.BoxSizer(wx.VERTICAL)

		tSizer = wx.BoxSizer()
		tSizer.Add(displayTitle,0,wx.ALL,0)
		tSizer.Add((25,25),1)
		dimSizer = wx.BoxSizer(wx.HORIZONTAL)
		dimSizer.Add(widthLbl)
		dimSizer.Add(widthTxt)
		dimSizer2 = wx.BoxSizer(wx.HORIZONTAL)
		dimSizer2.Add(heightLbl)
		dimSizer2.Add(heightTxt)
		colSizer = wx.BoxSizer(wx.HORIZONTAL)
		colSizer.Add(colLbl)
		colSizer.Add(colTxt)
		fileSizer = wx.BoxSizer(wx.HORIZONTAL)
		fileSizer.Add(fileLbl)
		fileSizer.Add(fileTxt)
		fileSizer.Add(fileBtn)
		mtSizer = wx.BoxSizer()
		mtSizer.Add(mutateTitle)
		mtSizer.Add((25,25),1)
		mSizer = wx.BoxSizer(wx.HORIZONTAL)
		mSizer.Add(mLabel)
		mSizer.Add(mTxt)
		fmSizer = wx.BoxSizer()
		fmSizer.Add(funcMutTitle,0,wx.ALL,0)

		fRandSizer = wx.BoxSizer(wx.HORIZONTAL)
		fRandSizer.Add(fRandLbl)
		fRandSizer.Add(fRandTxt)
		fDiffSizer = wx.BoxSizer(wx.HORIZONTAL)
		fDiffSizer.Add(fDiffLbl)
		fDiffSizer.Add(fDiffTxt)
		fBArgSizer = wx.BoxSizer(wx.HORIZONTAL)
		fBArgSizer.Add(fBArgLbl)
		fBArgSizer.Add(fBArgTxt)
		fBValSizer = wx.BoxSizer(wx.HORIZONTAL)
		fBValSizer.Add(fBValLbl)
		fBValSizer.Add(fBValTxt)
		fCopySizer = wx.BoxSizer(wx.HORIZONTAL)
		fCopySizer.Add(fCopyLbl)
		fCopySizer.Add(fCopyTxt)

		smSizer = wx.BoxSizer()
		smSizer.Add(scalMutTitle,0,wx.ALL,0)
		
		sRandSizer = wx.BoxSizer(wx.HORIZONTAL)
		sRandSizer.Add(sRandLbl)
		sRandSizer.Add(sRandTxt)
		sAdjSizer = wx.BoxSizer(wx.HORIZONTAL)
		sAdjSizer.Add(sAdjLbl)
		sAdjSizer.Add(sAdjTxt)
		sBArgSizer = wx.BoxSizer(wx.HORIZONTAL)
		sBArgSizer.Add(sBArgLbl)
		sBArgSizer.Add(sBArgTxt)
		sCopySizer = wx.BoxSizer(wx.HORIZONTAL)
		sCopySizer.Add(sCopyLbl)
		sCopySizer.Add(sCopyTxt)

		varmSizer = wx.BoxSizer()
		varmSizer.Add(varMutTitle,0,wx.ALL,0)

		vRandSizer = wx.BoxSizer(wx.HORIZONTAL)
		vRandSizer.Add(vRandLbl)
		vRandSizer.Add(vRandTxt)
		vBArgSizer = wx.BoxSizer(wx.HORIZONTAL)
		vBArgSizer.Add(vBArgLbl)
		vBArgSizer.Add(vBArgTxt)
		vCopySizer = wx.BoxSizer(wx.HORIZONTAL)
		vCopySizer.Add(vCopyLbl)
		vCopySizer.Add(vCopyTxt)

		bSizer = wx.BoxSizer(wx.HORIZONTAL)
		bSizer.Add((20,20),1)
		bSizer.Add(saveBtn)
		bSizer.Add((20,20),1)
		bSizer.Add(cancelBtn)
		bSizer.Add((20,20),1)

		mainSizer.Add(tSizer,0,wx.ALIGN_CENTER,5)
		mainSizer.Add(dimSizer)
		mainSizer.Add(dimSizer2)
		mainSizer.Add(colSizer)
		mainSizer.Add(fileSizer)
		mainSizer.Add(wx.StaticLine(pPanel, wx.ID_ANY),0,wx.EXPAND|wx.ALL,5)
		mainSizer.Add(mtSizer,0,wx.ALIGN_CENTER,5)
		mainSizer.Add(mSizer)
		mainSizer.Add(fmSizer)
		mainSizer.Add(fRandSizer)
		mainSizer.Add(fDiffSizer)
		mainSizer.Add(fBArgSizer)
		mainSizer.Add(fBValSizer)
		mainSizer.Add(fCopySizer)
		mainSizer.Add(wx.StaticLine(pPanel, wx.ID_ANY),0, wx.EXPAND|wx.ALL, 5)
		mainSizer.Add(smSizer)
		mainSizer.Add(sRandSizer)
		mainSizer.Add(sAdjSizer)
		mainSizer.Add(sBArgSizer)
		mainSizer.Add(sCopySizer)
		mainSizer.Add(wx.StaticLine(pPanel, wx.ID_ANY),0, wx.EXPAND|wx.ALL, 5)
		
		mainSizer.Add(varmSizer)
		mainSizer.Add(vRandSizer)
		mainSizer.Add(vBArgSizer)
		mainSizer.Add(vCopySizer)

		mainSizer.Add(wx.StaticLine(pPanel, wx.ID_ANY),0,wx.EXPAND|wx.ALL,5)
		mainSizer.Add(bSizer,0,wx.EXPAND|wx.BOTTOM,5)

		# Load information from config file if available
		try:
			f = open(self.CONFIG_FILE)
			lines = f.readlines()
			config = {}
			for line in lines:
				tmp = line.strip().split("=")
				if tmp[0] == "gradient":
					config[tmp[0]] = str(tmp[1])
				elif tmp[0] == "width" or tmp[0] == "height" or tmp[0] == "cols":
					config[tmp[0]] = int(tmp[1])
				else:
					config[tmp[0]] = float(tmp[1])
			widthTxt.SetValue(str(config["width"]))
			heightTxt.SetValue(str(config["height"]))
			colTxt.SetValue(str(config["cols"]))
			fileTxt.SetValue(str(config["gradient"]))
			mTxt.SetValue(str(config["globMut"]))
			fRandTxt.SetValue(str(config["funcRandMut"]))
			fDiffTxt.SetValue(str(config["funcDiffMut"]))
			fBArgTxt.SetValue(str(config["funcBArgMut"]))
			fBValTxt.SetValue(str(config["funcBValMut"]))
			fCopyTxt.SetValue(str(config["funcCopyMut"]))
			sRandTxt.SetValue(str(config["scalRandMut"]))
			sAdjTxt.SetValue(str(config["scalAdjMut"]))
			sBArgTxt.SetValue(str(config["scalBArgMut"]))
			sCopyTxt.SetValue(str(config["scalCopyMut"]))
			vRandTxt.SetValue(str(config["varRandMut"]))
			vBArgTxt.SetValue(str(config["varBArgMut"]))
			vCopyTxt.SetValue(str(config["varCopyMut"]))
			f.close()
		except:
			print "Error opening configuration file."

		pPanel.SetSizer(mainSizer)
		pFrame.Show()
	
	def OnPrefSave(self, event):

		self.picWidth = int(float(str(event.GetEventObject().GetParent().GetChildren()[2].GetValue())))
		self.picHeight = int(float(str(event.GetEventObject().GetParent().GetChildren()[4].GetValue())))
		self.displayCols = int(float(str(event.GetEventObject().GetParent().GetChildren()[6].GetValue())))
		self.gradientFile = str(event.GetEventObject().GetParent().GetChildren()[8].GetValue())

		self.globMut = float(str(event.GetEventObject().GetParent().GetChildren()[12].GetValue()))
		self.funcRandMut = float(str(event.GetEventObject().GetParent().GetChildren()[15].GetValue()))
		self.funcDiffMut = float(str(event.GetEventObject().GetParent().GetChildren()[17].GetValue()))
		self.funcBArgMut = float(str(event.GetEventObject().GetParent().GetChildren()[19].GetValue()))
		self.funcBValMut = float(str(event.GetEventObject().GetParent().GetChildren()[21].GetValue()))
		self.funcCopyMut = float(str(event.GetEventObject().GetParent().GetChildren()[23].GetValue()))
		self.scalRandMut = float(str(event.GetEventObject().GetParent().GetChildren()[26].GetValue()))
		self.scalAdjMut = float(str(event.GetEventObject().GetParent().GetChildren()[28].GetValue()))
		self.scalBArgMut = float(str(event.GetEventObject().GetParent().GetChildren()[30].GetValue()))
		self.scalCopyMut = float(str(event.GetEventObject().GetParent().GetChildren()[32].GetValue()))
		self.varRandMut = float(str(event.GetEventObject().GetParent().GetChildren()[35].GetValue()))
		self.varBArgMut = float(str(event.GetEventObject().GetParent().GetChildren()[37].GetValue()))
		self.varCopyMut = float(str(event.GetEventObject().GetParent().GetChildren()[39].GetValue()))

		try:
			f = open(self.CONFIG_FILE,"w")
			f.write("width=" + str(self.picWidth) + "\n")
			f.write("height=" + str(self.picHeight) + "\n")
			f.write("cols=" + str(self.displayCols) + "\n")
			f.write("gradient=" + self.gradientFile + "\n")
			f.write("globMut=" + str(self.globMut) + "\n")
			f.write("funcRandMut=" + str(self.funcRandMut) + "\n")
			f.write("funcDiffMut=" + str(self.funcDiffMut) + "\n")
			f.write("funcBArgMut=" + str(self.funcBArgMut) + "\n")
			f.write("funcBValMut=" + str(self.funcBValMut) + "\n")
			f.write("funcCopyMut=" + str(self.funcCopyMut) + "\n")
			f.write("scalRandMut=" + str(self.scalRandMut) + "\n")
			f.write("scalAdjMut=" + str(self.scalAdjMut) + "\n")
			f.write("scalBArgMut=" + str(self.scalBArgMut) + "\n")
			f.write("scalCopyMut=" + str(self.scalCopyMut) + "\n")
			f.write("varRandMut=" + str(self.varRandMut) + "\n")
			f.write("varBArgMut=" + str(self.varBArgMut) + "\n")
			f.write("varCopyMut=" + str(self.varCopyMut))
			f.close()
		except:
			print "Error writing configuration file."

		event.GetEventObject().GetParent().GetParent().Destroy()

	def OnPrefCancel(self, event):

		event.GetEventObject().GetParent().GetParent().Destroy()
	
	def OnPrefOpenFile(self, event, widget):
		
		dlg = wx.FileDialog(None, "Open GIMP gradient file", style=wx.OPEN)
		filename = ""
		if dlg.ShowModal() == wx.ID_OK:
			filename = dlg.GetFilename()
		dlg.Destroy()

		widget.SetValue(filename)
		
		try:
			self.pEvolver.colorMap = GimpGradient("./gradients/" + filename)
		except AttributeError:
			self.gradientFile = filename
	
	def OnCrossover(self, event):
		
		cFrame = wx.Frame(None,wx.ID_ANY,"Crossover",size=(400,200))
		cPanel = wx.Panel(cFrame)

		cSizer = wx.BoxSizer(wx.VERTICAL)
		cSizer.Add(wx.StaticText(cPanel,wx.ID_ANY,"Enter parent expression 1: "))
		cSizer.Add(wx.TextCtrl(cPanel,wx.ID_ANY,"",style=wx.TE_MULTILINE,size=(390,75)))
		cSizer.Add(wx.StaticText(cPanel,wx.ID_ANY,"Enter parent expression 2: "))
		cSizer.Add(wx.TextCtrl(cPanel,wx.ID_ANY,"",style=wx.TE_MULTILINE, size=(390,75)))

		pSizer = wx.BoxSizer(wx.HORIZONTAL)
		pSizer.Add(wx.StaticText(cPanel,wx.ID_ANY,"Enter number of offspring: "))
		pSizer.Add(wx.TextCtrl(cPanel,wx.ID_ANY,""))

		cSizer.Add(pSizer)

		bSizer = wx.BoxSizer(wx.HORIZONTAL)
		mButton = wx.Button(cPanel,wx.ID_ANY,"Mate!")
		mButton.Bind(wx.EVT_BUTTON,self.OnCrossoverButton,mButton)
		bSizer.Add(mButton)
		cButton = wx.Button(cPanel,wx.ID_ANY,"Cancel")
		cButton.Bind(wx.EVT_BUTTON,self.OnCrossoverCancel,cButton)
		bSizer.Add(cButton)

		cSizer.Add((15,15))
		cSizer.Add(bSizer,0,wx.ALIGN_CENTER,5)

		cPanel.SetSizer(cSizer)
		cPanel.Fit()
		cFrame.Fit()
		cFrame.Show()

	def OnCrossoverButton(self, event):
		"""Perform crossover"""

		parent1 = str(event.GetEventObject().GetParent().GetChildren()[1].GetValue())
		parent2 = str(event.GetEventObject().GetParent().GetChildren()[3].GetValue())
		self.popSize = int(event.GetEventObject().GetParent().GetChildren()[5].GetValue())

		self.progress = wx.ProgressDialog("Crossing Over","Creating Images",self.popSize)


		pEvolver = None
		if not type(self.gradientFile) == NoneType:
			pEvolver = PicEvolve.PicEvolve(self.popSize, (self.picWidth,self.picHeight), False,"./gradients/"+ self.gradientFile, mutRates=self.getMutRates())
		else:
			pEvolver = PicEvolve.PicEvolve(self.popSize, (self.picWidth,self.picHeight), False,mutRates=self.getMutRates())

		count = 0
		while count+1 < self.popSize:

			c1, c2 = pEvolver.crossOver(parent1,parent2)
			if c1 == "" or c2 == "":
				continue
			c1List = sexp.parseString(c1)[0]
			c2List = sexp.parseString(c2)[0]

			if not type(c1List) == float and not type(c2List) == float and len(c1List) > 1 and len(c2List) > 1:
				pEvolver.pop[count] = c1
				pEvolver.pop[count+1] = c2
				count += 2

		for i in range(len(pEvolver.pop)):

			print pEvolver.pop[i]
			pEvolver.createImage(pEvolver.pop[i],"img"+str(i)+".png",(self.picWidth,self.picHeight))
			self.UpdateProgress(i)

		self.pEvolver = pEvolver
		self.popSize = len(pEvolver.pop)

		self.progress.Destroy()
		self.DisplayImages(self.popSize)

	def OnCrossoverCancel(self, event):

		event.GetEventObject().GetParent().GetParent().Destroy()

	def OnAddLib(self, event):

		sexpr = str(self.textCtrl.GetValue())
		if len(sexpr) == 0:
			return

		# First add selected image expression to data file
		try:
			f = open(self.LIB_FILE,"a")
			pickler = cPickle.Pickler(f)
			pickler.dump(sexpr)
			f.close()
		except:
			print "Error saving image to library"

		# Create all the images in the genetic library
		images = []
		if not type(self.gradientFile) == NoneType:
			p = PicEvolve.PicEvolve(1,(0,0),False,"./gradients/" + self.gradientFile)
		else:
			p = PicEvolve.PicEvolve(1,(0,0),False)
		try:
			f = open(self.LIB_FILE,"r")
			upickler = cPickle.Unpickler(f)
	
			count = 0
			while True:

				try:
					images.append(upickler.load())
					p.createImage(images[count],"img"+str(count)+".png",(self.picWidth,self.picHeight))
					count += 1
				except:
					break
			f.close()
		except:
			print "Error loading genetic library"

		# If there isn't a libFrame open already
		if type(self.libFrame) == NoneType:

			# Put images in a grid sizer on the scrolled window panel
			self.libFrame = wx.Frame(None, wx.ID_ANY, "Genetic Library")
			self.libFrame.Bind(wx.EVT_CLOSE,self.OnCloseLib)
			libPanel = wx.ScrolledWindow(self.libFrame, wx.ID_ANY)
			libPanel.SetScrollbars(1,1,1,1)

			num = len(images)
			rows = int(math.ceil(num / float(self.displayCols)))
			sizer = wx.GridSizer(rows=rows,cols=self.displayCols,vgap=5)

			filenames = []
			for i in range(num):
				filenames.append("img"+str(i)+".png")
			imgId = 0
			for fn in filenames:
				img = wx.Image(fn,wx.BITMAP_TYPE_ANY)
				img2 = wx.StaticBitmap(libPanel,imgId,wx.BitmapFromImage(img))
				imgId += 1
				sizer.Add(img2)
				#img2.Bind(wx.EVT_LEFT_DCLICK,functools.partial(self.OnDClick,widget=img2))
				img2.Bind(wx.EVT_LEFT_DOWN,functools.partial(self.OnLibClick,widget=img2,sexpr=images[imgId-1]))

			libPanel.SetSizer(sizer)
			self.libFrame.Show()

		else: # If there is already a libFrame open

			panel = self.libFrame.GetChildren()[0]
			if not type(panel.GetSizer()) == NoneType:
				panel.GetSizer().Clear(deleteWindows=True)

			num = len(images)
			rows = int(math.ceil(num / float(self.displayCols)))
			sizer = wx.GridSizer(rows=rows,cols=self.displayCols)

			filenames = []
			for i in range(num):
				filenames.append("img"+str(i)+".png")
			imgId = 0
			for fn in filenames:
				img = wx.Image(fn,wx.BITMAP_TYPE_ANY)
				img2 = wx.StaticBitmap(panel,imgId,wx.BitmapFromImage(img))
				imgId += 1
				sizer.Add(img2)
				#img2.Bind(wx.EVT_LEFT_DCLICK,functools.partial(self.OnDClick,widget=img2))
				img2.Bind(wx.EVT_LEFT_DOWN,functools.partial(self.OnLibClick,widget=img2,sexpr=images[imgId-1]))
			panel.SetSizer(sizer)
			panel.Fit()
			self.libFrame.Fit()


	def OnCloseLib(self, event):

		try:
			self.libFrame.Destroy()
		except:
			pass
		self.libFrame = None
		event.Skip()

	def OnLoadLib(self, event):

		if not type(self.libFrame) == NoneType:
			return

		self.libFrame = wx.Frame(None, wx.ID_ANY, "Genetic Library")
		self.libFrame.Bind(wx.EVT_CLOSE,self.OnCloseLib)
		libPanel = wx.ScrolledWindow(self.libFrame, wx.ID_ANY)
		libPanel.SetScrollbars(1,1,1,1)

		# Load genetic library
		images = []
		if not type(self.gradientFile) == NoneType:
			p = PicEvolve.PicEvolve(1,(0,0),False, "./gradients/" + self.gradientFile)
		else:
			p = PicEvolve.PicEvolve(1,(0,0),False)
		try:
			f = open(self.LIB_FILE,"r")
			upickler = cPickle.Unpickler(f)
	
			count = 0
			while True:

				try:
					images.append(upickler.load())
					p.createImage(images[count],"img"+str(count)+".png",(self.picWidth,self.picHeight))
					count += 1
				except:
					break
			f.close()
		except:
			print "Error loading genetic library"

		# Put images in a grid sizer on the scrolled window panel
		num = len(images)
		rows = int(math.ceil(num / float(self.displayCols)))
		sizer = wx.GridSizer(rows=rows,cols=self.displayCols,vgap=5)

		filenames = []
		for i in range(num):
			filenames.append("img"+str(i)+".png")
		imgId = 0
		for fn in filenames:
			img = wx.Image(fn,wx.BITMAP_TYPE_ANY)
			img2 = wx.StaticBitmap(libPanel,imgId,wx.BitmapFromImage(img))
			imgId += 1
			sizer.Add(img2)
			#img2.Bind(wx.EVT_LEFT_DCLICK,functools.partial(self.OnDClick,widget=img2))
			img2.Bind(wx.EVT_LEFT_DOWN,functools.partial(self.OnLibClick,widget=img2,sexpr=images[imgId-1]))

		libPanel.SetSizer(sizer)
		self.libFrame.Show()
	
	def OnLibClick(self, event, widget, sexpr):

		self.textCtrl.SetValue(sexpr)
	
	def OnRemove(self, event):
		"""Remove selected s-expression from genetic library."""

		sexpr = str(self.textCtrl.GetValue())
		if len(sexpr) == 0:
			return

		images = []

		try:
			f = open(self.LIB_FILE,"r")
			unPickler = cPickle.Unpickler(f)
			while True:
				try:
					images.append(unPickler.load())
				except:
					break
			f.close()
		except:
			print "Error reading genetic library"

		if sexpr in images:
			images.remove(sexpr)

		try:
			f = open(self.LIB_FILE,"w")
			pickler = cPickle.Pickler(f)
			for image in images:
				pickler.dump(image)
			f.close()
		except:
			print "Error writing genetic library"

		if not type(self.libFrame) == NoneType:

			images = []
			if not type(self.gradientFile) == NoneType:
				p = PicEvolve.PicEvolve(1,(0,0),False, "./gradients/" + self.gradientFile)
			else:
				p = PicEvolve.PicEvolve(1,(0,0),False)
			try:
				f = open(self.LIB_FILE,"r")
				upickler = cPickle.Unpickler(f)
	
				count = 0
				while True:

					try:
						images.append(upickler.load())
						p.createImage(images[count],"img"+str(count)+".png",(self.picWidth,self.picHeight))
						count += 1
					except:
						break
				f.close()
			except:
				print "Error loading genetic library"

			panel = self.libFrame.GetChildren()[0]
			if not type(panel.GetSizer()) == NoneType:
				panel.GetSizer().Clear(deleteWindows=True)

			num = len(images)
			rows = int(math.ceil(num / float(self.displayCols)))
			sizer = wx.GridSizer(rows=rows,cols=self.displayCols)

			filenames = []
			for i in range(num):
				filenames.append("img"+str(i)+".png")
			imgId = 0
			for fn in filenames:
				img = wx.Image(fn,wx.BITMAP_TYPE_ANY)
				img2 = wx.StaticBitmap(panel,imgId,wx.BitmapFromImage(img))
				imgId += 1
				sizer.Add(img2)
				#img2.Bind(wx.EVT_LEFT_DCLICK,functools.partial(self.OnDClick,widget=img2))
				img2.Bind(wx.EVT_LEFT_DOWN,functools.partial(self.OnLibClick,widget=img2,sexpr=images[imgId-1]))
			panel.SetSizer(sizer)
			panel.Fit()
			self.libFrame.Fit()
	
	def getMutRates(self):
		"""Return a list of the mutation rates being used."""
	
		mRates = []
		try:
			mRates.append(self.globMut) 
			mRates.append(self.funcRandMut)
			mRates.append(self.funcDiffMut)
			mRates.append(self.funcBArgMut)
			mRates.append(self.funcBValMut)
			mRates.append(self.funcCopyMut)
			mRates.append(self.scalRandMut)
			mRates.append(self.scalAdjMut)
			mRates.append(self.scalBArgMut)
			mRates.append(self.scalCopyMut)
			mRates.append(self.varRandMut)
			mRates.append(self.varBArgMut) 
			mRates.append(self.varCopyMut)
		except:
			return PicEvolve.PicEvolve.DefaultMutationRates

		return mRates

	
class MutateThread(threading.Thread):

	def __init__(self, sExpr, populationSize, window):

		threading.Thread.__init__(self)
		self.sexpr = sExpr
		self.popSize = populationSize
		self.frame = window

	def run(self):

		count = 0

		mRates = self.frame.getMutRates()
		if not type(self.frame.gradientFile) == NoneType:
			pEvolver = PicEvolve.PicEvolve(self.popSize, (self.frame.picWidth,self.frame.picHeight), False,"./gradients/"+ self.frame.gradientFile, mutRates=mRates)
		else:
			pEvolver = PicEvolve.PicEvolve(self.popSize, (self.frame.picWidth,self.frame.picHeight), False, mutRates=mRates)


		for i in range(self.popSize):
			mutation = None
			while True:
				try:
					mutation = pEvolver.mutate(self.sexpr)
					mList = sexp.parseString(mutation)[0]
					if type(mList) == pyparsing.ParseResults:
						break
					else:
						continue
				except pyparsing.ParseException:
					pass
			pEvolver.pop[i] = mutation
			pEvolver.createImage(mutation, "img" + str(i) + ".png", (self.frame.picWidth,self.frame.picHeight))
			count += 1
			wx.CallAfter(self.frame.UpdateProgress,count)

		self.frame.pEvolver = pEvolver

		wx.CallAfter(self.frame.progress.Destroy)
		wx.CallAfter(self.frame.DisplayImages,len(pEvolver.pop))

class SaveImageThread(threading.Thread):

	def __init__(self, sExpr, fn, (width,height), window):

		threading.Thread.__init__(self)
		self.sexpr = sExpr
		self.filename = fn
		self.width = width
		self.height = height
		self.frame = window

	def run(self):

		self.frame.pEvolver.createImage(self.sexpr, self.filename, (self.width,self.height))

class ViewImageThread(threading.Thread):

	def __init__(self, sExpr, (width,height), window):

		threading.Thread.__init__(self)
		self.sexpr = sExpr
		self.width = width
		self.height = height
		self.frame = window

	def run(self):

		vFrame = wx.Frame(None, wx.ID_ANY, "View Image", size=(self.width,self.height))
		vPanel = wx.Panel(vFrame, wx.ID_ANY)

		pEvolver = None
		if not type(self.frame.gradientFile) == NoneType:
			pEvolver = PicEvolve.PicEvolve(self.frame.popSize, (self.width,self.height), False,"./gradients/"+ self.frame.gradientFile)
		else:
			pEvolver = PicEvolve.PicEvolve(self.frame.popSize, (self.width,self.height), False)

		pEvolver.createImage(self.sexpr, "tmp.png", (self.width,self.height))
		
		img = wx.Image("tmp.png",wx.BITMAP_TYPE_ANY)
		img2 = wx.StaticBitmap(vPanel,wx.ID_ANY,wx.BitmapFromImage(img))

		vFrame.Show()
		vPanel.Fit()

class PicEvolveApp(wx.App):

	def OnInit(self):
		
		self.frame = PicEvolveFrame(parent=None,title="PicEvolve",size=(650,450))
		self.frame.Show()
		self.SetTopWindow(self.frame)
		return True

if __name__ == "__main__":

	app = PicEvolveApp()
	app.MainLoop()
