package FruitGunnerGame;

// Rishi Shah and Kevin Chen; Final ISP project Grade 11 Computer Science; Mr. Harwood

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.awt.Rectangle;

class Images extends Rectangle {
    BufferedImage img;
    BufferedImage imgSplat;
    boolean splat;
    String fruit;
    String splatImage;
    int yspeed;
    int xspeed;
    boolean trigger = false;    

    Images(int x, int y, int width, int height, String fruit) {
        super(x, y, width, height);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fruit = fruit;
        // Uses the fruits that are being made to create a new variable called splatImage which wil contain the splatted version of the images
        splatImage = fruit.replace(".", "_Splatter.");
        if (!fruit.equals("bomb.png") && !fruit.equals("BlueCross.png") && !fruit.equals("RedCross.png")) {
            imgSplat = loadImage(splatImage);
        }
        img = loadImage(fruit);
    }

    static BufferedImage loadImage(String filename) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.out.println(e.toString());
            JOptionPane.showMessageDialog(null, "An image failed to load: " + filename, "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }
        return img;
    }
}
