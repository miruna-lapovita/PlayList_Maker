package com.example.radio;

import Domain.Song;
import Repository.SQLSongRepository;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

public class HelloApplication extends Application {
    private SQLSongRepository songRepository;
    private ObservableList<Song> songList;
    private ListView<Song> songListView;
    private TextField filterTextField;
    private Button resetButton;


    @Override
    public void start(Stage stage) throws Exception{

        songRepository = new SQLSongRepository();
        songList = FXCollections.observableArrayList();
        loadSongs();

        songListView = new ListView<>(songList);
        songListView.setCellFactory(param->new ListCell<>(){
            @Override
            protected void updateItem(Song song, boolean empty){
                super.updateItem(song, empty);
                if(empty || song == null){
                    setText(null);
                }else{
                    setText(song.getBand() + " - " + song.getTitle() + " [" + song.getGenre() + "] (" + song.formatDuration(song.getDuration()) + ")");

                }
            }
        });


        TextField playlistNameField = new TextField();
        playlistNameField.setPromptText("Introduceti numele listei de redare");


        Button generatePlaylistButton = new Button("Genereaza Lista de Redare");
        generatePlaylistButton.setOnAction(e -> generatePlaylist(playlistNameField.getText()));


        filterTextField = new TextField();
        filterTextField.setPromptText("Caută piese...");


        resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetFilter());


        filterTextField.setOnKeyReleased(e -> filterSongs());


        HBox topBar = new HBox(10, filterTextField, resetButton);
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(songListView);


        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Budget Spotify");
        stage.setScene(scene);
        stage.show();


        HBox bottomBar = new HBox(10, playlistNameField, generatePlaylistButton);
        root.setBottom(bottomBar);
    }

    public void generatePlaylist(String playlistName) {
        if (playlistName == null || playlistName.isEmpty()) {
            showError("Eroare", "Numele listei de redare nu poate fi gol.");
            return;
        }


        ArrayList<Song> availableSongs = new ArrayList<>(songRepository.getAllEntities());
        ArrayList<Song> playlistSongs = new ArrayList<>();
        Duration totalDuration = Duration.ZERO;
        Song lastSong = null;

        while (totalDuration.toMinutes() < 15 && !availableSongs.isEmpty()) {
            Song song = getRandomSong(availableSongs);


            if ((lastSong == null) ||
                    (!lastSong.getBand().equals(song.getBand()) && !lastSong.getGenre().equals(song.getGenre()))) {

                playlistSongs.add(song);
                totalDuration = totalDuration.plus(song.getDuration());
                availableSongs.remove(song);
                lastSong = song;
            }
        }

        if (totalDuration.toMinutes() >= 15) {

            savePlaylistToDatabase(playlistName, playlistSongs);
        } else {
            showError("Eroare", "Nu s-a putut crea lista de redare conform cerințelor.");
        }
    }

    private void savePlaylistToDatabase(String playlistName, ArrayList<Song> playlistSongs) {
        try {
            String tableName = playlistName.replaceAll("\\s+", "_").toLowerCase(); // Numele tabelului
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "band VARCHAR(100), " +
                    "title VARCHAR(100), " +
                    "genre VARCHAR(100), " +
                    "duration VARCHAR(5));";

            Statement stmt = songRepository.getConnection().createStatement();
            stmt.executeUpdate(createTableSql);


            String insertSql = "INSERT INTO " + tableName + " (band, title, genre, duration) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = songRepository.getConnection().prepareStatement(insertSql)) {
                for (Song song : playlistSongs) {
                    ps.setString(1, song.getBand());
                    ps.setString(2, song.getTitle());
                    ps.setString(3, song.getGenre());
                    ps.setString(4, song.formatDuration(song.getDuration()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            showError("Eroare", "Nu s-a putut salva lista de redare.");
        }
    }

    private Song getRandomSong(ArrayList<Song> availableSongs) {
        Random random = new Random();
        int index = random.nextInt(availableSongs.size());
        return availableSongs.get(index);
    }


    private void loadSongs() {
        songList.clear();
        try {

            for (Song song : songRepository.getAllEntities()) {
                insertSorted(song);
            }
        } catch (Exception e) {
            showError("Eroare", "Nu s-au putut încărca piesele din baza de date.");
        }
    }

    private void insertSorted(Song song) {

        int index = 0;
        for (Song s : songList) {
            if (s.getBand().compareToIgnoreCase(song.getBand()) > 0 ||
                    (s.getBand().equalsIgnoreCase(song.getBand()) && s.getTitle().compareToIgnoreCase(song.getTitle()) > 0)) {
                break;
            }
            index++;
        }
        songList.add(index, song);
    }



    private void filterSongs() {
        String query = filterTextField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadSongs();
            return;
        }

        ObservableList<Song> filteredList = FXCollections.observableArrayList();
        for (Song song : songRepository.getAllEntities()) {
            if (song.getBand().toLowerCase().contains(query) ||
                    song.getTitle().toLowerCase().contains(query) ||
                    song.getGenre().toLowerCase().contains(query) ||
                    song.formatDuration(song.getDuration()).contains(query)) {
                filteredList.add(song);
            }
        }

        if (filteredList.isEmpty()) {
            showError("Căutare eșuată", "Nu au fost găsite piese care să corespundă căutării.");
        } else {
            songList.setAll(filteredList);
        }
    }

    private void resetFilter() {
        filterTextField.clear();
        loadSongs();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public static void main(String[] args) {
        launch();
    }

}

