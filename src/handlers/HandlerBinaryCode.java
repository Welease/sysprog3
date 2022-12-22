package handlers;

import exceptions.OverflowException;
import exceptions.ProgramException;
import exceptions.UnknownCommandException;
import models.PseudoCommand;
import tables.OpcodeTableSingleton;
import tables.AuxiliaryTableSingleton;
import tables.SymbolicNamesTableSingleton;

import java.util.*;

public class HandlerBinaryCode {
    private int typeOfAddressation;
    private AuxiliaryTableSingleton auxiliaryTable;
    private SymbolicNamesTableSingleton symbolicNamesTable;
    private OpcodeTableSingleton opcodeTable;
    private ArrayList<String> listCommands;
    private ArrayList<String> listRegistr;
    private HashMap<Integer, String> mapOperands;
    private ArrayList<String> listOfSection;
    private String text;
    private String nameOfProgram;
    private String startAddress;
    private String endAddress;
    private String currentAddress;
    private String globalAddress;
    private String command;
    private String section;
    private String[] str;
    private boolean hasNext;
    private boolean isExtdef;
    private boolean isExtref;
    private boolean isProg;

    public HandlerBinaryCode(String text, int typeOfAddressation) {
        this.text = text.trim();
        this.typeOfAddressation = typeOfAddressation;
        auxiliaryTable = AuxiliaryTableSingleton.getInstance();
        symbolicNamesTable = SymbolicNamesTableSingleton.getInstance();
        opcodeTable = OpcodeTableSingleton.getInstance();
        mapOperands = new HashMap<>();
        listOfSection = new ArrayList<>();
        listRegistr = new ArrayList<>();
        listRegistr.add("");
        for (int i = 0; i < 16; i++)
            listRegistr.add("R" + i);
        listCommands = new ArrayList<>(Arrays.asList("start", "end", "word", "byte", "resb", "resw", "extdef",
                "extref", "csect"));
    }

    public void readText() {
        //Разбиение по строкам
        str = text.split("\n");
        //Первая строка
        command = str[0].trim();
        String[] arg = str[0].split(" ");

        nameOfProgram = arg[0].trim();
        section = nameOfProgram;
        AuxiliaryTableSingleton.setSection(section);

        if (!"start".equals(arg[1].trim().toLowerCase()))
            callException(0, "Отсутствует начало программы.");
        else
            listCommands.remove("start");

        if (arg.length == 3) {
            startAddress = arg[2];
            globalAddress = arg[2];
            int strt = 0;
            try {
                strt = Integer.parseInt(startAddress);
            } catch (Exception ex) {
                callException(0, "Некорректное значение адреса начала программы.");
            }
            if (strt != 0)
                callException(0, "Адрес начала программы должен = 0");
            if (startAddress.length() > 6) {
                startAddress = startAddress.substring(0, 6);
                globalAddress = startAddress;
            }
        } else if (arg.length == 2) {
            startAddress = "0";
            globalAddress = "0";
        } else {
            callException(0, "Неверное количество команд.");
        }
        checkAddress(startAddress, 0);

        PseudoCommand.setStartAddress(startAddress);
        hasNext = true;
        isExtdef = false;
        isExtref = false;
        isProg = false;
    }

    public int viewRows(int row) {
        AuxiliaryTableSingleton.clear();
        if (listOfSection.contains(section))
            throw new RuntimeException("Повторение имени секции: " + section);
        listOfSection.add(section);
        auxiliaryTable = AuxiliaryTableSingleton.getInstance();
        AuxiliaryTableSingleton.setSection(section);
        //просмотр каждой строки
        for (int i = row; i < str.length; i++) {
            str[i] = str[i].trim();
            if ("".equals(str[i]))
                continue;
            command = str[i];
            String metka = null;
            String operation = null;
            String[] operands = new String[0];
            String operand1;
            String operand2;
            //Разбиение на метку, МКОП, операнды
            try {
                metka = defineMetka();
                operation = defineCommand();
                operands = defineOperands();
            } catch (UnknownCommandException e) {
                callException(i, e.getMessage());
            }
            operand1 = operands[0];
            operand2 = operands[1];

            if (!checkValidSymbols(metka))
                throw new ProgramException(i, "Некорректное имя метки: " + metka);
            if (!checkValidSymbols(operation))
                throw new ProgramException(i, "Некорректное имя команды: " + operation);

            //обработка метки
            if (!"".equals(metka) && (!"csect".equals(operation.toLowerCase()))) {
                try {
                    symbolicNamesTable.addNewName(metka, section, currentAddress);
                } catch (UnknownCommandException e) {
                    callException(i, e.getMessage());
                }
            }

            //поиск в ТКО
            if (opcodeTable.checkCommand(operation)) {
                isProg = true;
                int addr = Integer.parseInt(currentAddress, 16) + opcodeTable.getLen(operation);
                checkAddress(Integer.toHexString(addr), i);
                int code;

                if ("".equals(operand2))
                    code = opcodeTable.getBinaryCode(operation) * 4 + getType(i, operand1);
                else
                    code = opcodeTable.getBinaryCode(operation) * 4 + getType(i, operand2);
                String newStr = Integer.toHexString(code).toUpperCase();
                if (newStr.length() % 2 != 0)
                    newStr = "0" + newStr;
                opcodeTable.getOpcodeTable().put(newStr, opcodeTable.getOpcodeTable().get(operation));

                if ("".equals(operand2))
                    auxiliaryTable.addCommand(globalAddress, String.valueOf(code), operand1);
                else
                    auxiliaryTable.addCommand(globalAddress, String.valueOf(code), operand1, operand2);
                globalAddress = currentAddress;

            }
            else if (listCommands.contains(operation.toLowerCase())) {
                if ("end".equals(operation.toLowerCase())) {
                    if (!metka.isEmpty() || !metka.isBlank()) {
                        callException(i, "Метка перед END");
                    }
                    listCommands.remove("end");
                    hasNext = false;
                }

                //Встречается extdef
                if ("extdef".equals(operation.toLowerCase())) {
                    if (!metka.isBlank() && !metka.isEmpty()) {
                        callException(i, "Ошибка, метка перед EXTDEF");
                    }
                    isExtdef = true;
                    if (isExtref || isProg)
                        callException(i, "Неверный порядок EXTDEF");
                    if ((!"".equals(operand2)) || ("".equals(operand1)))
                        callException(i, "EXTDEF должен содержать ровно 1 операнд.");
                    try {
                        symbolicNamesTable.addExternalName(operand1, section);
                    } catch (Exception ex) {
                        callException(i, ex.getMessage());
                    }
                    auxiliaryTable.addCommand("", operation, operand1);
                    continue;
                }

                //Встречается extref
                if ("extref".equals(operation.toLowerCase())) {
                    if (!metka.isBlank() && !metka.isEmpty()) {
                        callException(i, "Ошибка, метка перед EXTREF");
                    }
                    isExtref = true;
                    if (isProg)
                        callException(i, "Неверный порядок EXTREF");
                    if ((!"".equals(operand2)) || ("".equals(operand1)))
                        callException(i, "EXTREF должен содержать ровно 1 операнд.");
                    try {
                        symbolicNamesTable.addExternalLink(operand1, section);
                    } catch (Exception ex) {
                        callException(i, ex.getMessage());
                    }
                    auxiliaryTable.addCommand("", operation, operand1);
                    continue;
                }

                //Встречается csect
                if ("csect".equals(operation.toLowerCase())) {
                    if ((!"".equals(operand2)) || (!"".equals(operand1)))
                        callException(i, "CSECT не должен содержать операндов.");
                    auxiliaryTable.addCommand(globalAddress, operation, metka);
                    endAddress = currentAddress;
                    currentAddress = "0";
                    globalAddress = currentAddress;
                    section = metka;
                    isProg = false;
                    isExtref = false;
                    isExtdef = false;

                    return i + 1;
                }

                isProg = true;
                PseudoCommand pseudoCommand = null;
                PseudoCommand.setCurrentAddress(currentAddress);
                try {
                    pseudoCommand = new PseudoCommand(operation, operand1);
                } catch (UnknownCommandException | OverflowException e) {
                    callException(i, e.getMessage());
                }

                int addr = Integer.parseInt(currentAddress, 16) + pseudoCommand.getLen();
                checkAddress(Integer.toHexString(addr), i);
                auxiliaryTable.addCommand(globalAddress, operation, operand1);
                globalAddress = currentAddress;
            }
            else
                callException(i, "Неизвестная команда: " + operation);
        }
        endAddress = currentAddress;
        return -1;
    }

    private void checkAddress(String address, int i) {
        long code = 0;
        try {
            code = Long.parseLong(address, 16);
        } catch (Exception ex) {
            callException(i, "Некорректный адрес: " + address);
        }
        if (code > Integer.parseInt("FFFFFF", 16))
            callException(i, "Переполнение: " + Long.toHexString(code));
        if (code < 0)
            callException(i, "Некорректный адрес: " + address);
        currentAddress = address;
    }

    private int getType(int ind, String operand) {
        if (listRegistr.contains(operand)) {
            return 0;
        }
        try {
            if (operand.contains("h"))
                Integer.parseInt(operand, 16);
            else
                Integer.parseInt(operand);
            return 0;
        } catch (Exception ex) {
            mapOperands.put(ind, operand);
            if ((operand.contains("[")) && (operand.contains("]"))) {
                /*if (typeOfAddressation == 0)
                    throw new RuntimeException("Недопустимый тип адресации для: " + operand);*/
                return 2;
            }
            /*if (typeOfAddressation == 1)
                throw new RuntimeException("Недопустимый тип адресации для: " + operand);*/
            return 1;
        }
    }

    private String defineMetka() throws UnknownCommandException {
        String metka = "";
        //Разбиение на метку, МКОП, операнды
        if (command.contains(":")){
            String[] arr = command.split(":");
            metka = arr[0];
            if ("".equals(metka))
                throw new UnknownCommandException("Отстутствует название метки");
            command = command.replaceFirst(metka, "").trim();
            command = command.substring(1).trim();
        }
        if (command.contains(":"))
            throw new UnknownCommandException("Некорректное имя метки: " + metka + command.substring(0, command.indexOf(":") + 1));

        if (!"".equals(metka) && listRegistr.contains(metka))
            throw new UnknownCommandException("Некорректное имя метки: " + metka + ". Имя зазезервировано.");
        return metka;
    }

    private String defineCommand() throws UnknownCommandException {
        String operation = command.split(" ")[0];
        if (command.contains(",")) {
            if ((operation.length() > 3) &&
                    (operation.substring(0, 4).equals("LOAD") || operation.substring(0, 4).equals("SAVE")))
                throw new UnknownCommandException("Неверное количество операндов.");
        }
        command = command.replace(operation, "").trim();
        //command = command.substring(command.indexOf(' ') + 1).trim();
        return operation;
    }

    private String[] defineOperands() throws UnknownCommandException {
        String[] operands = new String[2];

        if (command.contains(",")) {
            operands[0] = command.substring(0, command.indexOf(','));
            command = command.substring(command.indexOf(',') + 1).trim();

            operands[1] = handleKovichki();

            if (!checkValidOperand1(operands[0]))
                throw new UnknownCommandException("Недопустимое значение 1 операнда: " + operands[0]);
        }
        else{
            operands[0] = handleKovichki();
            operands[1] = "";
        }

        return operands;
    }

    private boolean checkValidOperand1(String operand) {
        return listRegistr.contains(operand.toUpperCase());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    private void callException(int i, String message) {
        throw new ProgramException(i + 1, message);
    }

    public String getNameOfProgram() {
        return nameOfProgram;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    private boolean checkValidSymbols(String str) {
        str = str.toLowerCase();
        if ("".equals(str))
            return true;
        char t = str.charAt(0);
        if ((t < 'a') || (t > 'z')) {
            return false;
        }

        for (int i = 1; i < str.length(); i++) {
            t = str.charAt(i);
            if (((t >= 'a') && (t <= 'z')) || (t == '_') || (t == '@') || (t == '$') || ((t >= '0') && (t <= '9'))) {
                continue;
            }
            else
                return false;
        }
        return true;
    }

    private String handleKovichki() throws UnknownCommandException {
        int begin = command.indexOf('\'');
        int end = command.lastIndexOf('\'');

        StringBuilder stringBuilder = new StringBuilder();
        if ((begin != -1) && (end != begin)) {
            for (int i = 0; i < command.length(); i++) {
                char t = command.charAt(i);
                if ((i > begin) && (i < end))
                    stringBuilder.append(t);
                else if ((t == ' ') || (t == ','))
                    throw new UnknownCommandException("Ошибка при обработке операндов.");
                else
                    stringBuilder.append(t);
            }
            return stringBuilder.toString();
        }
        else if ((begin != -1) && (end == begin)) {
            throw new UnknownCommandException("Некорректное значение операнда: " + command);
        }
        else {
            if ((command.contains(" ")) || (command.contains(",")))
                throw new UnknownCommandException("Неверное количество операндов");
            return command;
        }
    }

    public boolean hasNextSection() {
        return hasNext;
    }
}
