package si.uni_lj.fe.tnuv.projekt_tnuv.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


//tukaj je doloceno kako bo tabela v bazi izgledala
@Entity
public class weatherTable {
    //DATABASE USING ROOM LIBRARY
    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "weather_forecast")
    private boolean isWeatherForecast;

    @ColumnInfo(name = "weather_id")
    private int weatherId;

    @ColumnInfo(name = "weather_temperature")
    private double weatherTemperature;

    @ColumnInfo(name = "weather_description")
    private String weatherDescription;

    //getters and setters are required for room to work
    public int getUid() {
        return uid;
    }

    public void setUid(int uid){
        this.uid = uid;
    }

    public boolean getIsWeatherForecast(){
        return isWeatherForecast;
    }

    public void setIsWeatherForecast(boolean isWeatherForecast) {
        this.isWeatherForecast = isWeatherForecast;
    }

    public int getWeatherId(){
        return weatherId;
    }

    public void setWeatherId(int weatherId){
        this.weatherId = weatherId;
    }

    public double getWeatherTemperature(){
        return weatherTemperature;
    }

    public void setWeatherTemperature(double weatherTemperature){
        this.weatherTemperature = weatherTemperature;
    }

    public String getWeatherDescription(){
        return weatherDescription;
    }
    public void setWeatherDescription(String weatherDescription){
        this.weatherDescription = weatherDescription;
    }

}
