package si.uni_lj.fe.tnuv.projekt_tnuv.list_item;

import java.util.List;

//vsak razred ki implementira ta interface mora imeti to metodo
public interface dataSourceInterface {
    List<listItem> getListOfData();
}
