package tables;

import exceptions.UnknownCommandException;
import models.NameCharacteristic;
import models.SymbolicName;

import java.util.HashMap;

public class SymbolicNamesTableSingleton {
    private static SymbolicNamesTableSingleton instance;
    private HashMap<SymbolicName, NameCharacteristic> symbolicNames;

    private SymbolicNamesTableSingleton() {
        symbolicNames = new HashMap<>();
    }

    public static SymbolicNamesTableSingleton getInstance(){
        if(instance == null){
            instance = new SymbolicNamesTableSingleton();
        }
        return instance;
    }

    public void addNewName(String name, String section, String address) throws UnknownCommandException {
        if (address.length() != 6){
            address = "0".repeat(Math.max(0, 6 - address.length())) + address;
        }
        address = address.toUpperCase();

        SymbolicName sName = new SymbolicName(name, section);
        if (symbolicNames.containsKey(sName)) {
            NameCharacteristic characteristic = symbolicNames.get(sName);
            if ("ВС".equals(characteristic.getType()))
                throw new UnknownCommandException("Совпадение имени метки с внешней ссылкой: " + name);
            if (!"".equals(characteristic.getAddress()))
                throw new UnknownCommandException("Адрес внешнего имени уже назначен: " + name);
            if ("".equals(characteristic.getType()))
                throw new UnknownCommandException("Повторение метки: " + name);
            characteristic.setAddress(address);
        }
        else
            symbolicNames.put(new SymbolicName(name, section), new NameCharacteristic(address, ""));
    }

    public void addExternalName(String name, String section) throws UnknownCommandException {
        if (symbolicNames.containsKey(new SymbolicName(name, section)))
            throw new UnknownCommandException("Повторение внешнего имени: " + name + " в секции " + section);
        symbolicNames.put(new SymbolicName(name, section), new NameCharacteristic("", "ВИ"));
    }

    public void addExternalLink(String name, String section) throws UnknownCommandException {
        if (symbolicNames.containsKey(new SymbolicName(name, section)))
            throw new UnknownCommandException("Повторение внешней ссылки: " + name + " в секции " + section);
        symbolicNames.put(new SymbolicName(name, section), new NameCharacteristic("", "ВС"));
    }

    public HashMap<SymbolicName, NameCharacteristic> getSymbolicNames() {
        return symbolicNames;
    }

    public static void clear() {
        instance = null;
    }
}
