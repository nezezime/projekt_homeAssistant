package si.uni_lj.fe.tnuv.projekt_tnuv.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

//database access object
@Dao
public interface weatherTableDao {
    @Query("SELECT * FROM weatherTable")
    List<weatherTable> getAll();

    @Query("SELECT * FROM weatherTable WHERE weather_forecast = :weather_forecast")
    List<weatherTable> getByForecast(int weather_forecast);

    @Query("DELETE FROM weatherTable")
    public void nukeWeatherTable();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertWeatherData(weatherTable weatherTable);

    @Update
    public void updateWeatherData(weatherTable weatherTable);

    @Delete
    public void deleteWeatherData(weatherTable weatherTable);


}
