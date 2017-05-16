/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tvshow;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Root
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label label;

    private WritableImage image;

    ImageList imageList;
    Stage stage;

    @FXML
    private AnchorPane ap;
    @FXML
    private Canvas canvas;

    private boolean anTimer = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    void load(Stage stage, File cfile) throws Exception {
        this.stage = stage;

        //String path = "C:\\PSP\\ppsspp\\memstick\\PSP\\VIDEO\\fd1492440010-38130.tvi";
        BufferedInputStream file = new BufferedInputStream(new FileInputStream(cfile), 8 * 1024 * 1024);

        byte[] bs = new byte[file.available()];
        file.read(bs);
        file.close();
        imageList = new ImageList(bs);

        stage.setWidth(2 * imageList.s_width + 50);
        stage.setHeight(2 * imageList.s_height + 50);

        canvas.setWidth(stage.getWidth());
        canvas.setHeight(stage.getHeight());

        canvas.getGraphicsContext2D().fillText("AAABBB", 50, 50);

        canvas.setOnMouseMoved((e) -> {
            if (!anTimer) {
                WritableImage timage = canvas.snapshot(new SnapshotParameters(), null);
                int rgb = timage.getPixelReader().getArgb((int) e.getX(), (int) e.getY());
                int tmr = (rgb & 0xFF0000) >> 16;
                int tmg = (rgb & 0xFF00) >> 8;
                int tmb = (rgb & 0xFF);
                stage.setTitle(tmr + " " + tmg + " " + tmb);
            }
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    if (!next()) {
                        this.stop();
                        anTimer = false;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();

    }

    private int nextcount = 0;

    private boolean next() throws Exception {
        if (imageList.size > nextcount) {
            ImageList.Image bsimage = imageList.getImage(nextcount++);

            stage.setTitle(Integer.toString(bsimage.getTime()) + " (" + bsimage.getWidth() + "x" + bsimage.getHeight() + ")  "
                    + (nextcount));

            image = new WritableImage(bsimage.getWidth(), bsimage.getHeight());
            PixelWriter writer = image.getPixelWriter();

            for (int h = 0; h < bsimage.getHeight(); h++) {
                for (int w = 0; w < bsimage.getWidth(); w++) {
                    int rgb = bsimage.getRGB(w, h);
                    writer.setArgb(w, h, rgb);
                }
            }
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
            return true;
        } else {
            stage.setTitle("END." + " (" + image.getWidth() + "x" + image.getHeight() + ")  " + (++nextcount));
            return false;
        }

    }

}
