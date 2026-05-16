package ru.mirea.danilov.employeedb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Hero.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract HeroDao heroDao();
}