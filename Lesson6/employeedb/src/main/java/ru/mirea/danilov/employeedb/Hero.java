package ru.mirea.danilov.employeedb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hero")
public class Hero {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String universe;
    public String superpower;
    public int powerLevel;
}