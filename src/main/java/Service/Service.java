package Service;

import Domain.Song;
import Repository.Repository;

import java.util.ArrayList;

public class Service {
    private final Repository<Song> songRepo;


    public Service(Repository<Song> songRepo) {
        this.songRepo = songRepo;

    }

    public void addSong(Song song) throws Exception{
        songRepo.addEntity(song);
    }




    public ArrayList<Song> getAllSongs(){
        return songRepo.getAllEntities();
    }



    public Song findSongById(int id){
        return songRepo.findById(id);
    }





}


