/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tvshow;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.scene.media.Track;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    private Scene mp4(Stage stage, File cfile) {

        Media media = new Media(cfile.toURI().toString());
        javafx.scene.media.MediaPlayer mediaPlayer = new javafx.scene.media.MediaPlayer(media);

        MediaView mediaView = new MediaView(mediaPlayer);
        Group root = new Group();

        Scene scene = new Scene(root);
        root.getChildren().add(mediaView);

        final String savepath = cfile.getAbsolutePath() + ".tvi";

        mediaPlayer.setOnReady(() -> {
            stage.sizeToScene();
            System.out.println("Size> " + media.getWidth() + "," + media.getHeight());
        }
        );

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            long start = (long) mediaPlayer.getCurrentTime().toMillis();

            @Override
            public void invalidated(Observable observable) {
                long end = (long) mediaPlayer.getCurrentTime().toMillis();
                if (end > (start + 60)) {
                    start = end;

                    //System.out.println("Playing>" + start);
                }
            }

        });

        mediaPlayer.play();
        return scene;
    }
}
