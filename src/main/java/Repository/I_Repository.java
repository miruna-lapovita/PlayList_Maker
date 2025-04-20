package Repository;

import java.util.ArrayList;
import Domain.Entity;

public interface I_Repository<T extends Entity> {

    void addEntity(T entityToAdd) throws Exception;

    T findById(int id);

    T getEntityById(int id);

    //void removeEntity(int id) throws Exception;

    ArrayList<T> getAllEntities();

    //void updateEntity(int id, T entityToUpdate) throws Exception;
}

