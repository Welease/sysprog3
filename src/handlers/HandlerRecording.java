package handlers;

import exceptions.ProgramException;
import exceptions.UnknownCommandException;
import models.*;
import tables.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class HandlerRecording {
    private String startAddress;
    private String endAddress;
    private String nameOfProgram;
    private ModificationTableSingleton modificationTable;
    private ArrayList<AuxiliarySection> arrayList;
    private HashMap<AuxiliarySection, ConvertedCommand> hashMap;
    RecordingTableSingleton recordingTable;
    ArrayList<String> listRegistr;

    public HandlerRecording(String startAddress, String endAddress, HashMap<AuxiliarySection, ConvertedCommand> hashMap) {
        modificationTable = ModificationTableSingleton.getInstance();
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.hashMap = hashMap;
        recordingTable = RecordingTableSingleton.getInstance();
        listRegistr = new ArrayList<>();
        for (int i = 0; i < 16; i++)
            listRegistr.add("R" + i);
    }

    public void generateObjectModule() throws UnknownCommandException {
        arrayList = new ArrayList<>();
        for (AuxiliarySection elem : hashMap.keySet()) {
            arrayList.add(elem);
            nameOfProgram = elem.getSection();
        }
        //Запись заголовок
        generateHeader();

        Collections.sort(arrayList);
        for (AuxiliarySection elem : arrayList) {
            ConvertedCommand command = hashMap.get(elem);
            if ("END".equals(command.getCommand())) {

                if ("".equals(command.getValue1().trim())) {
                    recordingTable.addRecord("E", "0".repeat(Math.max(0, 6 - startAddress.length())) + startAddress, "");
                }
                else {
                    try {
                        int val = Integer.parseInt(command.getValue1(), 16);
                        if (val > Integer.parseInt(endAddress, 16)) {
                            throw new ProgramException("Неправильный адрес точки входа.");
                        }
                        if (val < Integer.parseInt(startAddress, 16)) {
                            throw new ProgramException("Неправильный адрес точки входа.");
                        }
                        String end = Integer.toHexString(val);
                        recordingTable.addRecord("E", "0".repeat(Math.max(0, 6 - end.length())) + end, "");
                    } catch (Exception ex) {
                        throw new ProgramException("Неправильный адрес точки входа.");
                    }
                }

                return;
            }

            String value = getOperandType(elem, command.getCommand(), command.getValue1(), command.getValue2());
            if (!"-".equals(value))
                recordingTable.addRecord("T", elem.getAddress(), value);
        }
    }

    private void generateHeader() {
        int addr = Integer.parseInt(endAddress, 16) - Integer.parseInt(startAddress, 16);
        String start = "0".repeat(Math.max(0, 6 - startAddress.length())) + startAddress;
        String end = "0".repeat(Math.max(0, 6 - Integer.toHexString(addr).length())) + Integer.toHexString(addr);
        recordingTable.addRecord("H", nameOfProgram, start, end);
    }

    private String getOperandType(AuxiliarySection addrAndSection, String command, String operand1, String operand2) throws UnknownCommandException {
        if (("RESB".equals(command)) || ("RESW".equals(command))) {
            return "";
        }

        if (("WORD".equals(command)) || ("BYTE".equals(command))) {
            String str;
            if (operand1.contains("x"))
                str = operand1.substring(2, operand1.length() - 1);
            else if (operand1.contains("c")) {
                str = operand1.substring(2, operand1.length() - 1);
                StringBuilder answer = new StringBuilder();
                for (int i = 0; i < str.length(); i++) {
                    int code = str.charAt(i);
                    answer.append(Integer.toHexString(code));
                }
                str = answer.toString();
            }
            else {
                int code;
                if (operand1.contains("h"))
                    code = Integer.parseInt(operand1.substring(0, operand1.length() - 1), 16);
                else
                    code = Integer.parseInt(operand1);

                str = Integer.toHexString(code);
            }
            if ("WORD".equals(command)) {
                if (str.length() % 3 != 0)
                    str = "0".repeat(Math.max(0, 3 - str.length() % 3)) + str;
            }
            else {
                if (str.length() % 2 != 0)
                    str = "0" + str;
            }
            return str;
        }

        if ("END".equals(command)) {
            return "0".repeat(Math.max(0, 6 - command.length())) + command;
        }

        SymbolicNamesTableSingleton table = SymbolicNamesTableSingleton.getInstance();
        OpcodeTableSingleton opcodeTable = OpcodeTableSingleton.getInstance();
        HashMap<SymbolicName, NameCharacteristic> symbolicNames = table.getSymbolicNames();

        if ("EXTDEF".equals(command)) {
            recordingTable.addRecord("D", " " + operand1, symbolicNames.get(new SymbolicName(operand1, addrAndSection.getSection())).getAddress(), "");
            return "-";
        }

        if ("EXTREF".equals(command)) {

            recordingTable.addRecord("R", " " + operand1, symbolicNames.get(new SymbolicName(operand1, addrAndSection.getSection())).getAddress(), "");
            return "-";
        }

        if ("CSECT".equals(command)) {
            recordingTable.addRecord("E", "000000", "");
            return "-";
        }

        String output = "";
        output += command;
        //отсутствует второй операнд
        if ("".equals(operand2)) {
            if (opcodeTable.getLen(command) != 1) {
                if ("".equals(operand1))
                    throw new UnknownCommandException("Операнд не может быть пустым для команды: " + command);
                if (opcodeTable.getLen(command) == 2) {
                    int number = -1;
                    try {
                        number = Integer.parseInt(operand1);
                    } catch (Exception ex) {
                        throw new RuntimeException("Некорректное значение операнда для команды: " + command);
                    }
                    if (number > 255)
                        throw new RuntimeException("Переполнение памяти, некорректное значение операнда для команды:" + command);
                }
                output += handleOperand(command, addrAndSection, symbolicNames, operand1);
            } else {
                if (!"".equals(operand1)) {
                    throw new UnknownCommandException("Операнд не может иметь значения для команды: " + command);
                }
            }
        }
        //операнд 2 имеет какое-то значение
        else {
            //проверка на регистр операнда 1
            if (listRegistr.contains(operand1)) {
                String str = operand1.substring(operand1.indexOf('R') + 1);

                output += str;
            }
            else
                throw new UnknownCommandException("Некорректное значение 1 операнда: " + operand1);

            //
            output += handleOperand(command, addrAndSection, symbolicNames, operand2);
        }
        return output;
    }

    private String handleOperand(String command, AuxiliarySection addrAndSection, HashMap<SymbolicName, NameCharacteristic> symbolicNames, String operand1) throws UnknownCommandException {
        if (symbolicNames.containsKey(new SymbolicName(operand1, addrAndSection.getSection()))) {
            if ("ВС".equals(symbolicNames.get(new SymbolicName(operand1, addrAndSection.getSection())).getType())) {
                NameCharacteristic name = symbolicNames.get(new SymbolicName(operand1, addrAndSection.getSection()));
                symbolicNames.put(new SymbolicName(operand1, addrAndSection.getSection()),
                        new NameCharacteristic(addrAndSection.getAddress(), name.getType()));
                modificationTable.addModification(addrAndSection.getAddress() + " " + operand1, addrAndSection.getSection(), true);
                return addrAndSection.getAddress();
            } else {
                modificationTable.addModification(addrAndSection.getAddress() + " " + operand1, addrAndSection.getSection(), false);
            }
            NameCharacteristic name = symbolicNames.get(new SymbolicName(operand1, addrAndSection.getSection()));
            return name.getAddress();
        }
        else if (listRegistr.contains(operand1)) {
            OpcodeTableSingleton opcodeTable = OpcodeTableSingleton.getInstance();
            Operation operation = opcodeTable.getOpcodeTable().get(command);
            if (operation.getLen() == 4)
                throw new UnknownCommandException("Для данной команды операнд должен быть меткой: " + command);

            String str = operand1.substring(operand1.indexOf('R') + 1);
            int num = Integer.parseInt(str);
            return Integer.toHexString(num);
        }
        else {
            if ("".equals(operand1)) {
                return "";
            }
            if ((operand1.contains("[")) && (operand1.contains("]"))){
                operand1 = operand1.trim();
                if (operand1.charAt(0) != '[')
                    callOperandException(operand1);
                String operand = operand1.substring(1);
                String check = operand.substring(operand.lastIndexOf(']'));
                if ("".equals(check))
                    callOperandException(operand1);
                operand = operand.substring(0, operand.lastIndexOf(']'));
                if (symbolicNames.containsKey(new SymbolicName(operand, addrAndSection.getSection()))) {
                    NameCharacteristic address = symbolicNames.get(new SymbolicName(operand, addrAndSection.getSection()));
                    String nextAddress = arrayList.get(arrayList.indexOf(addrAndSection) + 1).getAddress();
                    int val = -1;
                    try {
                        val = Integer.parseInt(address.getAddress(), 16) - Integer.parseInt(nextAddress, 16);
                    }catch (Exception ex) {
                        throw new UnknownCommandException("Ошибка при вычислении операндной части: " + operand);
                    }
                    String numHex = Integer.toHexString(val);
                    if (numHex.length() < 6) {
                        numHex = "0".repeat(Math.max(0, 6 - numHex.length())) + numHex;
                    } else if (numHex.length() > 6) {
                        numHex = numHex.substring(2);
                    }
                    return numHex;
                } else {
                    callOperandException(operand1);
                }
            }else {
                OpcodeTableSingleton opcodeTable = OpcodeTableSingleton.getInstance();
                Operation operation = opcodeTable.getOpcodeTable().get(command);
                if (operation.getLen() == 4)
                    throw new UnknownCommandException("Для данной команды операнд должен быть меткой: " + command);
                int num = Integer.parseInt(operand1);
                return Integer.toHexString(num);
            }
            return "";
        }
    }

    private void callOperandException(String operand1) throws UnknownCommandException {
        throw new UnknownCommandException("Неизвестный операнд: " + operand1);
    }
}

