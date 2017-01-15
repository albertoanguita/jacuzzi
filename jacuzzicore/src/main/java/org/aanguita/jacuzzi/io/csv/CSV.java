package org.aanguita.jacuzzi.io.csv;

import org.aanguita.jacuzzi.string.StringOps;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * CSV api
 */
public class CSV {

    private final List<String> columns;

    private final Map<String, Integer> columnToIndex;

    private final List<List<String>> data;

    private final String separator;

    public CSV(List<String> columns, String separator) {
        this.columns = columns;
        columnToIndex = buildColumnToIndex(columns);
        data = new ArrayList<>();
        this.separator = separator;
    }

    public CSV(List<String> columns, List<List<String>> data, String separator) {
        this.columns = columns;
        columnToIndex = buildColumnToIndex(columns);
        this.data = data;
        this.separator = separator;
    }

    private static Map<String, Integer> buildColumnToIndex(List<String> columns) {
        Map<String, Integer> columnToIndex = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            columnToIndex.put(columns.get(i), i);
        }
        return columnToIndex;
    }

    public static CSV load(String path, String separator) throws IOException, ParseException {
        return load(path, separator, null);
    }

    public static CSV load(String path, String separator, String nullValue) throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        String line = reader.readLine();
        List<String> columns = StringOps.separateTokens(line, separator, true, nullValue, -1);
        List<List<String>> data = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            data.add(StringOps.separateTokens(line, separator, false, nullValue, -1));
        }
        reader.close();
        return new CSV(columns, data, separator);
    }

    public void save(String path, String nullValue) throws IOException {
        FileWriter fileWriter = new FileWriter(path);
        BufferedWriter out = new BufferedWriter(fileWriter);
        out.write(writeLine(columns, separator, nullValue));
        for (List<String> dataLine : data) {
            out.write(writeLine(dataLine, separator, nullValue));
        }
        out.close();
        fileWriter.close();
    }

    private static String writeLine(List<String> values, String separator, String nullValue) {
        if (!values.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder(values.get(0));
            for (int i = 1; i < values.size(); i++) {
                stringBuilder.append(separator);
                String token = (nullValue != null && values.get(i) == null) ? nullValue : values.get(i);
                stringBuilder.append(token);
            }
            stringBuilder.append('\n');
            return stringBuilder.toString();
        } else {
            return "";
        }
    }

    public int getRowCount() {
        return data.size();
    }

    public List<String> getColumns() {
        return columns;
    }

    public int getColumnIndex(String column) {
        return columnToIndex.get(column);
    }

    public Iterator<List<String>> getDataIterator() {
        return data.iterator();
    }

    public List<String> getRow(int index) {
        return data.get(index);
    }

    public void addRow(List<String> row) throws IndexOutOfBoundsException {
        addRow(data.size(), row);
    }

    public void addRow(int index, List<String> row) throws IndexOutOfBoundsException {
        if (columns.size() == row.size()) {
            data.add(index, row);
        } else {
            throw new IndexOutOfBoundsException("Wrong row size: " + row.size());
        }
    }

    public void removeRow(int index) {
        data.remove(index);
    }

    public String getSeparator() {
        return separator;
    }
}
