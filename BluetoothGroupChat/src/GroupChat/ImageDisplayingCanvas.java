/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package GroupChat;

import java.io.IOException;
import javax.microedition.lcdui.*;
import java.util.TimerTask;
import java.util.Timer;

/**
 * @author Shreya Sharma
 */
public class ImageDisplayingCanvas extends Canvas implements CommandListener {

    private Image image;
    private Timer timer;
    private removesImage remover;
    private Display disp;
    private Form form;

    public ImageDisplayingCanvas() {
        try {
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            image = Image.createImage("/MiscInfo/PS.jpg");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setFullScreenMode(true);
        timer=new Timer();
        remover=new removesImage();
        timer.schedule(remover, 2000);
    }

    public void setDisplayForm (Display disp, Form form){
        this.disp=disp;
        this.form=form;
    }

    public class removesImage extends TimerTask{

        public void run() {
            disp.setCurrent(form);
        }

    }

    public void paint(Graphics g) {
        g.drawImage(image, (getWidth()-image.getWidth())/2, (getHeight()-image.getHeight())/2, 0);
    }
    
    /**
     * Called when a key is pressed.
     */
    protected  void keyPressed(int keyCode) {
    }
    
    /**
     * Called when a key is released.
     */
    protected  void keyReleased(int keyCode) {
    }

    /**
     * Called when a key is repeated (held down).
     */
    protected  void keyRepeated(int keyCode) {
    }
    
    /**
     * Called when the pointer is dragged.
     */
    protected  void pointerDragged(int x, int y) {
    }

    /**
     * Called when the pointer is pressed.
     */
    protected  void pointerPressed(int x, int y) {
    }

    /**
     * Called when the pointer is released.
     */
    protected  void pointerReleased(int x, int y) {
    }
    
    /**
     * Called when action should be handled
     */
    public void commandAction(Command command, Displayable displayable) {
    }

}