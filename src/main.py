from kivy.app import App
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.gridlayout import GridLayout
from kivy.uix.image import Image
from kivy.uix.switch import Switch
from kivy.config import Config

Config.set('graphics', 'resizable', True)

# Google Pixel Viewport
# Config.set('graphics', 'width', '412')
# Config.set('graphics', 'height', '732')

# start button
# counter
class home(GridLayout):
    def __init__(self, **kwargs):
        super(home, self).__init__(**kwargs)
        self.cols = 1
        self.padding = '30sp'
        self.add_widget(Label(text="Welcome to People Counter",
                              size_hint_y=None,
                              height=50, 
                              font_size='50sp'))
        
        self.add_widget(Label(text="Invention Studio",
                              size_hint_y=None,
                              height=50, 
                              font_size='30sp'))
        
        self.image = Image(source='library_people_img.jpg',
                           width=150,
                           height=150)
        self.add_widget(self.image)

        self.add_widget(Label(text="Number of People Currently:",
                              size_hint_y=None,
                              height=50, 
                              font_size='30sp'))
        
        self.num_people = Label(text="3",
                              size_hint_y=None,
                              height=50, 
                              font_size='50sp')
        self.add_widget(self.num_people)

        self.add_widget(Label(text="Average Number of People:",
                              size_hint_y=None,
                              height=50, 
                              font_size='30sp'))
        
        self.avg_people = Label(text="4",
                              size_hint_y=None,
                              height=50, 
                              font_size='50sp')
        self.add_widget(self.avg_people)
        
        self.switch = Switch()
        self.add_widget(self.switch)



        # self.top_grid.add_widget(Label(text=" ", 
		# 	font_size=32,
		# 	size_hint_y = None,
		# 	height=50,
		# 	size_hint_x = None,
		# 	width=200
		# ))


    # def press(self, instance):
       
       


class MyApp(App):
    def build(self):
        return home()


if __name__ == "__main__":
    MyApp().run()