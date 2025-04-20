package Domain;

import java.time.Duration;

public class Song extends Entity{
    private String band;
    private String title;
    private String genre;
    private Duration duration;

    public Song(int id, String band, String title, String genre, Duration duration) {
        super(id);
        this.band = band;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
    }

    public String getBand() {
        return band;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
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

    @Override
    public String toString() {
        return "Song{" + "id" + id +
                "duration=" + duration +
                ", band='" + band + '\'' +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }
}
