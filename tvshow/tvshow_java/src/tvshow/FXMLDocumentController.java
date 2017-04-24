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
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Root
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label label;

    private BufferedInputStream file;
    private WritableImage image;

    int s_width;
    int s_height;
    Stage stage;

    @FXML
    private AnchorPane ap;
    @FXML
    private Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //   ap.get
    }

    void load(Stage stage, File cfile) throws Exception {
        this.stage = stage;

        //String path = "C:\\PSP\\ppsspp\\memstick\\PSP\\VIDEO\\fd1492440010-38130.tvi";
        file = new BufferedInputStream(new FileInputStream(cfile), 8 * 1024 * 1024);

        byte version = (byte) file.read();
        byte intsize = (byte) file.read();

        s_width = file.read() & 0xFF;
        s_width |= (file.read() & 0xFF) << 8;

        file.read();
        file.read();

        s_height = file.read() & 0xFF;
        s_height |= (file.read() & 0xFF) << 8;

        file.read();
        file.read();

        file.read();
        file.read();
        file.read();
        file.read();

        stage.setWidth(s_width + 50);
        stage.setHeight(s_height + 50);

        canvas.setWidth(stage.getWidth());
        canvas.setHeight(stage.getHeight());
        canvas.getGraphicsContext2D().fillText("AAABBB", 50, 50);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    if (!next()) {
                        this.stop();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();

    }

    private int nextcount = 0;

    private boolean next() throws Exception {
        if (file.available() > 0) {
            int ms = file.read() & 0xFF;
            ms |= (file.read() & 0xFF) << 8;
            ms |= (file.read() & 0xFF) << 16;
            ms |= (file.read() & 0xFF) << 24;

            stage.setTitle(Integer.toString(ms) + " (" + this.s_width + "x" + this.s_height + ")  " + (++nextcount));

            image = new WritableImage(s_width, s_height);
            PixelWriter writer = image.getPixelWriter();

            for (int h = 0; h < s_height; h++) {
                for (int w = 0; w < s_width; w++) {

                    int rgb = (file.read() & 0xFF) << 16;
                    rgb |= (file.read() & 0xFF) << 8;
                    rgb |= (file.read() & 0xFF);
                    rgb |= (0xFF) << 24;
                    writer.setArgb(w, h, rgb);

                    //writer.setColor(w, h, Color.rgb(file.read() & 0xFF, file.read() & 0xFF, file.read() & 0xFF));
                }
            }
            canvas.getGraphicsContext2D().drawImage(image, 0, 0);
            return true;
        } else {
            image = new WritableImage(s_width, s_height);
            PixelWriter writer = image.getPixelWriter();

            for (int h = 0; h < s_height; h++) {
                for (int w = 0; w < s_width; w++) {
                    int rgb = 0xAAAAAAAA;
                    writer.setArgb(w, h, rgb);
                }
            }
            stage.setTitle("END." + " (" + this.s_width + "x" + this.s_height + ")  " + (++nextcount));
            canvas.getGraphicsContext2D().drawImage(image, 0, 0);
            return false;
        }

    }

}
