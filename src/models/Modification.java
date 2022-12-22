package models;

public class Modification implements Comparable<Modification>{
    private String address;
    private String section;
    private Boolean isExternalRef;
    private int numberOfAdditing;
    private static int index = 0;

    public Modification(String address, String section, Boolean isExternalRef) {
        this.address = address;
        this.section = section;
        this.isExternalRef = isExternalRef;
        this.numberOfAdditing = index;
        index++;
    }

    public String getAddress() {
        return address;
    }

    public String getSection() {
        return section;
    }

    public int getNumberOfAdditing() {
        return numberOfAdditing;
    }

    @Override
    public int compareTo(Modification o) {
        return address.compareTo(o.getAddress());
    }

    public Boolean getExternalRef() {
        return isExternalRef;
    }
}
