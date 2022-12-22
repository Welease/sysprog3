package models;

public class SymbolicName implements Comparable<SymbolicName>{
    private String name;
    private String section;
    private int numberOfAdditing;
    public static int index = 0;

    public SymbolicName(String name, String section) {
        this.name = name;
        this.section = section;
        this.numberOfAdditing = index;
        index++;
    }

    public String getName() {
        return name;
    }

    public String getSection() {
        return section;
    }

    @Override
    public int hashCode() {
        String str = name + section;
        return str.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        SymbolicName sName = (SymbolicName) obj;
        return name.equals(sName.getName()) && section.equals(sName.getSection());
    }

    public int getNumberOfAdditing() {
        return numberOfAdditing;
    }

    @Override
    public int compareTo(SymbolicName o) {
        int x = o.getNumberOfAdditing();
        int y = this.getNumberOfAdditing();
        return Integer.compare(y, x);
    }
}
