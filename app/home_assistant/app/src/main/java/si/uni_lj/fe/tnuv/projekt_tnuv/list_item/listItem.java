package si.uni_lj.fe.tnuv.projekt_tnuv.list_item;

//za vsako sporocilo potrebujemo: datum, barvo(glede na userja) in vsebino
public class listItem {

    private long dateAndTime;
    private String message;
    private String userName;
    private int userColor;

    //constructor
    public listItem(long dateAndTime, String message, String userName, int userColor) {
        this.dateAndTime = dateAndTime;
        this.message = message;
        this.userName =userName;
        this.userColor = userColor;
    }

    public long getDateAndTime() {
        return dateAndTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDateAndTime(long dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUserColor() {
        return userColor;
    }

    public void setUserColor(int userColor) {
        this.userColor = userColor;
    }
}
