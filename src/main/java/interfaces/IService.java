package interfaces;

import model.Personne;

import java.util.List;

public interface IService<T> {

    void add(T p);
    List<T> getAll();
    void update(T p);
    void delete(T p);
    // getBy getByid etc..
}