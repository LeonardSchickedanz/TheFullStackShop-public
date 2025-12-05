package yorku.thefullstackshop.daos.interfaces;

import java.util.List;
import java.util.Optional;

public interface BaseDAO<T> {
    T save(T entity);

    T update(T entity);

    void delete(int id);

    Optional<T> findById(int id);

    List<T> findAll();
}
