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


Builder.load_string('''
<LinePlayground>:
    canvas:
        Rectangle: 
            pos: 275, 80
            size: 320, 320
            source: root.directory + 'background.png'

    BoxLayout:
        orientation: 'horizontal' 
        
        GridLayout:
            cols: 1
            size_hint: 0.1, 1

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
            
            GridLayout:
                cols: 1
                size_hint_y: None
                height: 80
                ToggleButton:
                    group: 'crumble'
                    text: 'crumble'
                    on_press: root.crumble = self.text
                ToggleButton:
                    group: 'move'
                    text: 'move'
                    on_press: root.move = self.text
                ToggleButton:
                    group: 'slippery'
                    text: 'slippery'
                    on_press: root.slippery = self.text

            Label:
                size_hint_y: 0.5
                text: ""

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
                    text: 'General'
                    size_hint_y: None
                    height: '48dp'
                    on_release: dropdown.select('General')
                    on_release: root.changeDirectory("general")


        AnchorLayout:
            size_hint: 1,0.1
            anchor_x: 'left'
            GridLayout:  
                cols: 2
                size_hint: 0.3, 1
                GridLayout:
                    rows: 2
                    size_hint: 1.6,1
                    Label:
                        text: 'Handhold scale'
                    Slider:
                        value: root.handHoldScale
                        on_value: root.handHoldScale = args[1]
                        on_value: root.updateSelected()
                        min: 0.5
                        max: 2.
                Button:
                    text: 'Delete' 
                    on_release: root.deleteHold()
        
                

        AnchorLayout: 
            size_hint: None, None
            width: 120
            height: 40
            anchor_x: 'right'
            GridLayout: 
                cols: 2
                Button: 
                    text: 'Submit'
                    on_release: root.createJSON()
                Button: 
                    text: 'Clear' 
                    on_release: root.clear()


''')

blockWidth = 32 
handhold_width = 2

handholds = []
selected = -1

WIDTH = Window.size[0]/3


class Handhold(): 

    #TODO: crumbling, moving, and slipping not yet implemented 
    def __init__(self,x,y,scale): 
        self.pos = [x,y]
        self.scale = scale
       

class LinePlayground(FloatLayout):
    blockHeight = NumericProperty(32)
    handHoldScale = NumericProperty(1)
    directory = StringProperty("assets/canyon/")
    background = ListProperty((0.2, 0.2, 0.2))
    # scaleX = Window.size[0]-80/blockWidth
    # scaleY = Window.size[1]-60/blockHeight
    def init(self, window, width, height): 
        with self.canvas: 
            Color(0,0,0)
            Rectangle(pos=[Window.size[0]/3,80],size=Window.size)
            Color(1,1,1)
            Rectangle(pos=[Window.size[0]/3,80],size=[blockWidth*10,self.blockHeight*10], source=self.directory + 'background.png')
        self.drawHandholds()


    def resize(self): 
        with self.canvas: 
            Color(0,0,0)
            Rectangle(pos=[Window.size[0]/3,80],size=Window.size)
            Color(1,1,1, 0.3)
            Rectangle(pos=[Window.size[0]/3,80],size=[blockWidth*10,self.blockHeight*10], source=self.directory + 'background.png')
            Color(1,1,1,1)
            Rectangle(pos=[(Window.size[0]+blockWidth*10)/3, 80], size=[320/5, 320/3], source='assets/character.png')
        self.removeBadHandholds()
        self.drawHandholds()

    def on_touch_down(self, touch):
        global selected
        if self.withinCanvas(touch.x, touch.y):
            self.doSelection(touch)
            if selected == -1: 
                handholds.append(Handhold(touch.x-15*self.handHoldScale, touch.y-15*self.handHoldScale, self.handHoldScale))
            else: 
                self.updateSelected()
            self.resize()
        return super(LinePlayground, self).on_touch_down(touch)  

    def on_touch_move(self,touch): 
        global selected
        if selected != -1 and self.withinCanvas(touch.x, touch.y): 
            handholds[selected].pos = [touch.x, touch.y]
        self.resize()

    def updateSelected(self): 
        global selected 
        if selected != -1: 
            handholds[selected].scale = self.handHoldScale 
        self.resize()

    def drawHandholds(self): 
        with self.canvas: 
            for i in range(len(handholds)): 
                hold = handholds[i]
                r = Rectangle(pos=[hold.pos[0],hold.pos[1]],size=[10*handhold_width*hold.scale,10*handhold_width*hold.scale],source=self.directory + 'handhold.png')
                if i == selected: 
                    r.source = self.directory + 'handholdselected.png'

    def doSelection(self, touch): 
        global selected
        for i in range(len(handholds)): 
            hold = handholds[i]
            if distance(hold.pos[0],hold.pos[1],touch.x-15*hold.scale,touch.y-15*hold.scale) < 15*hold.scale: 
                selected = i
                return 
        selected = -1

    def withinCanvas(self, x, y): 
        if Window.size[0]/3 < x < (Window.size[0]/3)+blockWidth*10: 
            if 80 < y < 80+self.blockHeight*10: 
                return True 
        return False   

    def clear(self):
        global handholds 
        global selected 
        handholds = []
        selected = -1
        self.resize()   

    def createJSON(self): 
        new_id = 0
        for link in glob('levels/' + self.directory[7:] + 'block*'): 
            if int(link[-6]) > new_id: 
                new_id = int(link[-6])
            pass
        new_id += 1
        new_file = open('levels/' + self.directory[7:] + 'block'+str(new_id)+'.json', 'w+')

        level = {'id': new_id, 
                 'difficulty': 'notRanked', 
                 'size': self.blockHeight, 
                 'handholds': {} 
                 } 
        for i in range(len(handholds)): 
            hold = handholds[i]
            level['handholds']['hold'+str(i)] = {
                'texture': self.directory[7:] + 'handhold.png',
                'glowTexture': self.directory[7:] + 'handholdglow.png',
                'gripTexture': self.directory[7:] + 'handholdgrabbed.png', 
                'width': handhold_width * hold.scale, 
                'height': handhold_width * hold.scale, 
                'positionX': (hold.pos[0]-Window.size[0]/3)/10, 
                'positionY': (hold.pos[1]-80)/10, 
                "friction": 1, 
                "restitution": 1, 
                "crumble": False
            }

        new_file.write(str(level))
        new_file.close()
        self.clear()

    def changeDirectory(self, btn):
        self.directory = "assets/" + btn + "/"
        self.resize() 

    def removeBadHandholds(self): 
        for hold in handholds: 
            if not self.withinCanvas(hold.pos[0], hold.pos[1]): 
                handholds.remove(hold)

    def deleteHold(self): 
        global selected
        if selected != -1: 
            handholds.remove(handholds[selected])
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