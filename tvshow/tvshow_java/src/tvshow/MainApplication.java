/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tvshow;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Root
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        File cfile = chooser.showOpenDialog(stage);

        Scene scene;
        if (cfile.getName().endsWith(".mp4")) {
            scene = mp4(stage, cfile);
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
            Parent root = fxmlLoader.load();
            ((FXMLDocumentController) fxmlLoader.getController()).load(stage, cfile);
            scene = new Scene(root);
        }
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private ArrayList<WritableImage> images = new ArrayList<>();
    private ArrayList<Integer> times = new ArrayList<>();

    private Scene mp4(Stage stage, File cfile) {

        Media media = new Media(cfile.toURI().toString());
        javafx.scene.media.MediaPlayer mediaPlayer = new javafx.scene.media.MediaPlayer(media);
        mediaPlayer.setCycleCount(1);

        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitWidth(480);
        mediaView.setFitHeight(272);
        Group root = new Group();

        Scene scene = new Scene(root);
        root.getChildren().add(mediaView);

        final String savepath = cfile.getAbsolutePath() + ".tvi";

        mediaPlayer.setOnReady(() -> {
            stage.sizeToScene();
            System.out.println("Size> " + media.getWidth() + "," + media.getHeight());
        }
        );
        mediaPlayer.setOnEndOfMedia(() -> {
            try {
                BufferedOutputStream file = new BufferedOutputStream(new FileOutputStream(savepath), 8 * 1024 * 1024);

                byte version = 1;
                file.write(version);
                byte intsize = 4;
                file.write(intsize);

                int s_width = (int) images.get(0).getWidth();
                file.write(s_width & 0xFF);
                file.write((s_width >> 8) & 0xFF);
                file.write(0);
                file.write(0);

                int s_height = (int) images.get(0).getHeight();
                file.write(s_height & 0xFF);
                file.write((s_height >> 8) & 0xFF);
                file.write(0);
                file.write(0);

                file.write(0);
                file.write(0);
                file.write(0);
                file.write(0);

                for (int i = 0; i < images.size(); i++) {
                    int time = times.get(i);
                    file.write(time & 0xFF);
                    file.write((time >> 8) & 0xFF);
                    file.write((time >> 16) & 0xFF);
                    file.write((time >> 24) & 0xFF);

                    PixelReader reader = images.get(i).getPixelReader();

                    for (int h = 0; h < s_height; h++) {
                        for (int w = 0; w < s_width; w++) {

                            int rgb = reader.getArgb(w, h);
                            file.write((rgb >> 16) & 0xFF);
                            file.write((rgb >> 8) & 0xFF);
                            file.write((rgb) & 0xFF);

                        }
                    }
                }

                file.flush();
                file.close();

            } catch (Exception ex) {
                Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("End> " + images.size());
        });

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            long start = (long) mediaPlayer.getCurrentTime().toMillis();

            @Override
            public void invalidated(Observable observable) {
                long end = (long) mediaPlayer.getCurrentTime().toMillis();
                if (end > (start + 60)) {
                    start = end;

                    //ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                    WritableImage image = mediaView.snapshot(new SnapshotParameters(), null);
                    System.out.println("Playing>" + start + " -" + image.getPixelReader().getArgb(10, 10)
                            + "(" + image.getWidth() + "x" + image.getHeight() + ")");
                    images.add(image);
                    times.add((int) start);
                }
            }

        });

        mediaPlayer.play();
        return scene;
    }
}
