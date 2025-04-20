package Repository;

import Domain.Song;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;


public class SQLSongRepository extends Repository<Song> implements I_Repository<Song>{
    private static final String JDBC_URL = "jdbc:sqlite:src/main/resources/songdatabase.db";
    private Connection conn = null;

    public SQLSongRepository() throws SQLException {
        openConnection();
        createSchema();
        loadData();
        populateDatabase();
    }

    private void loadData(){
        try{
            try(PreparedStatement statement = conn.prepareStatement("SELECT * FROM song");//obtinem randurile din tabel
                ResultSet rs = statement.executeQuery();){
                while(rs.next()){
                    Song song = new Song(rs.getInt("id"), rs.getString("band"), rs.getString("title"), rs.getString("genre"), parseDuration(rs.getString("duration")));
                    entities.add(song);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public Duration parseDuration(String duration){
        try{
            String[] parts = duration.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return Duration.ofMinutes(minutes).plusSeconds(seconds);
        } catch (Exception e) {
            throw new IllegalArgumentException("Durata nu este în formatul corect (minute:secunde). Exemplu: 03:45", e);
        }
    }

    public String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void openConnection(){//creeaza un sqlitedatasource si seteaza url ul bazei de date
        try{
            SQLiteDataSource ds = new SQLiteDataSource();
            ds.setUrl(JDBC_URL);
            if(conn == null || conn.isClosed())//deschidem conexiunea daca este inchisa
                conn = ds.getConnection();
        }catch(SQLException e ){
            throw new RuntimeException("Eroare la conectarea cu baze de date", e);
        }
    }

    private void createSchema() {
        try{//creeaza tabela car daca nu exista
            try (final Statement stmt = conn.createStatement()){
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS song(id int, band varchar(30), title varchar(30), genre varchar(30), duration varchar(5) )");
            }
        }catch(SQLException e){
            System.err.println("[ERROR] createSchema: " + e.getMessage());
        }
    }



    @Override
    public void addEntity(Song song) throws Exception {
        super.addEntity(song);
        try{
            try(PreparedStatement statement = conn.prepareStatement("INSERT INTO song Values(?, ?, ?, ?, ?)")){
                statement.setInt(1, song.getId());
                statement.setString(2, song.getBand());
                statement.setString(3, song.getTitle());
                statement.setString(4, song.getGenre());
                statement.setString(5, formatDuration(song.getDuration()));
                statement.executeUpdate();
            }
        }catch(SQLException e){
            throw new Exception("Eroare la salvarea in baza de date", e);
        }
    }

    public Song getEntityById(int id) {
        Song song = super.getEntityById(id);
        if (song != null) {
            return song;
        }
        try {//daca nu e gasit in lista, facem interogare pe baza de date
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM song WHERE id = ?")) {
                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        String durationString = rs.getString("duration");
                        Duration duration = parseDuration(durationString);
                        return new Song(
                                rs.getInt("id"),
                                rs.getString("band"),
                                rs.getString("title"),
                                rs.getString("genre"),
                                duration
                        );
                    }
                }
            }
        }catch(SQLException e){
            System.err.println("[ERROR] getEntityById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public ArrayList<Song> getAllEntities(){
        entities.clear();
        loadData();
        return super.getAllEntities();

    }

    public void close() throws Exception{
        try{
            if(conn != null)
                conn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void populateDatabase() throws SQLException {
        conn.setAutoCommit(false);

        String sql = "INSERT INTO song (id, band, title, genre, duration) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            Song[] songs = new Song[]{
                    new Song(1, "Queen", "Bohemian Rhapsody", "Rock", Duration.ofMinutes(5).plusSeconds(55)),
                    new Song(2, "The Beatles", "Hey Jude", "Rock", Duration.ofMinutes(7).plusSeconds(11)),
                    new Song(3, "Michael Jackson", "Thriller", "Pop", Duration.ofMinutes(5).plusSeconds(57)),
                    new Song(4, "Adele", "Hello", "Pop", Duration.ofMinutes(4).plusSeconds(55)),
                    new Song(5, "Metallica", "Enter Sandman", "Metal", Duration.ofMinutes(5).plusSeconds(32)),
                    new Song(5, "Metallica", "altcv", "Metal", Duration.ofMinutes(5).plusSeconds(32))
            };

            // Insereaza fiecare melodie
            for (Song song : songs) {
                preparedStatement.setInt(1, song.getId());
                preparedStatement.setString(2, song.getBand());
                preparedStatement.setString(3, song.getTitle());
                preparedStatement.setString(4, song.getGenre());
                preparedStatement.setString(5, song.formatDuration(song.getDuration()));
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    public Connection getConnection() {
        return conn;
    }



}
