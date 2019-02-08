package si.uni_lj.fe.tnuv.projekt_tnuv.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity (foreignKeys = @ForeignKey(entity = userTable.class,
        parentColumns = "uid",
        childColumns = "message_author_uid",
        onDelete = CASCADE))
public class messageTable {
    @PrimaryKey
    private long timestamp;

    @ColumnInfo (name = "message_author_uid")
    private int authorUid;

    @ColumnInfo (name = "message_body")
    private String messageBody;

    //getters and setters
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(int authorUid) {
        this.authorUid = authorUid;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }
}
