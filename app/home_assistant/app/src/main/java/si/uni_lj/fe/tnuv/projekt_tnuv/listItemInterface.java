package si.uni_lj.fe.tnuv.projekt_tnuv;

import java.util.List;

import si.uni_lj.fe.tnuv.projekt_tnuv.list_item.listItem;

//omogoca view-controller komunikacijo
public interface listItemInterface {

    void startMessageDetailActivity(long dateAndTime, String message, String userName, int userColor);

    void setupAdatapterAndView(List<listItem> listOfData);
}
