package si.uni_lj.fe.tnuv.projekt_tnuv.MessageManagement;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import si.uni_lj.fe.tnuv.projekt_tnuv.data.messageTable;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.messageTableDao;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.projektTnuvDatabase;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.userTable;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.userTableDao;

public class messageStorageManager {

    private projektTnuvDatabase database;
    private static userTableDao userTableDao;
    private static messageTableDao messageTableDao;

    //constructor
    public messageStorageManager(Context context){
        connectToDatabase(context);
    }

    //connects to user and message table
    private void connectToDatabase(Context context) {

        //db connection
        database = projektTnuvDatabase.getDatabaseInstance(context);
        userTableDao = database.userTableDao();
        messageTableDao = database.messageTableDao();

        Log.d("JURE", "connected to user and message database");
    }

    //converts unix time to standard date and time format
    public String unixToStandardTime(long unixTime){

        String datetime;

        Date date = new java.util.Date(unixTime);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        //sdf.setTimeZone();
        datetime = sdf.format(date);
        return datetime;
    }

    /*
     * USER TABLE
     *
     *
     * */
    public void writeUserDatabase(userTable... data) {
        Log.d("JURE", "writeUserDatabase");

        new writeUserDbAsync().execute(data);
        Log.d("JURE", "writeUserDatabase -> done");
    }

    public void updateUserDatabase(userTable... data) {
        Log.d("JURE", "updateUserDatabase");

        new updateUserDbAsync().execute(data);
        Log.d("JURE", "updateUserDatabase -> done");
    }

    public userTable setUserRowValues(String userName, int color, int... uids) {
        userTable data = new userTable();

        data.setUserName(userName);
        data.setUserColor(color); //hex form: 0x00rrggbb

        return data;
    }

    //make sure the uid is unique
    public userTable setAllUserRowValues(String userName, int color, int uid) {
        userTable data = new userTable();

        data.setUserName(userName);
        data.setUserColor(color); //hex form: 0x00rrggbb
        data.setUid(uid);

        return data;
    }

    private static class writeUserDbAsync extends AsyncTask<userTable, Void, Void> {

        @Override
        protected Void doInBackground(userTable... data) {
            int parNum = data.length;

            for (int i = 0; i < parNum; i++) {
                userTableDao.insertUserData(data[i]);
            }

            return null;
        }
    }

    private static class updateUserDbAsync extends AsyncTask<userTable, Void, Void> {

        @Override
        protected Void doInBackground(userTable... data) {
            int parNum = data.length;

            for (int i = 0; i < parNum; i++) {
                userTableDao.updateUserData(data[i]);
                //userTableDao.updateUser(data[i].getUserName(), data[i].getUserColor(), data[i].getUid());
            }

            return null;
        }
    }

    public List<String> getAllUserNames(){
        Log.d("JURE", "getAllUserNames");
        List<userTable> readResult = readUserDatabase();
        List<String> userNames = new ArrayList<String>();

        if(readResult.size() == 0) return null;

        Log.d("JURE", "number of users: " + Integer.toString(readResult.size()));

        for(int i=0; i<readResult.size(); i++){
            userNames.add(readResult.get(i).getUserName());
        }

        return userNames;
    }

    public List<userTable> readUserDatabase() {
        Log.d("JURE", "readUserDatabase");

        List<userTable> readResult = null;

        try {
            readResult = new readUserDbAsync().execute().get(200, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.e("JURE", "Execution exception readUserDatabase");
        } catch (InterruptedException e) {
            Log.e("JURE", "Interrupted exception readUserDatabase");
        } catch (TimeoutException e) {
            Log.e("JURE", "Timeout exception readUserDatabase");
        }

        //check if there is data in read result
        if (readResult == null || readResult.isEmpty()) {
            Log.d("JURE", "readUserDatabase -> no data returned");
            return null;
        }

        //log the data read
        Log.d("JURE", "readUserDatabase -> number of entries: " + Integer.toString(readResult.size()));

        for (int i = 0; i < readResult.size(); i++) {
            Log.d("JURE", "user data read: ");
            Log.d("JURE", "uid: " + Integer.toString(readResult.get(i).getUid()));
            Log.d("JURE", "user name: " + readResult.get(i).getUserName());
            Log.d("JURE", "user color: " + Integer.toHexString(readResult.get(i).getUserColor()));
        }

        return readResult;
    }

    private static class readUserDbAsync extends AsyncTask<Void, Void, List<userTable>> {


        @Override
        protected List<userTable> doInBackground(Void... voids) {
            List<userTable> dataList;

            dataList = userTableDao.getAll();

            return dataList;
        }
    }

    public userTable readUserDbById(int uid){
        Log.d("JURE", "readUserDbById -> author uid: " + Integer.toString(uid));
        List<userTable> user = null;

        try{
            user = new readUserDbByIdAsync().execute(uid).get(200, TimeUnit.MILLISECONDS);
            return user.get(0);
        } catch (ExecutionException e) {
            Log.e("JURE", "Execution exception readUserDatabaseById");
        } catch (InterruptedException e) {
            Log.e("JURE", "Interrupted exception readUserDatabaseById");
        } catch (TimeoutException e) {
            Log.e("JURE", "Timeout exception readUserDatabaseById");
        }

        return null;
    }

    private static class readUserDbByIdAsync extends  AsyncTask<Integer, Void, List<userTable>> {

        @Override
        protected List<userTable> doInBackground(Integer... uids) {
            List<userTable> dataList;
            int uid = uids[0]; //use only the first uid given for now

            dataList = userTableDao.getByUid(uid);

            return dataList;
        }
    }

    public int getUserUid(String userName){
        Log.d("JURE", "getUserUid");
        int uid = 0;
        List<userTable> dataList;

        try{
            dataList = new readUserDbByNameAsync().execute(userName).get(200, TimeUnit.MILLISECONDS);

            if(dataList.size() > 1){
                Log.d("JURE", "getUserUid -> too many users match the user name given");
            }

            uid = dataList.get(0).getUid();

        } catch (ExecutionException e) {
            Log.e("JURE", "Execution exception readUserDbByNameAsync");
        } catch (InterruptedException e) {
            Log.e("JURE", "Interrupted exception readUserDbByNameAsync");
        } catch (TimeoutException e) {
            Log.e("JURE", "Timeout exception readUserDbByNameAsync");
        }

        return uid;
    }

    public static class readUserDbByNameAsync extends AsyncTask<String, Void, List<userTable>>{

        @Override
        protected List<userTable> doInBackground(String... names) {
            List<userTable> dataList;
            dataList = userTableDao.getByUserName(names[0]);

            return dataList;
        }
    }

    /*
     * MESSAGE TABLE
     *
     *
     * */
    public void writeMessageDatabase(messageTable... data) {
        Log.d("JURE", "writeMessageDatabase");

        new writeMessageDbAsync().execute(data);
        Log.d("JURE", "writeMessageDatabase -> done");
    }

    public messageTable setMessageRowValues(int authorUid, String message) {
        messageTable data = new messageTable();

        //set the values
        data.setTimestamp(System.currentTimeMillis());
        data.setAuthorUid(authorUid);
        data.setMessageBody(message);

        return data;
    }

    public messageTable setMessageRowValuesTs(int authorUid, long unixTimestamp, String message) {
        messageTable data = new messageTable();

        //set the values
        //data.setTimestamp(System.currentTimeMillis());
        data.setTimestamp(unixTimestamp*1000);
        data.setAuthorUid(authorUid);
        data.setMessageBody(message);

        return data;
    }

    private static class writeMessageDbAsync extends AsyncTask<messageTable, Void, Void> {

        @Override
        protected Void doInBackground(messageTable... data) {
            int parNum = data.length;

            for (int i = 0; i < parNum; i++) {
                messageTableDao.insertMessage(data[i]);
            }
            return null;
        }
    }

    public List<messageTable> readMessageDatabase() {
        Log.d("JURE", "readMessageDatabase");

        List<messageTable> readResult = null;

        try {
            readResult = new readMessageDbAsync().execute().get(100, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.e("JURE", "Execution exception readMessageDatabase");
        } catch (InterruptedException e) {
            Log.e("JURE", "Interrupted exception readMessageDatabase");
        } catch (TimeoutException e) {
            Log.e("JURE", "Timeout exception readMessageDatabase");
        }

        //check if there is data in read result
        if (readResult == null || readResult.isEmpty()) {
            Log.d("JURE", "readMessageDatabase -> no data returned");
            return null;
        }

        //log the data read
        Log.d("JURE", "readMessageDatabase -> number of entries: " + Integer.toString(readResult.size()));

        for (int i = 0; i < readResult.size(); i++) {
            Log.d("JURE", "message data read: ");
            Log.d("JURE", "timestamp: " + Long.toString(readResult.get(i).getTimestamp()));
            Log.d("JURE", "author uid: " + Integer.toString(readResult.get(i).getAuthorUid()));
            Log.d("JURE", "message: " + readResult.get(i).getMessageBody());
        }

        return readResult;
    }

    private static class readMessageDbAsync extends AsyncTask<Void, Void, List<messageTable>> {


        @Override
        protected List<messageTable> doInBackground(Void... voids) {
            List<messageTable> dataList;

            dataList = messageTableDao.getAll();

            return dataList;
        }
    }

    public void deleteFromMessageDb(long timestamp) {
        Log.d("JURE", "deleteFromMessageDb");

        //readMessageDatabase();

        //Log.d("JURE", "timestamp to delete: " + Long.toString(timestamp));

        new deleteFromMessageDbAsync().execute(timestamp);
    }

    private static class deleteFromMessageDbAsync extends AsyncTask<Long, Void, Void>{

        @Override
        protected Void doInBackground(Long... timestamps) {
            messageTableDao.deleteMessageItem(timestamps[0]);
            return null;
        }
    }

    public long getMostRecentMessageTimestamp() {
        Log.d("JURE", "getMostRecentMessageTimestamp");

        long readResult = -1;
        try {
            readResult = new getMostRecentMessageTimestampAsync().execute().get(100, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.e("JURE", "Execution exception getMostRecentMessageTimestamp");
        } catch (InterruptedException e) {
            Log.e("JURE", "Interrupted exception getMostRecentMessageTimestamp");
        } catch (TimeoutException e) {
            Log.e("JURE", "Timeout exception getMostRecentMessageTimestamp");
        }

        //the result is in millis since epoch, convert to seconds
        readResult = readResult/1000 + 1;

        return readResult;
    }

    private static class getMostRecentMessageTimestampAsync extends AsyncTask<Void, Void, Long>{

        @Override
        protected Long doInBackground(Void... voids) {
            return messageTableDao.getMostRecentTimestamp();
        }
    }
}