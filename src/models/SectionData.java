package models;

public class SectionData {
    private String nameOfProgram;
    private String startAddress;
    private String endAddress;

    public SectionData(String nameOfProgram, String startAddress, String endAddress) {
        this.nameOfProgram = nameOfProgram;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    public String getNameOfProgram() {
        return nameOfProgram;
    }

    public void setNameOfProgram(String nameOfProgram) {
        this.nameOfProgram = nameOfProgram;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }
}
