package si.uni_lj.fe.tnuv.projekt_tnuv.list_item_logic;

import si.uni_lj.fe.tnuv.projekt_tnuv.listItemInterface;
import si.uni_lj.fe.tnuv.projekt_tnuv.list_item.dataSourceInterface;
import si.uni_lj.fe.tnuv.projekt_tnuv.list_item.listItem;

public class listItemController {

    private listItemInterface view;
    private dataSourceInterface dataSource;

    public listItemController(listItemInterface view, dataSourceInterface dataSource) {
        this.view = view;
        this.dataSource = dataSource;

        getListFromDataSource();
    }

    //pridobi podatke iz shrambe in jih posreduje naprej
    public void getListFromDataSource(){
        view.setupAdatapterAndView( dataSource.getListOfData());
    }

    //prikaze detajle sporocila ob kliku
    public void onListItemClick(listItem testItem){
        view.startMessageDetailActivity(
                testItem.getDateAndTime(),
                testItem.getMessage(),
                testItem.getUserName(),
                testItem.getUserColor()
        );
    }
}
