package si.uni_lj.fe.tnuv.projekt_tnuv.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Color;

@Entity
public class userTable {
    @PrimaryKey (autoGenerate = true)
    private int uid;

    @ColumnInfo (name = "user_name")
    private String userName;

    @ColumnInfo (name = "userColor")
    private int userColor; //hex form: 0x00rrggbb;

    //getters and setters
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserColor() {
        return userColor;
    }

    public void setUserColor(int userColor) {
        this.userColor = userColor;
    }
}
