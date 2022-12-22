package GUI;

import exceptions.UnknownCommandException;
import handlers.HandlerBinaryCode;
import handlers.HandlerRecording;
import models.*;
import tables.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class MainWindow extends JFrame {
    private String nameOfProgram;
    private String startAddress;
    private String endAddress;
    private int typeOfAddressation;
    private ArrayList<HashMap<AuxiliarySection, ConvertedCommand>> auxiliaryList;
    private ArrayList<SectionData> sectionDataArrayList;

    private final JPanel rootPanel;
    private final JPanel infoPanel;
    private final JPanel firstPassPanel;
    private final JPanel secondPassPanel;
    private final JPanel buttonsPanel;

    private JLabel choosingExampleLabel;
    private JButton firstPassButton;
    private JButton secondPassButton;
    private JButton choosingExample;
    private JComboBox<String> choosingTypeComboBox;

    //Левая панель
    private JLabel sourceTextLabel;
    private JTextArea sourceTextArea;
    private JScrollPane scrollPaneSourceText;
    private JLabel codesOfOperationLabel;
    private JTable codesOfOperationTable;
    private DefaultTableModel tableModelCodesOfOperation;
    private JScrollPane scrollPaneCodesOfOperation;
    private JButton btnAddNewRow;
    private JButton btnDelRow;

    //Центральная панель
    private JLabel auxiliaryLabel;
    private JTable auxiliaryTable;
    private DefaultTableModel tableModelAuxiliary;
    private JScrollPane scrollPaneAuxiliary;
    private JLabel symbolicNamesLabel;
    private JTable symbolicNamesTable;
    DefaultTableModel tableModelSymbolicNames;
    private JScrollPane scrollPaneSymbolicNames;
    private JLabel modificationLabel;
    private JTable modificationsTable;
    DefaultTableModel tableModelModifications;
    private JScrollPane scrollPaneModifications;
    private JLabel errorsFirstPassLabel;
    private JTextArea errorsFirstPassText;
    private JScrollPane scrollPaneErrorFirst;

    //Правая панель
    private JLabel objectTitleLabel;
    private JTable objectTitleTable;
    private JLabel binaryCodeLabel;
    private JTextArea binaryCodeText;
    private JScrollPane scrollPaneBinaryCode;
    private JLabel errorsSecondPassLabel;
    private JTextArea errorsSecondPassText;
    private JScrollPane scrollPaneErrorSecond;

    public MainWindow() {
        super("Двухпросмотровый ассемблер");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        //Настройка панелей
        rootPanel = new JPanel();
        infoPanel = new JPanel();
        firstPassPanel = new JPanel();
        secondPassPanel = new JPanel();
        buttonsPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(320, 530));
        firstPassPanel.setPreferredSize(new Dimension(320, 530));
        secondPassPanel.setPreferredSize(new Dimension(320, 530));
        buttonsPanel.setPreferredSize(new Dimension(970, 100));
        infoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        firstPassPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        secondPassPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        rootPanel.add(infoPanel);
        rootPanel.add(firstPassPanel);
        rootPanel.add(secondPassPanel);
        rootPanel.add(buttonsPanel);
        //

        //Настройка кнопок
        firstPassButton = new JButton();
        secondPassButton = new JButton();
        firstPassButton.setText("Первый проход");
        secondPassButton.setText("Второй проход");
        choosingExampleLabel = new JLabel("Выбор примера. Тип адресации: ");
        //choosingExample = new JButton();
        choosingTypeComboBox = new JComboBox<>();
        firstPassButton.setEnabled(true);
        secondPassButton.setEnabled(false);
        buttonsPanel.add(firstPassButton);
        buttonsPanel.add(secondPassButton);
        buttonsPanel.add(choosingExampleLabel);
        buttonsPanel.add(choosingTypeComboBox);
        //choosingExample.setText("Загрузить пример");
        choosingTypeComboBox.addItem("Прямая");
        choosingTypeComboBox.addItem("Относительная");
        choosingTypeComboBox.addItem("Смешанная");
        //

        //Настройка левой панели
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        sourceTextLabel = new JLabel("Исходный текст");
        sourceTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sourceTextArea = new JTextArea();
        sourceTextArea.setEnabled(true);
        scrollPaneSourceText = new JScrollPane(sourceTextArea);
        scrollPaneSourceText.setMinimumSize(new Dimension(300, 300));
        scrollPaneSourceText.setPreferredSize(new Dimension(300, 330));

        codesOfOperationLabel = new JLabel("Таблица кодов операций");
        codesOfOperationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableModelCodesOfOperation = new DefaultTableModel(new String[] {"МКОП", "Дв. код", "Длина"}, 0);
        codesOfOperationTable = new JTable();
        codesOfOperationTable.setModel(tableModelCodesOfOperation);
        codesOfOperationTable.setEnabled(true);
        scrollPaneCodesOfOperation = new JScrollPane(codesOfOperationTable);
        btnAddNewRow = new JButton("Добавить строку");
        btnAddNewRow.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        btnAddNewRow.setPreferredSize(new Dimension(200, 10));
        btnDelRow = new JButton("Удалить строку");
        btnDelRow.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        btnDelRow.setPreferredSize(new Dimension(200, 10));

        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(sourceTextLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(scrollPaneSourceText);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(codesOfOperationLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(scrollPaneCodesOfOperation);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(btnAddNewRow);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(btnDelRow);
        //

        //Настройка центральной панели
        firstPassPanel.setLayout(new BoxLayout(firstPassPanel, BoxLayout.Y_AXIS));
        auxiliaryLabel = new JLabel("Вспомогательная таблица");
        auxiliaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableModelAuxiliary = new DefaultTableModel(new String[] {"СА", "Код команды", "Операнд", "Операнд"}, 0);
        auxiliaryTable = new JTable();
        auxiliaryTable.setModel(tableModelAuxiliary);
        auxiliaryTable.setEnabled(false);
        scrollPaneAuxiliary = new JScrollPane(auxiliaryTable);
        scrollPaneAuxiliary.setMinimumSize(new Dimension(250, 180));

        symbolicNamesLabel = new JLabel("Таблица символических имен");
        symbolicNamesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableModelSymbolicNames = new DefaultTableModel(new String[]{"СИ", "Адрес", "Секция", "Тип"}, 0);
        symbolicNamesTable = new JTable();
        symbolicNamesTable.setModel(tableModelSymbolicNames);
        symbolicNamesTable.setEnabled(false);
        scrollPaneSymbolicNames = new JScrollPane(symbolicNamesTable);

        modificationLabel = new JLabel("Таблица модификаций");
        modificationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableModelModifications = new DefaultTableModel(new String[]{"Адрес", "Секция"}, 0);
        modificationsTable = new JTable();
        modificationsTable.setModel(tableModelModifications);
        modificationsTable.setEnabled(false);
        scrollPaneModifications = new JScrollPane(modificationsTable);

        errorsFirstPassLabel = new JLabel("Ошибки первого прохода");
        errorsFirstPassLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorsFirstPassText = new JTextArea();
        errorsFirstPassText.setMinimumSize(new Dimension(250, 100));
        //errorsFirstPassText.setColumns(5);
        errorsFirstPassText.setEnabled(false);
        errorsFirstPassText.setDisabledTextColor(Color.BLACK);
        scrollPaneErrorFirst = new JScrollPane(errorsFirstPassText);
        scrollPaneErrorFirst.setMinimumSize(new Dimension(250, 100));

        firstPassPanel.add(Box.createVerticalStrut(20));
        firstPassPanel.add(auxiliaryLabel);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(scrollPaneAuxiliary);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(symbolicNamesLabel);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(scrollPaneSymbolicNames);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(modificationLabel);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(scrollPaneModifications);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(errorsFirstPassLabel);
        firstPassPanel.add(Box.createVerticalStrut(10));
        firstPassPanel.add(scrollPaneErrorFirst);
        //

        //Настройка правой панели
        secondPassPanel.setLayout(new BoxLayout(secondPassPanel, BoxLayout.Y_AXIS));
        objectTitleLabel = new JLabel("Заголовок объекта");
        objectTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        DefaultTableModel tableModelObjectTitle = new DefaultTableModel(1, 3);
        objectTitleTable = new JTable();
        objectTitleTable.setModel(tableModelObjectTitle);
        objectTitleTable.setRowHeight(20);
        objectTitleTable.setEnabled(false);

        binaryCodeLabel = new JLabel("Двоичный код");
        binaryCodeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        binaryCodeText = new JTextArea();
        binaryCodeText.setDisabledTextColor(Color.BLACK);
        binaryCodeText.setEnabled(false);
        scrollPaneBinaryCode = new JScrollPane(binaryCodeText);

        errorsSecondPassLabel = new JLabel("Ошибки второго прохода");
        errorsSecondPassLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorsSecondPassText = new JTextArea();
        errorsSecondPassText.setDisabledTextColor(Color.BLACK);
        //errorsSecondPassText.setColumns(5);
        errorsSecondPassText.setEnabled(false);
        scrollPaneErrorSecond = new JScrollPane(errorsSecondPassText);

        secondPassPanel.add(Box.createVerticalStrut(20));
        secondPassPanel.add(objectTitleLabel);
        secondPassPanel.add(Box.createVerticalStrut(10));
        secondPassPanel.add(objectTitleTable);
        secondPassPanel.add(Box.createVerticalStrut(10));
        secondPassPanel.add(binaryCodeLabel);
        secondPassPanel.add(Box.createVerticalStrut(10));
        secondPassPanel.add(scrollPaneBinaryCode);
        secondPassPanel.add(Box.createVerticalStrut(10));
        secondPassPanel.add(errorsSecondPassLabel);
        secondPassPanel.add(Box.createVerticalStrut(10));
        secondPassPanel.add(scrollPaneErrorSecond);
        //

        typeOfAddressation = 0;
        addListeners();

        pack();
        setContentPane(rootPanel);
        // Вывод окна на экран
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setVisible(true);
        tableModelCodesOfOperation.setRowCount(0);
        insertText();
        btnDelRow.setEnabled(true);
    }

    private void addListeners() {
        firstPassButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearTables();

                try {
                    updateTKO();
                }catch (UnknownCommandException exep) {
                    updateButtonsAndText();
                    errorsFirstPassText.setText(exep.getMessage());
                    return;
                }
                catch (Exception ex) {
                    updateButtonsAndText();
                    errorsFirstPassText.setText("Ошибка при обработке таблицы ТКО.");
                    return;
                }

                auxiliaryList = new ArrayList<>();
                sectionDataArrayList = new ArrayList<>();
                HandlerBinaryCode handler = new HandlerBinaryCode(sourceTextArea.getText(), typeOfAddressation);
                try {
                    handler.readText();
                    int row = 1;
                    while (row != -1) {
                        row = handler.viewRows(row);
                        auxiliaryList.add(AuxiliaryTableSingleton.getInstance().getAuxiliaryTable());
                        sectionDataArrayList.add(new SectionData(handler.getNameOfProgram(), handler.getStartAddress(), handler.getEndAddress()));
                    }
                    if (handler.hasNextSection()) {
                        updateButtonsAndText();
                        errorsFirstPassText.setText("Отсутствует конец программы.");
                        return;
                    }
                } catch (Exception ex) {
                    updateButtonsAndText();
                    errorsFirstPassText.setText(ex.getMessage());
                    return;
                }
                nameOfProgram = handler.getNameOfProgram();
                startAddress = handler.getStartAddress();
                endAddress = handler.getEndAddress();

                updateAuxiliary();
                try {
                    updateTSI();
                } catch (Exception ex) {
                    updateButtonsAndText();
                    tableModelAuxiliary.setRowCount(0);
                    tableModelSymbolicNames.setRowCount(0);
                    errorsFirstPassText.setText(ex.getMessage());
                    return;
                }
                RecordingTableSingleton.clear();
                secondPassButton.setEnabled(true);
                btnAddNewRow.setEnabled(false);
                btnDelRow.setEnabled(false);
                binaryCodeText.setText("");
                errorsSecondPassText.setText("");
            }
        });

        secondPassButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < auxiliaryList.size(); i++) {
                    HashMap<AuxiliarySection, ConvertedCommand> hashMap = auxiliaryList.get(i);
                    SectionData sectionData = sectionDataArrayList.get(i);
                    HandlerRecording handlerRecording = new HandlerRecording(sectionData.getStartAddress(),
                            sectionData.getEndAddress(), hashMap);
                    try {
                        handlerRecording.generateObjectModule();
                    } catch (UnknownCommandException ex) {
                        errorsSecondPassText.setText(ex.getMessage());
                        btnAddNewRow.setEnabled(true);
                        btnDelRow.setEnabled(true);
                        return;
                    }
                    updateModification();
                    updateObjectModule();
                }

                secondPassButton.setEnabled(false);
                btnAddNewRow.setEnabled(true);
                btnDelRow.setEnabled(true);
            }
        });

        btnAddNewRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnDelRow.setEnabled(true);
                tableModelCodesOfOperation.addRow(new Object[] {"", "", ""});
            }
        });

        btnDelRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModelCodesOfOperation.removeRow(tableModelCodesOfOperation.getRowCount() - 1);
                if (tableModelCodesOfOperation.getRowCount() == 0)
                    btnDelRow.setEnabled(false);
            }
        });

        choosingTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Прямая".equals(choosingTypeComboBox.getSelectedItem().toString())) {
                    typeOfAddressation = 0;
                    tableModelCodesOfOperation.setRowCount(0);
                    insertText();
                    btnDelRow.setEnabled(true);
                } else if ("Относительная".equals(choosingTypeComboBox.getSelectedItem().toString())) {
                    typeOfAddressation = 1;
                    tableModelCodesOfOperation.setRowCount(0);
                    insertTextOtnosit();
                    btnDelRow.setEnabled(true);
                } else if ("Смешанная".equals(choosingTypeComboBox.getSelectedItem().toString())) {
                    typeOfAddressation = 2;
                    tableModelCodesOfOperation.setRowCount(0);
                    insertTextSmesh();
                    btnDelRow.setEnabled(true);
                }
            }
        });
    }

    private void updateTKO() throws UnknownCommandException {
        OpcodeTableSingleton opcodeTable = OpcodeTableSingleton.getInstance();
        for (int i = 0; i < codesOfOperationTable.getRowCount(); i++) {
            String name = codesOfOperationTable.getValueAt(i, 0).toString();
            if (!checkValidSymbols(name)) {
                throw new UnknownCommandException("Некорректное имя команды в таблице ТКО");
            }
            String binaryCode = codesOfOperationTable.getValueAt(i, 1).toString();
            if ("".equals(name) || "".equals(binaryCode))
                return;
            int len = Integer.parseInt(codesOfOperationTable.getValueAt(i, 2).toString());
            if (len <= 0)
                throw new RuntimeException();
            opcodeTable.addCommand(name, binaryCode, len);
        }
    }

    private void updateAuxiliary() {
        for (int i = 0; i < auxiliaryList.size(); i++) {
            ArrayList<AuxiliarySection> arrayList = new ArrayList<>();
            HashMap<AuxiliarySection, ConvertedCommand> hashMap = auxiliaryList.get(i);
            for (AuxiliarySection elem : hashMap.keySet()) {
                arrayList.add(elem);
            }
            Collections.sort(arrayList);
            for (AuxiliarySection elem : arrayList) {
                ConvertedCommand command = hashMap.get(elem);
                tableModelAuxiliary.addRow(new Object[]{elem.getAddress().trim(), command.getCommand(), command.getValue1(), command.getValue2()});
            }
        }
    }

    private void updateTSI() {
        ArrayList<SymbolicName> arrayList = new ArrayList<>();
        SymbolicNamesTableSingleton symbolicNames = SymbolicNamesTableSingleton.getInstance();
        HashMap<SymbolicName, NameCharacteristic> hashMap = symbolicNames.getSymbolicNames();
        for (SymbolicName elem : hashMap.keySet()) {
            arrayList.add(elem);
        }

        Collections.sort(arrayList);
        for (SymbolicName elem : arrayList) {
            if (("".equals(hashMap.get(elem).getAddress()) && ("ВИ".equals(hashMap.get(elem).getType()))))
                throw new RuntimeException("Неопределенное внешнее имя: " + elem.getName() + " в секции " + elem.getSection());
            tableModelSymbolicNames.addRow(new Object[]{elem.getName(), hashMap.get(elem).getAddress(),
                    elem.getSection(), hashMap.get(elem).getType()});
        }
    }

    private void updateModification() {
        ModificationTableSingleton modificationTable = ModificationTableSingleton.getInstance();
        ArrayList<Modification> arrayList = modificationTable.getModificationList();
        Collections.sort(arrayList);
        for (Modification elem : arrayList) {
            tableModelModifications.addRow(new Object[] {elem.getAddress(), elem.getSection()});
        }
    }

    private void updateObjectModule() {
        ArrayList<String> arrayList = new ArrayList<>();
        RecordingTableSingleton recordingTable = RecordingTableSingleton.getInstance();
        HashMap<String , RecordBody> hashMap = recordingTable.getRecordingTable();
        for (String elem : hashMap.keySet()) {
            arrayList.add(elem);
        }
        Collections.sort(arrayList);

        ModificationTableSingleton modificationTable = ModificationTableSingleton.getInstance();
        ArrayList<Modification> modificationList = modificationTable.getModificationList();
        Collections.sort(modificationList);

        StringBuilder stringBuilder = new StringBuilder();
        String end = "";
        int indexD = 0;
        int indexR = 0;
        for (String elem : arrayList) {
            if (elem.charAt(0) == 'E') {
                end = elem;
                continue;
            }
            if (elem.charAt(0) == 'H') {
                StringBuilder stb = new StringBuilder();
                stb.append(elem);
                stb.append(" ");
                stb.append(hashMap.get(elem).getLen());
                stb.append(" ");
                stb.append(hashMap.get(elem).getBody());
                stb.append("\n");
                stringBuilder.insert(0, stb.toString());
                continue;
            }

            stringBuilder.append(elem);
            stringBuilder.append(" ");
            stringBuilder.append(hashMap.get(elem).getLen());
            stringBuilder.append(" ");
            stringBuilder.append(hashMap.get(elem).getBody());
            stringBuilder.append("\n");
        }
        for (Modification elem : modificationList) {
            stringBuilder.append("M");
            if (!elem.getExternalRef())
                stringBuilder.append(elem.getAddress().substring(0, elem.getAddress().indexOf(" ")));
            else
                stringBuilder.append(elem.getAddress());
            stringBuilder.append("\n");
        }
        stringBuilder.append(end);
        stringBuilder.append("\n");
        binaryCodeText.append(stringBuilder.toString());
        ModificationTableSingleton.clear();
        RecordingTableSingleton.clear();
    }

    private void clearTables() {
        OpcodeTableSingleton.clear();
        SymbolicNamesTableSingleton.clear();
        AuxiliaryTableSingleton.clear();
        RecordingTableSingleton.clear();
        ModificationTableSingleton.clear();
        tableModelSymbolicNames.setRowCount(0);
        tableModelAuxiliary.setRowCount(0);
        tableModelModifications.setRowCount(0);
        errorsFirstPassText.setText("");
        errorsSecondPassText.setText("");
    }

    private void insertText() {
        String prog = new StringBuilder()
                .append("Prog1 START 0\n")
                .append("      EXTDEF D23\n")
                .append("      EXTDEF D4\n")
                .append("      EXTREF D2\n")
                .append("      EXTREF D546\n")
                .append("D4: RESB 10\n")
                .append("D23: RESW 20\n")
                .append("B1: WORD 4096\n")
                .append("      JMP D2\n")
                .append("      SAVER1 D546\n")
                .append("      RESB 10\n")
                .append("A1: CSECT\n")
                .append("      EXTDEF D42\n")
                .append("      EXTREF D4\n")
                .append("D42: SAVER1 D4\n")
                .append("      INT 200\n")
                .append("      END 0\n")
                .toString();
        sourceTextArea.setText(prog);

        tableModelCodesOfOperation.addRow(new Object[] {"JMP", "01", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"LOADR1", "02", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"LOADR2", "03", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"ADD", "04", "2"});
        tableModelCodesOfOperation.addRow(new Object[] {"SAVER1", "05", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"INT", "06", "2"});
    }

    private void insertTextOtnosit() {
        String prog = new StringBuilder()
                .append("Prog1 START 0\n")
                .append("      EXTDEF D23\n")
                .append("      EXTDEF D4\n")
                .append("      EXTREF D2\n")
                .append("      EXTREF D546\n")
                .append("D4: RESB 10\n")
                .append("D23: RESW 20\n")
                .append("B1: WORD 4096\n")
                .append("      JMP D4\n")
                .append("      SAVER1 [B1]\n")
                .append("      RESB 10\n")
                .append("A1: CSECT\n")
                .append("      EXTDEF D42\n")
                .append("      EXTREF D4\n")
                .append("D42: SAVER1 D4\n")
                .append("      INT 200\n")
                .append("      END 0\n")
                .toString();
        sourceTextArea.setText(prog);

        tableModelCodesOfOperation.addRow(new Object[] {"JMP", "01", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"LOADR1", "02", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"LOADR2", "03", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"ADD", "04", "2"});
        tableModelCodesOfOperation.addRow(new Object[] {"SAVER1", "05", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"INT", "06", "2"});
    }

    private void insertTextSmesh() {
        String prog = new StringBuilder()
                .append("Prog1 START 0\n")
                .append("      EXTDEF D23\n")
                .append("      EXTDEF D4\n")
                .append("      EXTREF D2\n")
                .append("      EXTREF D546\n")
                .append("D4: RESB 10\n")
                .append("D23: RESW 20\n")
                .append("B1: WORD 4096\n")
                .append("      JMP [D4]\n")
                .append("      SAVER1 D546\n")
                .append("      RESB 10\n")
                .append("A1: CSECT\n")
                .append("      EXTDEF D42\n")
                .append("      EXTREF D4\n")
                .append("D42: SAVER1 D4\n")
                .append("      INT 200\n")
                .append("      END 0\n")
                .toString();
        sourceTextArea.setText(prog);

        tableModelCodesOfOperation.addRow(new Object[] {"JMP", "01", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"LOADR1", "02", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"LOADR2", "03", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"ADD", "04", "2"});
        tableModelCodesOfOperation.addRow(new Object[] {"SAVER1", "05", "4"});
        tableModelCodesOfOperation.addRow(new Object[] {"INT", "06", "2"});
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

    private void updateButtonsAndText() {
        secondPassButton.setEnabled(false);
        btnAddNewRow.setEnabled(true);
        btnDelRow.setEnabled(true);
        errorsSecondPassText.setText("");
        binaryCodeText.setText("");
    }

    public static void main(String[] args) {
        new MainWindow();
    }
}
