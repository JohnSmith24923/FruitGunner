package FruitGunnerGame;

// Rishi Shah and Kevin Chen; Final ISP project Grade 11 Computer Science; Mr. Harwood
// This game resembles fruit ninja but is a clicking version, you are SHOOTING the fruits

import hsa2.GraphicsConsole;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Cursor;
import java.awt.Point;

public class FruitGunner {
    public static void main(String[] args) {
        new FruitGunner().run();
    }

    //  The Weird SCRW and SCRH numbers are because of the images chosen for the background. They are originally 362 pixels by 225 pixels so the dimensions were multipled by three to keep good resolution of the images. 
    static int SCRW = 1086;
    static int SCRH = 675;
    GraphicsConsole gc = new GraphicsConsole(SCRW, SCRH, "Fruit Gunner");

    // Sets the mouse off screen
    int mx = -200, my = -200;

    // Background image 
    BufferedImage bImg;

    // Bomb object
    int num_of_bombs = 2;
    Images[] bombs = new Images[num_of_bombs];

    // The Score
    int score = 0;
    // The Splattered Image that will be beside the score
    Images ScoreImg;

    // Number of lives lost:
    int num_of_lives_lost = 0;

    // These are the X's that will represent the lives the user has left
    Images redCrossImg;
    Images blueCrossImg;

    // Fruit Images:
    String[] possiblefruits = { "Coconut.png", "Lemon.png", "Passion_Fruit.png", "Pineapple.png", "Tomato.png","Watermelon.png" };
    Images[] fruits = new Images[possiblefruits.length];

    // Explosion Image for the Bomb
    BufferedImage expimg; 
    
    // This is for the intro/starting screen. This is the image that is the background with the ring and text and the fruit that will be clicked in the middle of the ring
    BufferedImage introImg;
    Images introFruit; 

    // Same thing repeated from Intro Screen to End Screen
    BufferedImage EndImg;
    Images endFruit;

    FruitGunner() {
        // Loading and making the images as well as running the setup method to create the window perfectly
        setup();
        bImg = Images.loadImage("BackgroundImage.png");
        makefruits();
        makeBombs();
        redCrossImg = new Images(0, 10, 100, 100, "RedCross.png");
        blueCrossImg = new Images(0, 10, 100, 100, "BlueCross.png");

        ScoreImg = new Images(10, 10, 100,100,"Coconut.png");
        introImg = Images.loadImage("IntroPage.png");
        EndImg = Images.loadImage("EndImage.png");
        introFruit = new Images(SCRW/2-65, SCRH-300, 125,125,"Watermelon.png");
        endFruit = new Images(SCRW/2-55, SCRH-240, 112,112,"Passion_Fruit.png");
    }

    // This is the main game loop   
    void run() {    
        // This runs the start screen code that won't let any of the future code run until the fruit is clicked
        startScreen();
        while (true) {
            // Update Fruit and Bomb Movement
            fruitMovement(); 
            bombMovement();
            drawGraphics(); // Draw the updated positions

            // Checks if the mouse is clicked and sees if there was a fruit collision or bomb collision
            if (gc.getMouseClick() > 0) {
                checkFruitCollision();
                if(BombCollision()){
                    // If a bomb is clicked, the gameover loop is called by breaking out of the loop
                    break;
                }
            }
            // Checks if any of the fruits fell through the screen without being clicked and turned to a splat, if so, it gets rid of a life
            fruitFall();

            // This checks if all of the fruits have fallen either splatted or fell through the screen, if so, it respawns all of the fruits; This creates waves of new fruit 
            if (checkFallen()){
                for (int i = 0; i<fruits.length; i++){
                    respawn(i);
                }
            }

            // If the player runs out of lives, the gameover loop is called by breaking out of the loop
            if (num_of_lives_lost == 3){
                break; 
            }
            gc.sleep(10);
        }
        // This is the gameover that loops until the rematch button is clicked
        gameover();
    }

    void setup() {
        gc.setLocationRelativeTo(null);
        gc.setAntiAlias(true);
        gc.enableMouse(); // detect mouse click
        gc.enableMouseMotion();
        gc.setStroke(6);

        // Gets rid of the regular curser
        Toolkit t = Toolkit.getDefaultToolkit();
        Image im = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Cursor noCursor = t.createCustomCursor(im, new Point(0, 0), "none");
        gc.setCursor(noCursor);
    }

    void drawGraphics() {
        synchronized (gc) {
            gc.clear();
            // Draws the background image
            gc.drawImage(bImg, 0, 0, SCRW, SCRH);

            // Draws all the fruit images unless if the fruit.splat has been turned to true
            for (Images fruit : fruits) {
                if (fruit.splat == true) {
                    gc.drawImage(fruit.imgSplat, fruit.x, fruit.y, fruit.width, fruit.height);
                } else {
                    gc.drawImage(fruit.img, fruit.x, fruit.y, fruit.width, fruit.height);
                }
            }
            // Draws bombs
            for (Images bomb : bombs) {
                gc.drawImage(bomb.img, bomb.x, bomb.y, bomb.width, bomb.height);
            }
            // Updates the X's 
            loseLives();
            // Draws the score
            score();
            // Mouse cursor that acts as crosshairs
            MouseCursor();
        }
    }
    void MouseCursor(){
        // Draws the Crosshair mouse
        gc.setColor(Color.WHITE);
        mx = gc.getMouseX();
        my = gc.getMouseY();
        gc.drawOval(mx - 25, my - 25, 50, 50);
        gc.drawLine(mx - 25, my, mx + 25, my);
        gc.drawLine(mx, my - 25, mx, my + 25);
    }

    void makefruits() {
        for (int i = 0; i < fruits.length; i++) {
            int fruitIndex = (int) (Math.random() * possiblefruits.length);
            String fruit = possiblefruits[fruitIndex]; // Choose a random fruit from the array possible fruits
            int initialX = (int) (Math.random() * (SCRW - 250)); // Random x position
            int initialY = SCRH + (int) (Math.random() * SCRH); // Start below the screen
            int speed =  ((int)(Math.random() * 3) + 1); // Random speed

            fruits[i] = new Images(initialX, initialY, 125, 125, fruit);
            fruits[i].yspeed = speed;
            fruits[i].xspeed = speed;
        }
    }

    void fruitMovement() {
        int fallThreshold = SCRH / 3; // Set the fall threshold to one third of the screen height

        for (Images fruit : fruits) {
            if (fruit.y < fallThreshold) {
                // Check if the fruit should start falling
                boolean shouldfall = Math.random() < 0.05;
                if (shouldfall && !fruit.trigger) {
                    fruit.yspeed *= -1; // Reverse the fruit's vertical speed
                    fruit.trigger = true; // Set the trigger to true
                }
            }
            
            if (fruit.splat == false) {
                fruit.y += fruit.yspeed; // Move the fruit upward based on its speed
                fruit.x += fruit.xspeed; // Move the fruit horizontally based on its speed   
            }
            
            if (fruit.x + fruit.width >= SCRW && fruit.xspeed > 0 || fruit.x < 0 && fruit.xspeed < 0) {
                // If the fruit reaches the screen edges horizontally, reverse its horizontal speed
                fruit.xspeed *= -1;
            }
            
            if (fruit.y > SCRH && fruit.yspeed > 0) {
                fruit.x = (int) (Math.random() * SCRW); // Random x position    
                fruit.y = SCRH; // Reset the fruit position to the top of the screen
                fruit.trigger = false; // Reset the trigger flag
                fruit.yspeed *= -1; // Reverse the fruit's vertical speed
            }

            // Make sure the fruit falls before it reaches the top of the screen
            if (fruit.y < 100 && fruit.yspeed < 0) {
                fruit.yspeed *= -1; // Reverse the fruit's vertical speed
                fruit.trigger = true; // Set the trigger flag to true
            }
        }
    }

    void checkFruitCollision() {
        // Checks if the fruit is clicked and makes the score go up while making sure that the fruit gets splat
        mx = gc.getMouseX();
        my = gc.getMouseY();
        for (int i = 0; i < fruits.length; i++) {
            if (fruits[i].contains(mx, my) && fruits[i].splat == false) {
                fruits[i].splat = true;
                score++;
            }
        }
    }

    void fruitFall() {
        // If a fruit falls and hits the SCRH before getting clicked, the number of lives lost goes up, hence meaning that the player loses a life. 
        for (int i = 0; i < fruits.length; i++) {
            if (fruits[i].y >= SCRH && fruits[i].trigger == true) {
                fruits[i].y = SCRH; // Reset the fruit position
                fruits[i].splat = true; // Makes sure that the fruit doesn't just respawn
                num_of_lives_lost++;
                fruits[i].trigger = false;
            }
        }
    }

    boolean checkFallen(){
        // This checks if all fruits have been splatted either by being clicked or touching the SCRH
        for (int i = 0; i < fruits.length; i++){
            if (fruits[i].splat == false){
                return false;
            }
        }
        return true;
    }

    void respawn(int i) {
        // Respawns all the fruits and creates the next wave
        fruits[i].splat = false;
        fruits[i].x = (int) (Math.random() * SCRW); // Random x position
        fruits[i].y = SCRH; // Start below the screen

        // New Random Fruit
        int fruitIndex = (int) (Math.random() * possiblefruits.length);
        fruits[i].fruit = possiblefruits[fruitIndex];
        // This will make the xspeed and yspeed, whether negative or positive to be faster but making sure that it doesn't go past speed 6 - makes the game more playable as beyond speed 6 is extremely difficult
        if (fruits[i].xspeed>0 && fruits[i].xspeed < 6){
            fruits[i].xspeed ++;
        }
        if (fruits[i].xspeed<0 && fruits[i].xspeed > -6){
            fruits[i].xspeed --;
        }
        if (fruits[i].yspeed>0 && fruits[i].yspeed < 6){
            fruits[i].yspeed ++;
        }
        if (fruits[i].yspeed<0 && fruits[i].yspeed > -6){
            fruits[i].yspeed --;
        }
        fruits[i].splatImage = fruits[i].fruit.replace(".", "_Splatter.");
        if (!fruits[i].fruit.equals("bomb.png")) {
            fruits[i].imgSplat = Images.loadImage(fruits[i].splatImage);
        }
        fruits[i].img = Images.loadImage(fruits[i].fruit);
        drawGraphics();
        gc.sleep(50);
    }
    
    void makeBombs() {
        for (int i = 0; i < bombs.length; i++) {
            int initialX = (int) (Math.random() * (SCRW - 250)); // Random x position
            int initialY = SCRH + (int) (Math.random() * SCRH); // Start below the screen
            int speed = (int) ((Math.random() * 6) + 3); // Random speed 

            bombs[i] = new Images(initialX, initialY, 150, 150, "bomb.png"); 
            bombs[i].yspeed = speed;
            bombs[i].xspeed = (int) ((Math.random() * 3) + 1);
        }
    }

    void bombMovement(){
        int fallThreshold = SCRH / 3; // Set the fall threshold to a third of the screen height
        for (Images bomb : bombs) {
            if (bomb.y < fallThreshold) {
                boolean shouldfall = Math.random() < 0.05; // Tries to find when to drop and randomizes the drop
                if (shouldfall == true && bomb.trigger == false) {
                    bomb.yspeed *= -1;
                    bomb.trigger = true;
                }
            }
            bomb.y += bomb.yspeed; 
            bomb.x += bomb.xspeed; 

            if (bomb.x + bomb.width >= SCRW && bomb.xspeed > 0 || bomb.x < 0 && bomb.xspeed < 0) {
                bomb.xspeed *= -1;
            }

            // Bounces the ball if it touches the bottom of the screen
            if (bomb.y > SCRH && bomb.yspeed > 0) {
                bomb.x = (int) (Math.random() * SCRW); // Random x position
                bomb.y = SCRH;
                bomb.trigger = false;
                bomb.yspeed *= -1;
            }   
            // Make sure that it falls before it reaches the top of the screen
            if (bomb.y < 100 && bomb.yspeed < 0) {
                bomb.yspeed *= -1;
                bomb.trigger = true;
            }
        }
    }

    boolean BombCollision() {
        int explosionSize = 1400; // The size of the explosion image

        for (int i = 0; i < bombs.length; i++) {
            if (bombs[i].contains(mx, my)) {
                expimg = Images.loadImage("Explosion.png");
                gc.drawImage(expimg, mx - explosionSize / 2, my - explosionSize / 2, explosionSize, explosionSize);
                gc.sleep(600);
                return true;
            } 
        }
        return false;
    }

    void loseLives() {
        // If the number of lives lost goes up, draw a Red Cross Image and replace one of the blue cross images. This shows how you have 3 lives at first, but as soon as you miss a fruit, a life is lost and a red cross replaces one of the blue crosses
        for (int i = 0; i < 3; i++) {
            if (i < num_of_lives_lost){
                redCrossImg.x = 75 * i + SCRW -260;
                gc.drawImage(redCrossImg.img,redCrossImg.x , blueCrossImg.y, redCrossImg.width, redCrossImg.height);
            }
            else{
                blueCrossImg.x = 75 * i + SCRW-260;
                gc.drawImage(blueCrossImg.img, blueCrossImg.x, blueCrossImg.y, blueCrossImg.width, blueCrossImg.height);
            }
        } 
    }
    
    void score() {
        //Draws the score onto the screen with a splatted coconut image 
        gc.setColor(Color.yellow);
        gc.drawImage(ScoreImg.imgSplat,ScoreImg.x,ScoreImg.y,ScoreImg.width,ScoreImg.height);
        Font Score = new Font("Arial", Font.BOLD, 75);
        gc.setFont(Score);
        gc.drawString("  " + score, 80, 80);
    }

    boolean startScreen(){
        // This is the code for the starting screen where you have to press on the screen to load to the main game loop. 
        while(true){ 
            synchronized(gc){
                gc.clear();
                // Draws the intro screen image and the intro fruit 
                gc.drawImage(introImg, 0,0,SCRW,SCRH);
                gc.drawImage(introFruit.img, introFruit.x,introFruit.y, introFruit.width,introFruit.height);
                MouseCursor();
                if (gc.getMouseClick() > 0) {
                    mx = gc.getMouseX();
                    my = gc.getMouseY();
                    if (introFruit.contains(mx, my)){
                        // If the introfruit is clicked on, the image splat is shown and then the main game loop is run
                        introFruit.splat = true;
                        gc.drawImage(introFruit.imgSplat,introFruit.x,introFruit.y,introFruit.width, introFruit.height);
                        gc.sleep(200);
                        return true;
                    }
                }
            }
            gc.sleep(10);
        }
    }

    void gameover(){
        while(true){
            synchronized(gc){
                // This is the end game loop which won't be broken until the rematch button is clicked 
                gc.clear();
                //Draws the background image and the passionfruit associated to act as the respawn button
                gc.drawImage(EndImg, 0,0,SCRW,SCRH ); 
                gc.drawImage(endFruit.img,endFruit.x,endFruit.y,endFruit.width,endFruit.height);
                // This checks if the passionfruit is clicked and if so, it breaks out of the gameover loop and runs rematch which will then run the main game loop
                if (gc.getMouseClick()>0){
                    mx = gc.getMouseX();
                    my = gc.getMouseY();
                    if (endFruit.contains(mx, my)){
                        endFruit.splat = true;
                        gc.drawImage(endFruit.imgSplat, endFruit.x, endFruit.y, endFruit.width, endFruit.height);
                        gc.sleep(200);
                        break;
                    }
                }
                // Draws the score, the lives lost and the cursor
                score();
                loseLives();
                MouseCursor();
            }
            gc.sleep(10);
        }
        // Runs rematch if broken out of while(true) loop
        rematch();
    }

    void rematch(){
        //Resets the game to be run again
        score = 0; 
        num_of_lives_lost =0;
        makeBombs();
        makefruits();
        run();
    }
}

