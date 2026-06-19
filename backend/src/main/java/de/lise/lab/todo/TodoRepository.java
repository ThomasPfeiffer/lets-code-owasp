package de.lise.lab.todo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t FROM Todo t WHERE :owner IS NULL OR t.owner = :owner")
    List<Todo> findByOwner(@Param("owner") String owner);
}
