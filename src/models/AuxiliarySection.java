package models;

public class AuxiliarySection implements Comparable<AuxiliarySection>{
    private String section;
    private String address;

    public AuxiliarySection(String section, String address) {
        this.section = section;
        this.address = address;
    }

    public String getSection() {
        return section;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        String str = section + address;
        return str.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        AuxiliarySection sName = (AuxiliarySection) obj;
        return section.equals(sName.getSection()) && address.equals(sName.getAddress());
    }

    @Override
    public int compareTo(AuxiliarySection o) {
        String val = section + address;
        return val.compareTo(o.getSection() + o.getAddress());
    }
}
