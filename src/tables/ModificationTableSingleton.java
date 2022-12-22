package tables;

import models.Modification;

import java.util.ArrayList;

public class ModificationTableSingleton {
    private static ModificationTableSingleton instance;
    private ArrayList<Modification> modificationList;

    private ModificationTableSingleton() {
        modificationList = new ArrayList<>();
    }

    public static ModificationTableSingleton getInstance() {
        if(instance == null){
            instance = new ModificationTableSingleton();
        }
        return instance;
    }

    public void addModification(String address, String section, Boolean isExtRef) {
        modificationList.add(new Modification(address, section, isExtRef));
    }

    public ArrayList<Modification> getModificationList() {
        return modificationList;
    }

    public static void clear() {
        instance = null;
    }
}
