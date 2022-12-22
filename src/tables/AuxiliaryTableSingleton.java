package tables;

import models.AuxiliarySection;
import models.ConvertedCommand;

import java.util.HashMap;

public class AuxiliaryTableSingleton {
    private static AuxiliaryTableSingleton instance;
    private HashMap<AuxiliarySection, ConvertedCommand> auxiliaryTable;
    private static int count = 0;
    private static String section;

    private AuxiliaryTableSingleton() {
        auxiliaryTable = new HashMap<>();
    }

    public static AuxiliaryTableSingleton getInstance(){
        if(instance == null){
            instance = new AuxiliaryTableSingleton();
        }
        return instance;
    }

    public void addCommand(String address, String command, String value) {
        if (!"".equals(address))
            address = processAddress(address);
        else
            address = createEmptyString();
        auxiliaryTable.put(new AuxiliarySection(section, address.toUpperCase()), new ConvertedCommand(command, value));
    }

    public void addCommand(String address, String command, String value1, String value2) {
        if (!"".equals(address))
            address = processAddress(address);
        else
            address = createEmptyString();
        auxiliaryTable.put(new AuxiliarySection(section, address.toUpperCase()), new ConvertedCommand(command, value1, value2));
    }

    public HashMap<AuxiliarySection , ConvertedCommand> getAuxiliaryTable() {
        return auxiliaryTable;
    }

    public static void clear() {
        instance = null;
    }

    private String processAddress(String address) {
        if (address.length() != 6){
            address = "0".repeat(Math.max(0, 6 - address.length())) + address;
        }
        return address;
    }

    private String createEmptyString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++){
            stringBuilder.append(" ");
        }
        count++;
        return stringBuilder.toString();
    }

    public static String getSection() {
        return section;
    }

    public static void setSection(String section1) {
        section = section1;
    }
}
