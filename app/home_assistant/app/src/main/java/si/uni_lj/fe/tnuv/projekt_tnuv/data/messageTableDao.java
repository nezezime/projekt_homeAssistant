package si.uni_lj.fe.tnuv.projekt_tnuv.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface messageTableDao {
    @Query("SELECT * FROM messageTable")
    List<messageTable> getAll();

    @Query("SELECT * FROM messageTable WHERE message_author_uid = :message_author_uid")
    List<messageTable> getByAuthorUid(long message_author_uid);

    @Query("DELETE FROM messageTable")
    void nukeMessageTable();

    @Query("DELETE FROM messageTable WHERE timestamp = :timestamp")
    void deleteMessageItem(long timestamp);

    @Query("SELECT MAX(timestamp) FROM messageTable")
    long getMostRecentTimestamp();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertMessage(messageTable messageTable);

    @Update
    public void updateMessage(messageTable messageTable);

    @Delete
    public void deleteMessage(messageTable messageTable);
}
