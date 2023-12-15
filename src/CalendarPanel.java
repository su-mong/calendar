import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarPanel extends JPanel implements MouseListener {
    final JPanels win;

    private static DateTimeFormatter dateTimeFormatterL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS+09");
    private static DateTimeFormatter dateTimeFormatterS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss+09");

    private static YearMonth currentMonth = YearMonth.now();
    private static DefaultTableModel tableModel = new DefaultTableModel(
        new Object[12][7],
        new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" }
    );
    private static Map<Integer, List<EventModel>> eventModelMap = new HashMap<>();

    private static JTable calendarTable = new JTable(tableModel) {
        @Override // Make cells non-editable.
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private static JButton monthButton = new JButton();

    public CalendarPanel(JPanels win) {
        this.win = win;
        setLayout(null);

        JButton prevButton = new JButton("<");
        prevButton.setBounds(0, 0, 120, 35);
        prevButton.addActionListener(e -> changeMonth(-1, win.currentUserId));

        JButton nextButton = new JButton(">");
        nextButton.setBounds(880, 0, 120, 35);
        nextButton.addActionListener(e -> changeMonth(1, win.currentUserId));

        monthButton.setBounds(130, 0, 740, 35);

        add(prevButton);
        add(monthButton);
        add(nextButton);

        JButton reminderButton = new JButton("show reminders");
        reminderButton.setBounds(0, 35, 480, 35);
        reminderButton.addActionListener(e -> {
            win.eventReminderPanel = new EventReminderPanel(win);
            win.change("eventReminder");
        });
        add(reminderButton);

        JButton rsvpsButton = new JButton("show rsvps");
        rsvpsButton.setBounds(520, 35, 480, 35);
        rsvpsButton.addActionListener(e -> {
            win.rsvpPanel = new RsvpPanel(win);
            win.change("rsvp");
        });
        add(rsvpsButton);

        for (int i = 0; i < 11; i += 2) {
            calendarTable.setRowHeight(i, 20); // Date rows.
        }
        for (int i = 1; i < 12; i += 2) {
            calendarTable.setRowHeight(i, 58); // Event rows.
        }

        calendarTable.addMouseListener(this);

        JScrollPane calendarScrollPane = new JScrollPane(calendarTable);
        calendarScrollPane.setBounds(0, 70, 1000, 530);
        add(calendarScrollPane);
        fillCalendar(win.currentUserId);
        setVisible(true);
    }

    public void fillCalendar(int currentUserId) {
        for (int rowIndex = 0; rowIndex < 12; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 7; columnIndex++) {
                tableModel.setValueAt("", rowIndex, columnIndex);
            }
        }

        if(currentUserId != -1) {
            loading(
                    currentUserId,
                    Timestamp.valueOf(currentMonth.atDay(1).atStartOfDay()),
                    Timestamp.valueOf(currentMonth.atEndOfMonth().atTime(23, 59, 59))
            );
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        int rowIndex = startDayOfWeek / 7 * 2;
        int columnIndex = startDayOfWeek % 7;

        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            tableModel.setValueAt(dayOfMonth, rowIndex, columnIndex);
            if(!eventModelMap.isEmpty() && eventModelMap.containsKey(dayOfMonth)) {
                tableModel.setValueAt(eventModelMap.get(dayOfMonth).get(0).title, rowIndex + 1, columnIndex);
            }

            if (columnIndex == 6) {
                rowIndex += 2;
            }
            columnIndex = (columnIndex + 1) % 7;
        }

        monthButton.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
    }

    private void changeMonth(int delta, int currentUserId) {
        currentMonth = currentMonth.plusMonths(delta);
        fillCalendar(currentUserId);
    }

    // 다른 화면에서 넘어올 때 실행
    public void init(int currentUserId) {
        currentMonth = YearMonth.now();
        fillCalendar(currentUserId);
    }

    private void loading(int currentUserId, Timestamp start, Timestamp end) {
        String SQL_SELECT = "SELECT * FROM events LEFT JOIN event_infos ON events.event_info_id = event_infos.id " +
                "WHERE events.user_id = ? AND ? > event_infos.start_date_time AND event_infos.end_date_time > ?;";

        // establishes database connection
        // auto closes connection and preparedStatement
        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {
            eventModelMap.clear();

            preparedStatement.setInt(1, currentUserId);
            preparedStatement.setTimestamp(2, end);
            preparedStatement.setTimestamp(3, start);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                EventModel eventModel = new EventModel();
                eventModel.eventId = resultSet.getInt("id");
                eventModel.eventInfoId = resultSet.getInt("event_info_id");
                eventModel.title = resultSet.getString("event_title");

                if(resultSet.getString("start_date_time").length() == 22) {
                    eventModel.start = Timestamp.valueOf(LocalDateTime.from(dateTimeFormatterS.parse(resultSet.getString("start_date_time"))));
                } else {
                    eventModel.start = Timestamp.valueOf(LocalDateTime.from(dateTimeFormatterL.parse(resultSet.getString("start_date_time"))));
                }
                if(resultSet.getString("end_date_time").length() == 22) {
                    eventModel.end = Timestamp.valueOf(LocalDateTime.from(dateTimeFormatterS.parse(resultSet.getString("end_date_time"))));
                } else {
                    eventModel.end = Timestamp.valueOf(LocalDateTime.from(dateTimeFormatterL.parse(resultSet.getString("end_date_time"))));
                }

                int startDate = eventModel.start.getDate();
                int endDate = eventModel.end.getDate();

                for(int date = startDate; date <= endDate; date++) {
                    if(eventModelMap.containsKey(date)) {
                        eventModelMap.get(date).add(eventModel);
                    } else {
                        List<EventModel> list = new ArrayList<>();
                        list.add(eventModel);
                        eventModelMap.put(date, list);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Loading Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Loading Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = calendarTable.getSelectedRow();
        int col = calendarTable.getSelectedColumn();

        int date = (int)calendarTable.getModel().getValueAt((row % 2 == 1) ? row-1 : row, col);
        CalendarOneDayPopup calendarOneDayPopup = new CalendarOneDayPopup(
            this,
            eventModelMap.get(date),
            currentMonth,
            date
        );
        calendarOneDayPopup.setVisible(true);
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}

class CalendarOneDayPopup extends JFrame {
    private final CalendarPanel calendarPanel;
    private final JList<EventModel> eventJList;
    private final List<EventModel> eventList;

    public CalendarOneDayPopup(CalendarPanel calendarPanel, List<EventModel> eventList, YearMonth currentMonth, int date) {
        this.calendarPanel = calendarPanel;

        if(eventList != null) {
            this.eventList = eventList;
        } else {
            this.eventList = new ArrayList<>();
        }

        setTitle(currentMonth.getYear() + "/" + currentMonth.getMonth() + "/" + date);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose this frame when closed.

        eventJList = new JList<>(this.eventList.toArray(new EventModel[0]));
        eventJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroller = new JScrollPane(eventJList);

        JPanel panel = new JPanel(new BorderLayout());
        listScroller.setBounds(0, 25, 400, 275);
        panel.add(listScroller); // And here

        JButton createEventButton = new JButton("Create New Event");
        createEventButton.setBounds(0, 0, 400, 25);
        createEventButton.addActionListener(e -> {
            this.calendarPanel.win.eventBookingPanel = new EventBookingPanel(
                this.calendarPanel.win,
                currentMonth,
                date
            );
            this.calendarPanel.win.change("eventBooking");
            dispose();
        });
        panel.add(createEventButton);

        JButton eventDetailButton = new JButton("Show Detail...");
        eventDetailButton.setBounds(0, 300, 400, 25);
        eventDetailButton.addActionListener(e -> showEventDetail());
        panel.add(eventDetailButton);

        JButton modifyEventButton = new JButton("Modify This Event");
        modifyEventButton.setBounds(0, 325, 400, 25);
        modifyEventButton.addActionListener(e -> {});
        panel.add(modifyEventButton);

        JButton deleteEventButton = new JButton("Delete This Event");
        deleteEventButton.setBounds(0, 350, 400, 25);
        deleteEventButton.addActionListener(e -> deleteEvent());
        panel.add(deleteEventButton);

        add(panel);
    }

    private void showEventDetail() {
        List<EventModel> selectedMembers = eventJList.getSelectedValuesList();
        if(selectedMembers.isEmpty()) {
            return;
        }

        this.calendarPanel.win.eventDetailPanel = new EventDetailPanel(this.calendarPanel.win, selectedMembers.get(0).eventId);
        this.calendarPanel.win.change("eventDetail");
        dispose();
    }

    private void deleteEvent() {
        List<EventModel> selectedMembers = eventJList.getSelectedValuesList();
        if(selectedMembers.isEmpty()) {
            return;
        }

        String SQL_CHECK_HOST_IS_ME = "SELECT * FROM events LEFT JOIN event_infos ON events.event_info_id = event_infos.id " +
                "WHERE events.id = ? AND event_infos.host_id = ? AND events.user_id = ?;";
        String SQL_DELETE_NOT_MY_HOST = "DELETE FROM events WHERE id = ?;";
        String SQL_DELETE_MY_HOST = "DELETE FROM events WHERE event_info_id = ?; DELETE FROM event_infos WHERE id = ?;";

        // establishes database connection
        // auto closes connection and preparedStatement
        try (Connection conn = DriverManager.getConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_CHECK_HOST_IS_ME)) {
            preparedStatement.setInt(1, selectedMembers.get(0).eventId);
            preparedStatement.setInt(2, this.calendarPanel.win.currentUserId);
            preparedStatement.setInt(3, this.calendarPanel.win.currentUserId);

            int eventInfoId = -1;
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventInfoId = resultSet.getInt("event_info_id");
            }

            if(eventInfoId != -1) {
                try (PreparedStatement preparedStatement2 = conn.prepareStatement(SQL_DELETE_MY_HOST)) {
                    preparedStatement2.setInt(1, eventInfoId);
                    preparedStatement2.setInt(2, eventInfoId);
                    preparedStatement2.executeUpdate();
                    JOptionPane.showMessageDialog(
                        getParent(),
                        "Delete succeed.",
                        "Delete Result",
                        JOptionPane.PLAIN_MESSAGE
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        getParent(),
                        "Error : " + e.getMessage(),
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
            } else {
                try (PreparedStatement preparedStatement2 = conn.prepareStatement(SQL_DELETE_NOT_MY_HOST)) {
                    preparedStatement2.setInt(1, selectedMembers.get(0).eventId);
                    preparedStatement2.executeUpdate();
                    JOptionPane.showMessageDialog(
                        getParent(),
                        "Delete succeed.",
                        "Delete Result",
                        JOptionPane.PLAIN_MESSAGE
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        getParent(),
                        "Error : " + e.getMessage(),
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    System.out.print(e.getMessage());
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Delete Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.out.print(e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Delete Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }

        // eventBookingPage.setSelectedMembers(selectedMembers);
        this.calendarPanel.fillCalendar(this.calendarPanel.win.currentUserId);
        dispose();
    }
}