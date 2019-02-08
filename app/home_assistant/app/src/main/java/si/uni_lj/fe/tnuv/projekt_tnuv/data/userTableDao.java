package si.uni_lj.fe.tnuv.projekt_tnuv.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface userTableDao {
    @Query("SELECT * FROM userTable")
    List<userTable> getAll();

    @Query("SELECT * FROM userTable WHERE user_name = :user_name")
    List<userTable> getByUserName(String user_name);

    @Query("SELECT * FROM userTable WHERE uid = :uid")
    List<userTable> getByUid(int uid);

    @Query("DELETE FROM userTable")
    public void nukeUserTable();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertUserData(userTable userTable);

    @Update
    public void updateUserData(userTable userTable);

    @Query("UPDATE userTable SET user_name = :user_name, userColor = :color WHERE uid = :uid")
    public void updateUser(String user_name, int color, int uid);

    @Delete
    public void deleteUserData(userTable userTable);
}
