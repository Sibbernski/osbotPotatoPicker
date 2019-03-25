
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;

import org.osbot.rs07.script.ScriptManifest;
import scripts.Timer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@ScriptManifest(name = "SibbernskiPotatoArmy", author = "Sibbernski", version = 1.0, info = "revolutionary potato picking army script", logo = "")

public class SibbernskiPotatoArmy extends Script {

    public static int countPotatoes;
    private long startTime, millis, hours, minutes, seconds;

    public Area potatoField = new Area(3140, 3289, 3155, 3271);
    public String status = "Unspecified";

    scripts.Timer time = new Timer(3000);

    BufferedImage progressImage;

    @Override
    public void onStart() {

        try {
            progressImage = ImageIO.read(SibbernskiPotatoArmy.class.getResourceAsStream("/resources/sibpotato.png"));
        } catch (IOException e){
            log(e);
        }

        this.startTime = System.currentTimeMillis();
        log("Potato Army started");
    }

    private void updateTimer() {
        millis = System.currentTimeMillis() - startTime;
        hours = millis / (1000 * 60 * 60);
        millis -= hours * (1000 * 60 * 60);
        minutes = millis / (1000 * 60);
        millis -= minutes * (1000 * 60);
        seconds = millis / 1000;
    }

    @Override
    public void onExit() {
        log("Potato Army stopped");

    }


    @Override
    public int onLoop() throws InterruptedException {

        if (!potatoStocked()) {
            pickPotato();
        } else {
            bank();
        }

        return 1000; //The amount of time in milliseconds before the loop starts over
    }

    private void bank() throws InterruptedException {
        if (getInventory().isFull()) {
            status = "Inventory full, banking";
            if (Banks.DRAYNOR.contains(myPosition())) {
                if (getBank().isOpen()) {
                    getBank().depositAll();
                    status = "Depositing potatoes";
                } else {
                    getBank().open();
                    status = "Opening bank";
                }
            } else {
                getWalking().webWalk(Banks.DRAYNOR);
                status = "Inventory full walking to bank";
            }
        }
    }

    private void pickPotato() throws InterruptedException {
        RS2Object potato = getObjects().closest(312);
        if (potatoField.contains(myPosition()) && potato.isVisible()) {
            status = "Clicking potatoes";
            potato.interact("Pick");
            time.reset();
            while (myPlayer().getAnimation() == -1 && time.isRunning()) {
                sleep(100);
                status = "Picking potato";
            }
        } else {
            status = "Not in field walking there";
            getWalking().webWalk(potatoField);
        }
    }

    private boolean potatoStocked() {
        return getInventory().isFull();
    }


    public void onMessage(Message message) throws InterruptedException {
        if (message.getMessage().contains("Hi")) {
            log(message.getUsername() + " " + message.getMessage());
        }

        if (message.getMessage().contains("You pick a potato.")) {
            countPotatoes++;
        }
    }

    private int getPickedHouer() {
        return (int) ((countPotatoes) * 3600000D / (System.currentTimeMillis() - startTime));
    }

    @Override
    public void onPaint(Graphics2D g) {
        updateTimer();
        g.setColor(Color.black);
        g.drawImage(progressImage, null, 5, 340);
        g.drawString("Sibbernski Potato Picker", 185, 380);
        g.drawString("Potatoes picked " + countPotatoes + " (" + getPickedHouer() + ")", 185, 390);
        g.drawString("Time run : " + hours + " : " + minutes + " : " + seconds, 185, 400);
        g.drawString("Status: " + status, 185, 410);
    }

}
