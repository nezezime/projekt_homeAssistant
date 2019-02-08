package si.uni_lj.fe.tnuv.projekt_tnuv.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {weatherTable.class, userTable.class, messageTable.class}, version = 3, exportSchema = false)
public abstract class projektTnuvDatabase extends RoomDatabase{

    //bind DAO to the database class
    public abstract weatherTableDao weatherTableDao();
    public abstract userTableDao userTableDao();
    public abstract messageTableDao messageTableDao();

    private static projektTnuvDatabase DB_INSTANCE;

    public static  projektTnuvDatabase getDatabaseInstance(Context context){

        //.allowMainThreadQueries() je dovoljen samo za testiranje
        if(DB_INSTANCE == null){
            DB_INSTANCE = Room.databaseBuilder(context,
                    projektTnuvDatabase.class, "tnuv_sample.db").fallbackToDestructiveMigration().build();
        }

        return DB_INSTANCE;
    }

    public static void destroyDatabaseInstance(){
        DB_INSTANCE = null;
    }
}
