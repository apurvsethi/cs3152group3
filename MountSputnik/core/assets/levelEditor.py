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
    BoxLayout:
        size_hint: 0.15, 1
        orientation: 'horizontal' 
        
        GridLayout:
            cols: 1
            size_hint: 1,1
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
        
            ToggleButton:
                size_hint_y: 0.1
                group: 'crumble'
                text: 'crumble'
                on_press: root.crumble = self.text
            ToggleButton:
                size_hint_y: 0.1
                group: 'move'
                text: 'move'
                on_press: root.move = self.text
            ToggleButton:
                size_hint_y: 0.1
                group: 'slippery'
                text: 'slippery'
                on_press: root.slippery = self.text

            Label:
                size_hint_y: 0.1
                text: 'Handhold scale'
            Slider:
                size_hint_y: 0.1
                value: root.handHoldScale
                on_value: root.handHoldScale = args[1]
                on_value: root.updateSelected()
                min: 0.5
                max: 2.
            Button:
                size_hint_y: 0.1
                text: 'Delete' 
                on_release: root.deleteHold()

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
            size_hint: 1, None
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
handhold_width = 1
scaling = 32

handholds = []
selected = -1
canvas_left = Window.size[0]/4
canvas_origin = 0


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
    def __init__(self, **kwargs): 
        super(LinePlayground, self).__init__(**kwargs)

    def init(self, window, width, height): 
        self._keyboard = Window.request_keyboard(self._disable_keyboard, self, 'text')
        self._keyboard.bind(on_key_down=self._capture_key)
        with self.canvas: 
            Color(0,0,0)
            Rectangle(pos=[canvas_left,0],size=Window.size)
            Color(1,1,1)
            Rectangle(pos=[canvas_left, canvas_origin],size=[blockWidth*scaling,self.blockHeight*scaling], source=self.directory+'background.png')
        self.drawHandholds()

    def _capture_key(self,keyboard,keycode,text,modifiers): 
        global canvas_origin
        if keycode[0] == 273: 
            if canvas_origin < 10: 
                canvas_origin += 10
        if keycode[0] == 274: 
            if canvas_origin+self.blockHeight*scaling > Window.size[1]-10: 
                canvas_origin -= 10
        self.resize()   

    def _disable_keyboard(self):
        """Disables keyboard events for this input handler"""
        self._keyboard.unbind(on_key_down=self._capture_key)

    def resize(self): 
        with self.canvas: 
            Color(0,0,0)
            Rectangle(pos=[canvas_left, 0], size=[blockWidth*scaling, Window.size[1]])
            Color(1,1,1,1)
            Rectangle(pos=[canvas_left, canvas_origin],size=[blockWidth*scaling,self.blockHeight*scaling], source=self.directory+"background.png")
            Color(1,1,1,1)
            Rectangle(pos=[canvas_left+blockWidth*scaling/2.5, 0], size=[blockWidth*scaling/6, blockWidth*scaling/4], source='assets/character.png')
        self.removeBadHandholds()
        self.drawHandholds()


    def on_touch_down(self, touch):
        global selected
        if self.withinCanvas(touch.x):
            self.doSelection(touch)
            if selected == -1:
                touch_x = touch.x - canvas_left
                touch_y = touch.y - canvas_origin
                handholds.append(Handhold(touch_x-0.5*handhold_width*self.handHoldScale*scaling, touch_y-0.5*handhold_width*self.handHoldScale*scaling, self.handHoldScale))
            else: 
                self.updateSelected()
            self.resize()
        return super(LinePlayground, self).on_touch_down(touch)  

    def on_touch_move(self,touch): 
        global selected
        if selected != -1 and self.withinCanvas(touch.x): 
            touch_x = touch.x - canvas_left
            touch_y = touch.y - canvas_origin
            handholds[selected].pos = [touch_x, touch_y]
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
                if self.withinCanvas(canvas_left + hold.pos[0]): 
                    r = Rectangle(pos=[canvas_left + hold.pos[0],canvas_origin + hold.pos[1]],size=[scaling*handhold_width*hold.scale,scaling*handhold_width*hold.scale],source=self.directory + 'handhold.png')
                    if i == selected: 
                        r.source = self.directory + 'handholdgrabbed.png'

    def doSelection(self, touch): 
        global selected
        for i in range(len(handholds)): 
            hold = handholds[i]
            if distance(canvas_left + hold.pos[0], canvas_origin + hold.pos[1],touch.x,touch.y) < hold.scale*handhold_width*scaling: 
                selected = i
                return 
        selected = -1

    def withinCanvas(self, x): 
        return canvas_left < x < (canvas_left)+blockWidth*scaling  

    def clear(self):
        global handholds 
        global selected 
        handholds = []
        selected = -1
        canvas_origin = 0
        self.resize()   

    def createJSON(self): 
        new_id = 0
        for link in glob('levels/' + self.directory[7:] + 'block*'): 
            if int(link[-6]) > new_id: 
                new_id = int(link[-6])
            pass
        new_id += 1
        print "Creating " + 'levels/' + self.directory[7:] + 'block'+str(new_id)+'.json'
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
                'width': handhold_width * hold.scale /3.0, 
                'height': handhold_width * hold.scale /3.0, 
                'positionX': (hold.pos[0])/scaling, 
                'positionY': (hold.pos[1])/scaling, 
                "friction": 1, 
                "restitution": 1, 
                "crumble": False
            }

        s = str(level).replace("'", '"'); 

        new_file.write(s)
        new_file.close()
        #self.clear()

    def changeDirectory(self, btn):
        self.directory = "assets/" + btn + "/"
        self.resize() 

    def removeBadHandholds(self): 
        for hold in handholds: 
            if hold.pos[1] + canvas_origin > self.blockHeight*scaling: 
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