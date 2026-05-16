package ru.mirea.danilov.employeedb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HeroDao {

    @Query("SELECT * FROM hero")
    List<Hero> getAll();

    @Query("SELECT * FROM hero WHERE id = :id")
    Hero getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Hero hero);

    @Update
    void update(Hero hero);

    @Delete
    void delete(Hero hero);

    @Query("DELETE FROM hero")
    void deleteAll();
}