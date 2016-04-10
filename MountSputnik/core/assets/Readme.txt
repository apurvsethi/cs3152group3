Readme.txt for levelEditor 


GUI: 

scale in the top right controls block height. The picture of the character is there for reference on how big your block will be (approx.)

The three parameters under the scale (crumble, slip, move) are not yet implemented 

The button in the bottom left is a drop down to choose a level. Currently, the only one available is canyon. Choosing a level will later affect what parameters and art assets are available to the user. 

Handhold scale controls the size of a handhold that you are about to draw or that you've selected 

Delete will delete a selected handhold 

Submit will create your json and store it in the proper directory 

Clear will clear the board 


LEVEL: 

Click anywhere in your canvas to place a hold. The size is determined by the handhold scale. Clicking on a placed handhold will select it. Selected handholds can be resized, deleted, or moved (click and drag). If you resize the window, handholds that are out of bounds will be deleted automatically. 


JSON OUTPUT: 

The difficulty is set to "notRanked" because we still need to playtest and determine this. 

All scales and box2D units in the json are based on the assumption that the screen width is 32.0 meters. 

handhold texture names are changed slightly and standardized. 

the screen height is also in Box2D meters rather than a number like 1 or 2 