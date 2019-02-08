package si.uni_lj.fe.tnuv.projekt_tnuv.list_item;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import si.uni_lj.fe.tnuv.projekt_tnuv.MessageManagement.messageStorageManager;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.messageTable;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.userTable;

//nadomesti s podatkovno bazo
public class MessageDataSource implements dataSourceInterface{

    private static final int sizeOfCollection = 12;

    private final String[] datesAndTimes = {
            "6:30AM 06/01/2017",
            "9:26PM 04/22/2013",
            "2:01PM 12/02/2015",
            "2:43AM 09/7/2018",
    };

    private final String[] messages = {
            "Check out content like Fragmented Podcast to expose yourself to the knowledge, ideas, " +
                    "and opinions of experts in your field",
            "Look at Open Source Projects like Android Architecture Blueprints to see how experts" +
                    " design and build Apps",
            "Write lots of Code and Example Apps. Writing good Quality Code in an efficient manner "
                    + "is a Skill to be practiced like any other.",
            "If at first something doesn't make any sense, find another explanation. We all " +
                    "learn/teach different from each other. Find an explanation that speaks to you."
    };

    private final int[] userColors = {
            1,
            2,
            3,
            4
    };

    @Override
    public List<listItem> getListOfData() {
        ArrayList<listItem> listOfData = new ArrayList<>();

        /*DUMMY EXAMPLE
        //at this point we create a random list item from the data we have available
        Random random = new Random();

        for(int i = 0; i<12; i++){
            int rndOne = random.nextInt(4);
            int rndTwo = random.nextInt(4);
            int rndThree = random.nextInt(4);

            listItem listItem = new listItem(
                    datesAndTimes[rndOne],
                    messages[rndTwo],
                    userColors[rndThree]
            );

            listOfData.add(listItem);
        }
        */
        Log.d("JURE", "GETLISTOFDATA");

        // here a message can be inserted into the database
        //messageTable messageTmp = storageManager.setMessageRowValues(1, "test msg");
        //storageManager.writeMessageDatabase(messageTmp);
        //storageManager.writeMessageDatabase(storageManager.setMessageRowValues(1, "Welcome to home assistant"));
        Log.d("JURE", "TUKAJ");
        List<messageTable> data = storageManager.readMessageDatabase();
        int dataLength = data.size();

        //polni od zadnjega proti prvemu
        for(int i=dataLength-1; i>=0; i--){

            //get user color and user name from the users table
            userTable user = storageManager.readUserDbById(data.get(i).getAuthorUid());
            int userColor = user.getUserColor();
            String userName = user.getUserName();

            Log.d("JURE", "user color retrieved: " + Integer.toString(userColor));

            listItem listItem = new listItem(
                    data.get(i).getTimestamp(),
                    data.get(i).getMessageBody(),
                    userName,
                    userColor);

            //append to list
            listOfData.add(listItem);
        }

        return listOfData;
    }

    private messageStorageManager storageManager;

    public MessageDataSource(Context context) {

        storageManager = new messageStorageManager(context);

        //test read the database
        //storageManager.writeUserDatabase(storageManager.setUserRowValues("Jure", 0xFF0000FF));
        //storageManager.writeUserDatabase(storageManager.setUserRowValues("Petra", 0xFF00FF00));
        //storageManager.writeUserDatabase(storageManager.setUserRowValues("Živa", 0xFFFF0000));
        //storageManager.writeUserDatabase(storageManager.setUserRowValues("Dejan", 0xFFF000F0));
        storageManager.readUserDatabase();
        //storageManager.readMessageDatabase();
    }
}
