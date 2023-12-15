import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class CalendarV2 {
    private static YearMonth currentMonth = YearMonth.now();
    private static DefaultTableModel tableModel = new DefaultTableModel(
            new Object[12][7],
            new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" }
    );
    private static JButton monthButton = new JButton();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Calendar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        JPanel panel = new JPanel(new BorderLayout());

        JButton prevButton = new JButton("<");
        prevButton.addActionListener(e -> changeMonth(-1));

        JButton nextButton = new JButton(">");
        nextButton.addActionListener(e -> changeMonth(1));

        JPanel buttonPanel = new JPanel(new BorderLayout());

        buttonPanel.add(prevButton, BorderLayout.WEST);
        buttonPanel.add(monthButton, BorderLayout.CENTER);
        buttonPanel.add(nextButton, BorderLayout.EAST);

        panel.add(buttonPanel, BorderLayout.NORTH);

        JTable calendarTable = new JTable(tableModel) {
            @Override // Make cells non-editable.
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (int i = 0; i < 11; i += 2) {
            calendarTable.setRowHeight(i, 20); // Date rows.
        }
        for (int i = 1; i < 12; i += 2) {
            calendarTable.setRowHeight(i, 82); // Event rows.
        }

        panel.add(new JScrollPane(calendarTable), BorderLayout.CENTER);

        frame.add(panel);

        frame.setVisible(true);

        fillCalendar();
    }

    private static void fillCalendar() {
        for (int rowIndex = 0; rowIndex < 12; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 7; columnIndex++) {
                tableModel.setValueAt("", rowIndex, columnIndex);
            }
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        int rowIndex = startDayOfWeek / 7 * 2;
        int columnIndex = startDayOfWeek % 7;

        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            tableModel.setValueAt(dayOfMonth, rowIndex, columnIndex);

            if (columnIndex == 6) {
                rowIndex += 2;
            }
            columnIndex = (columnIndex + 1) % 7;
        }

        monthButton.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
        // JButton monthYearButton = new JButton(
        // currentMonth.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.US)
        // + " "
        // + currentMonth.getYear());

        // System.out.println(currentMonth.getMonth()+"<<<<<");

    }

    private static void changeMonth(int delta) {
        currentMonth = currentMonth.plusMonths(delta);
        fillCalendar();
    }
}