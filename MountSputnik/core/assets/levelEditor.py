#must include the level:    cayon/block10.json
#							mountain/block3.json
LEVEL_TO_LOAD = "canyon/block3.json"

'''
Repeat Texture on Resize
========================

This examples repeats the letter 'K' (mtexture1.png) 64 times in a window.
You should see 8 rows and 8 columns of white K letters, along a label
showing the current size. As you resize the window, it stays an 8x8.
This example includes a label with a colored background.

Note the image mtexture1.png is a white 'K' on a transparent background, which
makes it hard to see.
'''

from kivy.app import App
from kivy.uix.image import Image
from kivy.uix.label import Label
from kivy.properties import OptionProperty, NumericProperty, ListProperty, \
        BooleanProperty, StringProperty
from kivy.lang import Builder
from kivy.uix.floatlayout import FloatLayout
from kivy.core.window import Window
from kivy.graphics import * 
from math import sqrt
from glob import glob 
import json 


Builder.load_string('''
<LinePlayground>:
	diffdropdown: diffdropdown.__self__
	dropdown: dropdown.__self__
                

    BoxLayout:
        size_hint: 1, 1
        orientation: 'horizontal' 
        
        GridLayout:
            cols: 1
            size_hint: 0.03,1
            Label: 
                size_hint_y: None
                height: 30
                text: 'Block height' 

            Slider:
                orientation: 'vertical'
                value: root.blockHeight
                on_value: root.blockHeight = args[1]
                on_value: root.resize()
                min: 16.
                max: 48.
            
            Label: 
                size_hint_y: None
                height: 10
                text: ('Crumble: ' + str(round(root.crumbling,2)))
            Slider: 
                size_hint_y: 0.1
                orientation: 'horizontal'
                value: root.crumbling
                on_value: root.crumbling = args[1]
                on_value: root.updateSelected()
                min:0
                max:10

            Label:
                size_hint_y: None
                height: 10
                text: ('Slip: ' + str(round(root.slipping,2)))
            Slider: 
                size_hint_y: 0.1
                orientation: 'horizontal'
                value: root.slipping
                on_value: root.slipping = args[1]
                on_value: root.updateSelected()
                min:0
                max:10            

            Label:
                size_hint_y: None
                height: 10
                text: ('Move Speed: ' + str(round(root.movingSpeed,2)))
            Slider:
                size_hint_y: 0.1 
                orientation: 'horizontal'
                value: root.movingSpeed
                on_value: root.movingSpeed = args[1]
                on_value: root.updateSelected()
                min:0
                max:3            
            
            Label:
                size_hint_y: 0.1
                text: ('Scale: ' + str(round(root.currentScale,2)))
            Slider:
                size_hint_y: 0.1
                value: root.currentScale
                on_value: root.currentScale = args[1]
                on_value: root.updateSelected()
                min: 0.5
                max: 5.
            Button:
                size_hint_y: 0.1
                text: 'Delete' 
                on_release: root.deleteHold()

            Button:
                id: whatToPlaceButton
                text: 'Handhold'
                on_release: whatToPlace.open(self)
                size_hint_y: None
                height: '48dp'

            DropDown: 
                id: whatToPlace
                on_parent: self.dismiss()
                on_select: whatToPlaceButton.text = '{}'.format(args[1])

                Button: 
                    text: 'Handhold'
                    size_hint_y: None
                    height: '48dp'
                    on_release: whatToPlace.select('Handhold')
                    on_release: root.changePlacement('Handhold')

                Button:
                    text: 'Static Obstacle'
                    size_hint_y: None
                    height: '48dp'
                    on_release: whatToPlace.select('Static Obstacle')
                    on_release: root.changePlacement('StaticObstacle')

                Button:
                    text: 'Falling Obstacle'
                    size_hint_y: None
                    height: '48dp'
                    on_release: whatToPlace.select('Falling Obstacle')
                    on_release: root.changePlacement('FallingObstacle')

                Button:
                    text: 'Moving Obstacle'
                    size_hint_y: None
                    height: '48dp'
                    on_release: whatToPlace.select('Moving Handhold')
                    on_release: root.changePlacement('MovingHandhold')


            Button:
                id: btn
                text: 'Canyon'
                on_release: dropdown.open(self)
                size_hint_y: None
                height: '48dp'

            DropDown:
            	id: dropdown
                on_parent: self.dismiss()
                on_select: btn.text = '{}'.format(args[1])

                Button:
                    text: 'Canyon'
                    size_hint_y: None
                    height: '48dp'
                    on_release: dropdown.select('Canyon')
                    on_release: root.changeDirectory("canyon")

                Button:
                    text: 'Mountain'
                    size_hint_y: None
                    height: '48dp'
                    on_release: dropdown.select('Mountain')
                    on_release: root.changeDirectory("mountain")

                Button:
                    text: 'Sky'
                    size_hint_y: None
                    height: '48dp'
                    on_release: dropdown.select('Sky')
                    on_release: root.changeDirectory("sky")

                Button:
                    text: 'Space'
                    size_hint_y: None
                    height: '48dp'
                    on_release: dropdown.select('Space')
                    on_release: root.changeDirectory("space")
                Button:
                    text: 'Tutorial'
                    size_hint_y: None
                    height: '48dp'
                    on_release: dropdown.select('Tutorial')
                    on_release: root.changeDirectory("tutorial")   

        AnchorLayout: 
            size_hint: 0.15, 1 

        AnchorLayout: 
            size_hint: 0.02, 1
            anchor_x: 'right'
            GridLayout: 
                cols: 1
                size_hint: 1,1

                Label: 
                    size_hint_y: 0.1
                    text: 'Falling Obstacles: '

                Label:
                    size_hint_y: 0.1
                    height: 10
                    text: ('Width: ' + str(round(root.horizontalRange,2)))
                Slider:
                    size_hint_y: 0.1 
                    orientation: 'horizontal'
                    value: root.horizontalRange
                    on_value: root.horizontalRange = args[1]
                    on_value: root.updateSelected()
                    min:3
                    max:32

                Label:
                    size_hint_y: 0.1
                    height: 10
                    text: ('Height: ' + str(round(root.verticalRange,2)))
                Slider:
                    size_hint_y: 0.1 
                    orientation: 'horizontal'
                    value: root.verticalRange
                    on_value: root.verticalRange = args[1]
                    on_value: root.updateSelected()
                    min:2
                    max:10 

                Label:
                    size_hint_y: 0.1
                    height: 10
                    text: ('Frequency: ' + str(round(root.frequency,2)))
                Slider:
                    size_hint_y: 0.1 
                    orientation: 'horizontal'
                    value: root.frequency
                    on_value: root.frequency = args[1]
                    on_value: root.updateSelected()
                    min:1
                    max:20

                Label: 
                    text: ''

                Label: 
                	text: 'Difficulty'
                	size_hint_y: None
                	height: '40px'

	            Button:
	                id: diff
	                text: 'Easy'
	                on_release: diffdropdown.open(self)
	                size_hint_y: None
	                height: '48dp'

	            DropDown:
	            	id: diffdropdown
	                on_parent: self.dismiss()
	                on_select: diff.text = '{}'.format(args[1])

	                Button:
	                    text: 'Easy'
	                    size_hint_y: None
	                    height: '48dp'
	                    on_release: diffdropdown.select('Easy')  
	                    on_release: root.changeDifficulty('easy')

	                Button:
	                    text: 'Medium'
	                    size_hint_y: None
	                    height: '48dp'
	                    on_release: diffdropdown.select('Medium')
	                    on_release: root.changeDifficulty('medium')

	                Button:
	                    text: 'Hard'
	                    size_hint_y: None
	                    height: '48dp'
	                    on_release: diffdropdown.select('Hard')
	                    on_release: root.changeDifficulty('hard')
	                

	            Label:
	            	text: ''
	            	size_hint_y: None
	            	height: '30px'

	            Button: 
	            	size_hint_y: 0.1
	            	text: 'Load'
	            	on_release: root.loadJSON()
                Button: 
                    size_hint_y: 0.1
                    text: 'Submit'
                    on_release: root.createJSON()
                Button: 
                    size_hint_y: 0.1
                    text: 'Clear' 
                    on_release: root.clear()



''')

class Handhold(object): 

    def __init__(self,x,y,scale,c,s): 
        self.pos = [x,y]
        self.scale = scale
        self.crumbling = c
        self.slipping = s

class MovingHandhold(Handhold): 

    def __init__(self,x,y,scale,c,s,speed): 
        super(MovingHandhold, self).__init__(x,y,scale,c,s)
        self.speed = speed

class FallingObstacle(object): 
    def __init__(self,x,y,width,height,frequency): 
        self.pos = [x,y]
        self.width = width
        self.height = height 
        self.frequency = frequency
        self.scale = fallingObstacleWidth

class StaticObstacle(object): 
    def __init__(self,x,y,scale): 
        self.pos = [x,y]
        self.scale = scale


       

blockWidth = 16
handhold_width = 1
fallingObstacleWidth = 2
scaling = 32

handholds = []
staticObstacles = []
fallingObstacles = []
movingHandholds = []


selected = -1
canvas_left = Window.size[0]/2
canvas_origin = 0




class LinePlayground(FloatLayout):
    blockHeight = NumericProperty(22)
    currentScale = NumericProperty(1)
    directory = StringProperty("assets/canyon/")
    background = ListProperty((0.2, 0.2, 0.2))
    whatToPlace = StringProperty("Handhold")
    crumbling = NumericProperty(0)
    slipping = NumericProperty(0)
    movingSpeed = NumericProperty(0)
    difficulty = StringProperty('easy')

    horizontalRange = NumericProperty(5)
    verticalRange = NumericProperty(5)
    frequency = NumericProperty(5)
    # scaleX = Window.size[0]-80/blockWidth
    # scaleY = Window.size[1]-60/blockHeight
    def __init__(self, **kwargs): 
        super(LinePlayground, self).__init__(**kwargs)

    def init(self, window, width, height): 
        self._keyboard = Window.request_keyboard(self._disable_keyboard, self, 'text')
        self._keyboard.bind(on_key_down=self._capture_key)
        with self.canvas: 
            Color(0,0,0)
            Rectangle(pos=[canvas_left,0],size=[blockWidth*scaling, Window.size[1]])
            Color(1,1,1)
            Rectangle(pos=[canvas_left, canvas_origin],size=[blockWidth*scaling,self.blockHeight*scaling], source=self.directory+'Surface.png')
            Rectangle(pos=[canvas_left+blockWidth*scaling/2.9, 150], size=[blockWidth*scaling/2.8, blockWidth*scaling/2.8], source='assets/character.png')
        self.drawHandholds()
        self.drawObstacles()

    def _capture_key(self,keyboard,keycode,text,modifiers): 
        global canvas_origin
        if keycode[0] == 273: 
            if canvas_origin < 10: 
                canvas_origin += 20
        if keycode[0] == 274: 
            if canvas_origin+self.blockHeight*scaling > Window.size[1]-10: 
                canvas_origin -= 20
        self.resize()   

    def _disable_keyboard(self):
        """Disables keyboard events for this input handler"""
        self._keyboard.unbind(on_key_down=self._capture_key)

    def resize(self): 
        with self.canvas: 
            Color(0,0,0)
            Rectangle(pos=[canvas_left, 0], size=[blockWidth*scaling, Window.size[1]])
            Color(1,1,1,1)
            Rectangle(pos=[canvas_left, canvas_origin],size=[blockWidth*scaling,self.blockHeight*scaling], source=self.directory+"Surface.png")
            Color(1,1,1,1)
            Rectangle(pos=[canvas_left+blockWidth*scaling/2.9, 150], size=[blockWidth*scaling/2.8, blockWidth*scaling/2.8], source='assets/character.png')
        self.removeBadHandholds()
        self.drawHandholds()
        self.drawObstacles()


    def on_touch_down(self, touch):
        global selected
        if self.withinCanvas(touch.x,0):
            self.doSelection(touch)
            if selected == -1:
                touch_x = touch.x - canvas_left
                touch_y = touch.y - canvas_origin
                if self.whatToPlace == "Handhold": 
                    handholds.append(Handhold(touch_x-0.5*handhold_width*self.currentScale*scaling, touch_y-0.5*handhold_width*self.currentScale*scaling, self.currentScale, self.crumbling, self.slipping))
                elif self.whatToPlace == "FallingObstacle":
                    if self.withinCanvas(touch.x - (scaling*(self.horizontalRange/2)), (self.horizontalRange)*scaling): 
                        fallingObstacles.append(FallingObstacle(touch_x-0.5*fallingObstacleWidth*scaling, touch_y-0.5*fallingObstacleWidth*scaling, self.horizontalRange, self.verticalRange, self.frequency))
                elif self.whatToPlace == "StaticObstacle": 
                    staticObstacles.append(StaticObstacle(touch_x-0.5*self.currentScale*scaling, touch_y-0.5*self.currentScale*scaling, self.currentScale))
                elif self.whatToPlace == "MovingHandhold": 
                    movingHandholds.append(MovingHandhold(touch_x-0.5*handhold_width*self.currentScale*scaling, touch_y-0.5*handhold_width*self.currentScale*scaling, self.currentScale, self.crumbling, self.slipping, self.movingSpeed))
            else: 
                self.updateSelected()
            self.resize()
        return super(LinePlayground, self).on_touch_down(touch)  

    def on_touch_move(self,touch): 
        global selected
        if selected != -1 and self.withinCanvas(touch.x,0): 
            touch_x = touch.x - canvas_left
            touch_y = touch.y - canvas_origin
            selected.pos = [touch_x, touch_y]
        self.resize()

    def updateSelected(self): 
        global selected 
        if selected != -1: 
            selected.scale = self.currentScale 
            if type(selected) is Handhold or type(selected) is MovingHandhold: 
                selected.crumbling = self.crumbling
                selected.slipping = self.slipping
            if type(selected) is MovingHandhold: 
                selected.speed = self.movingSpeed
            if type(selected) is FallingObstacle: 
                if self.withinCanvas(canvas_left + selected.pos[0] - (scaling*(self.horizontalRange/2)), (self.horizontalRange)*scaling): 
                    selected.width = self.horizontalRange
                selected.height = self.verticalRange
                selected.frequency = self.frequency
        self.resize()

    def drawHandholds(self): 
        with self.canvas: 
            for i in range(len(handholds)): 
                hold = handholds[i]
                if self.withinCanvas(canvas_left + hold.pos[0], hold.scale*scaling): 
                    if hold == selected: 
                        Color(1,0,0,1)
                    else: 
                        Color(1,1,1,1)
                    r = Rectangle(pos=[canvas_left + hold.pos[0],canvas_origin + hold.pos[1]],size=[scaling*handhold_width*hold.scale,scaling*handhold_width*hold.scale],source=self.directory + 'Handhold1.png')
            for i in range(len(movingHandholds)):
                if i % 2 == 1:  
                    hold = movingHandholds[i-1]
                    endHold = movingHandholds[i]
                    if self.withinCanvas(canvas_left + hold.pos[0], hold.scale*scaling): 
                        r = Rectangle(pos=[canvas_left + hold.pos[0],canvas_origin + hold.pos[1]],size=[scaling*handhold_width*hold.scale,scaling*handhold_width*hold.scale],source=self.directory + 'Handhold1.png')
                        q = Rectangle(pos=[canvas_left + endHold.pos[0],canvas_origin + endHold.pos[1]],size=[scaling*handhold_width*hold.scale,scaling*handhold_width*hold.scale],source=self.directory + 'movinghandholdend.png')
                        if hold == selected or endHold == selected: 
                            r.source = self.directory + 'handholdgrabbed.png'
                            q.source = self.directory + 'movinghandholdendgrabbed.png'
            if len(movingHandholds) % 2 == 1: 
                hold = movingHandholds[-1]
                r = Rectangle(pos=[canvas_left + hold.pos[0],canvas_origin + hold.pos[1]],size=[scaling*handhold_width*hold.scale,scaling*handhold_width*hold.scale],source=self.directory + 'Handhold1.png')
                if hold == selected: 
                    r.source = self.directory + 'handholdgrabbed.png'

    def drawObstacles(self): 
        with self.canvas: 
            for i in range(len(staticObstacles)):
                obstacle = staticObstacles[i]
                if self.withinCanvas(canvas_left + obstacle.pos[0], obstacle.scale * scaling): 
                    if obstacle == selected: 
                        Color(1,0,0,1)
                    else: 
                        Color(1,1,1,1)
                    Rectangle(pos=[canvas_left + obstacle.pos[0],canvas_origin + obstacle.pos[1]],size=[scaling*obstacle.scale,scaling*obstacle.scale],source=self.directory + 'FallingRock.png')
            for i in range(len(fallingObstacles)): 
                obstacle = fallingObstacles[i]
                if obstacle == selected: 
                    Color(1,0,0,0.5)
                else: 
                    Color(1,0,0,0.3)
                Rectangle(pos=[canvas_left + obstacle.pos[0] - (scaling*(obstacle.width/2)),canvas_origin + obstacle.pos[1] - (scaling*(obstacle.height/2))],size=[obstacle.width*scaling, obstacle.height*scaling])
                        

    def doSelection(self, touch): 
        global selected
        for L in [handholds, staticObstacles, fallingObstacles, movingHandholds]: 
            for i in range(len(L)): 
                current = L[i]
                if distance(canvas_left + current.pos[0], canvas_origin + current.pos[1],touch.x,touch.y) < current.scale*scaling: 
                    selected = L[i]
                    if L == handholds or L == movingHandholds: 
                        self.currentScale = L[i].scale
                        self.crumbling = L[i].crumbling
                        self.slipping = L[i].slipping
                    if L == movingHandholds: 
                        self.movingSpeed = L[i].speed
                    if L == staticObstacles: 
                        self.currentScale = L[i].scale
                    if L == fallingObstacles: 
                        self.horizontalRange = L[i].width
                        self.verticalRange = L[i].height
                        self.frequency = L[i].frequency   
                    return 
        selected = -1

    def withinCanvas(self, x, w): 
        return canvas_left < x and x+w < (canvas_left)+blockWidth*scaling  

    def clear(self):
        global handholds
        global movingHandholds
        global fallingObstacles
        global staticObstacles 
        global selected 
        handholds = []
        movingHandholds = [] 
        fallingObstacles = []
        staticObstacles = []
        selected = -1
        canvas_origin = 0
        self.resize()   

    def createJSON(self): 
    	if LEVEL_TO_LOAD != "": 
    		new_file = open('levels/'+LEVEL_TO_LOAD, 'w')
    		print "Updating " + LEVEL_TO_LOAD
    	else: 
	        new_id = 0
	        for link in glob('levels/' + self.directory[7:] + 'block*'): 
	            link_num = link[link.find("block")+5:-5]
	            if int(link_num) > new_id: 
	                new_id = int(link_num)
	            pass
	        new_id += 1
	        print "Creating " + 'levels/' + self.directory[7:] + 'block'+str(new_id)+'.json'
	        new_file = open('levels/' + self.directory[7:] + 'block'+str(new_id)+'.json', 'w+')

        level = {'difficulty': self.difficulty, 
                 'size': self.blockHeight, 
                 'handholds': {}, 
                 'static': {},
                 'obstacles': {}
                 } 
        for i in range(len(handholds)): 
            hold = handholds[i]
            level['handholds']['hold'+str(i)] = {
                # 'texture': self.directory[7:] + 'handhold.png',
                # 'glowTexture': self.directory[7:] + 'handholdglow.png',
                # 'gripTexture': self.directory[7:] + 'handholdgrabbed.png', 
                'width': handhold_width * hold.scale /3.0, 
                'height': handhold_width * hold.scale /3.0, 
                'positionX': 8.5 + (hold.pos[0])/scaling, 
                'positionY': (hold.pos[1])/scaling, 
                "friction": 1, 
                "restitution": 1, 
                "crumble": hold.crumbling,
                "slip": hold.slipping
            }
        for i in range(len(movingHandholds)): 
            if i % 2 == 1: 
                hold = movingHandholds[i-1] 
                endHold = movingHandholds[i]
                level['handholds']['hold'+str(i+len(handholds))] = {
                    # 'texture': self.directory[7:] + 'handhold.png',
                    # 'glowTexture': self.directory[7:] + 'handholdglow.png',
                    # 'gripTexture': self.directory[7:] + 'handholdgrabbed.png', 
                    'width': handhold_width * hold.scale /3.0, 
                    'height': handhold_width * hold.scale /3.0, 
                    'positionX': 8.5 + (hold.pos[0])/scaling, 
                    'positionY': (hold.pos[1])/scaling, 
                    "friction": 1, 
                    "restitution": 1, 
                    "crumble": hold.crumbling,
                    "slip": hold.slipping,
                    "movement": {
                        'startX': 8.5 + (hold.pos[0])/scaling, 
                        'startY': (hold.pos[1])/scaling, 
                        'endX': 8.5 + (endHold.pos[0])/scaling, 
                        'endY': (endHold.pos[1])/scaling, 
                        'speed': hold.speed
                    }
                }
        for i in range(len(staticObstacles)): 
            obstacle = staticObstacles[i]
            level['static']['obstacle'+str(i)] = {
                'x': 8.5 + obstacle.pos[0]/scaling, 
                'y': obstacle.pos[1]/scaling, 
                'size': obstacle.scale/3.0    
            }

        for i in range(len(fallingObstacles)): 
            obstacle = fallingObstacles[i]
            level['obstacles']['obstacle'+str(i)] = {
                'originX': 8.5 + obstacle.pos[0]/scaling, 
                'originY': (obstacle.pos[1]/scaling) + 18, 
                'width': obstacle.width/3.0, 
                'height': obstacle.height/3.0, 
                'frequency': obstacle.frequency * 60
            }

        s = str(level).replace("'", '"'); 

        new_file.write(s)
        new_file.close()
        #self.clear()

    def loadJSON(self):
    	if LEVEL_TO_LOAD == "":
    		return 
    	jfile = open(glob("levels/"+LEVEL_TO_LOAD)[0], 'r')
    	level = json.load(jfile)
    	jfile.close()
    	self.difficulty = level['difficulty']
    	self.blockHeight = level['size']
    	self.directory = "assets/" + LEVEL_TO_LOAD[:LEVEL_TO_LOAD.find('/')+1]
    	for h in level['handholds']: 
    		hold = level['handholds'][h]
    		if 'movement' in hold: 
    			scale = hold['width']/handhold_width*3.0
    			crumble = hold['crumble']
    			slip = hold['slip']
    			speed = hold['movement']['speed']
    			startX = (hold['movement']['startX']-8.5)*scaling
    			startY = hold['movement']['startY']*scaling
    			endX = (hold['movement'][endX]-8.5)*scaling
    			endY = hold['movement']['endY']*scaling
    			movingHandholds.append(MovingHandhold(startX,startY,scale,crumble,slip,speed))
    			movingHandholds.append(MovingHandhold(endX,endY,scale,crumble,slip,speed))
    		else:
    			scale = hold['width']/handhold_width*3.0
    			crumble = hold['crumble']
    			slip = hold['slip']
    			startX = (hold['positionX']-8.5)*scaling
    			startY = hold['positionY']*scaling
    			handholds.append(Handhold(startX, startY, scale, crumble, slip))
    	for static in level['static']: 
    		x = (static['x']-8.5)*scaling
    		y = static['y']*scaling
    		size = static['size']*3.0
    		staticObstacles.append(StaticObstacle(x,y,size))
    	for obstacle in level['obstacles']: 
    		originX = (obstacle['originX']-8.5)*scaling 
    		originY = obstacle['originY']*scaling - 18
    		width = obstacle['width']*3
    		height = obstacle['height']*3
    		frequency = obstacle['frequency']/60.0
    		fallingObstacles.append(FallingObstacle(originX,originY,width,height,frequency))
    	self.resize()

    def changeDirectory(self, btn):
        self.directory = "assets/" + btn + "/"
        self.resize() 

    def changePlacement(self, btn):
        self.whatToPlace = btn
        self.resize()

    def changeDifficulty(self, btn): 
    	self.difficulty = btn

    def removeBadHandholds(self): 
        for L in [handholds, movingHandholds, fallingObstacles, staticObstacles]: 
            for hold in L: 
              if hold.pos[1] + canvas_origin > self.blockHeight*scaling: 
                L.remove(hold)

    def deleteHold(self): 
        global selected
        if selected in handholds: 
            handholds.remove(selected)
        if selected in movingHandholds: 
            i = movingHandholds.index(selected)
            if i % 2 == 0 and len(movingHandholds)-1 != i: 
                movingHandholds.remove(movingHandholds[i+1])
            elif i % 2 == 1: 
                movingHandholds.remove(movingHandholds[i-1])
            movingHandholds.remove(selected)
            
        if selected in fallingObstacles: 
            fallingObstacles.remove(selected)
        if selected in staticObstacles: 
            staticObstacles.remove(selected)
        selected = -1
        self.resize()



def distance(x, y, z, w): 
    return sqrt(((x-z)**2)+((y-w)**2))

    

class TestLineApp(App):
    def build(self):
        l = LinePlayground()
        Window.bind(on_resize=l.init)
        return l


if __name__ == '__main__':
    TestLineApp().run()