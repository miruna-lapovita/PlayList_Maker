package Repository;

import java.util.ArrayList;
import java.util.List;

import Domain.Entity;

public class Repository<T extends Entity> {
    List<T> entities = new ArrayList<>();

    public T findById(int id) {
        for (T entity : entities) {
            if (entity.getId() == id){
                return entity;
            }
        }
        return null;
    }


    public void addEntity(T entityToAdd) throws Exception{
        if(findById(entityToAdd.getId()) != null){
            throw new Exception("Exista deja o entittate cu Id-ul dat");
        }
        entities.add(entityToAdd);
    }

    public T getEntityById(int id){
        for (T entity : entities){
            if(entity.getId() == id)
                return entity;
        }
        return null;
    }


    public ArrayList<T> getAllEntities(){
        return new ArrayList<>(entities);
    }


    @Override
    public String toString() {
        return "Repository{" +
                "entities=" + entities +
                '}';
    }

}
