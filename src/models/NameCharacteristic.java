package models;

public class NameCharacteristic {
    private String address;
    private String type;

    public NameCharacteristic(String address, String type) {
        this.address = address;
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
