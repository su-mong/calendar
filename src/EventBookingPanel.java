import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EventBookingPanel extends JPanel {
    private static Calendar timeZome = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));

    private final JLabel selectedMembersLabel = new JLabel();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+09");

    private int selectedReminderInterval = 0;

    public EventBookingPanel(JPanels win, YearMonth currentMonth, int date) {
        setLayout(null);

        JPanel panel = new JPanel(new GridLayout(0, 2)); // Set grid layout with any number of rows and 2 columns
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Event name
        JLabel eventNameLabel = new JLabel("Event Name:");
        JTextField eventNameField = new JTextField();

        // Description
        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField();

        // Add components to the panel.
        panel.add(eventNameLabel);
        panel.add(eventNameField);
        panel.add(descriptionLabel);
        panel.add(descriptionField);

        JPanel memberSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectMembersButton = new JButton("Select Members");
        selectedMembersLabel.setText("No members selected.");
        memberSelectionPanel.add(selectMembersButton);
        memberSelectionPanel.add(selectedMembersLabel);

        panel.add(new JLabel("Join Request Member:"));
        panel.add(memberSelectionPanel);

        // Start and end time
        JSpinner startTimeSpinner = new JSpinner(new SpinnerDateModel());
        // startTimeSpinner.setValue("23. 12. 15 오후 4:37");
        JSpinner endTimeSpinner = new JSpinner(new SpinnerDateModel());
        // startTimeSpinner.setValue("23. 12. 15 오후 4:37");

        JLabel startTimeLabel = new JLabel("Start Time:");
        JLabel endTimeLabel = new JLabel("End Time:");

        // Add components to the panel.
        panel.add(startTimeLabel);
        panel.add(startTimeSpinner);
        panel.add(endTimeLabel);
        panel.add(endTimeSpinner);

        JPanel selections = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox allDayCheckBox = new JCheckBox("All Day");
        JCheckBox reminderCheckBox = new JCheckBox("Remind please");

        JPanel reminderSelections = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reminderSelections.setLayout(new GridLayout(1,5));
        JLabel reminderIntervalLabel = new JLabel("interval:");
        JRadioButton rb1 = new JRadioButton("15 min");
        JRadioButton rb2 = new JRadioButton("30 min");
        JRadioButton rb3 = new JRadioButton("45 min");
        JRadioButton rb4 = new JRadioButton("60 min");
        ButtonGroup group = new ButtonGroup();
        group.add(rb1); group.add(rb2); group.add(rb3); group.add(rb4);
        reminderSelections.add(reminderIntervalLabel); reminderSelections.add(rb1); reminderSelections.add(rb2);
        reminderSelections.add(rb3); reminderSelections.add(rb4);
        rb1.setEnabled(false); rb2.setEnabled(false); rb3.setEnabled(false); rb4.setEnabled(false);

        JPanel reminderTimeFramePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reminderTimeFramePanel.setLayout(new GridLayout(0,2));
        JLabel reminderTimeFrameLabel = new JLabel("time frame:");
        JTextField reminderTimeFrameField = new JTextField();
        reminderTimeFramePanel.add(reminderTimeFrameLabel);
        reminderTimeFramePanel.add(reminderTimeFrameField);
        reminderTimeFrameField.setText("60");
        reminderTimeFrameLabel.setEnabled(false);

        selections.add(allDayCheckBox);
        selections.add(reminderCheckBox);
        selections.add(reminderSelections);
        selections.add(reminderTimeFramePanel);
        allDayCheckBox.setSelected(false); // Default to false.
        JLabel allDayLabel = new JLabel("All Day & Reminder");
        panel.add(allDayLabel);
        panel.add(selections);

        reminderCheckBox.addActionListener(e -> {
            rb1.setEnabled(reminderCheckBox.isSelected());
            rb2.setEnabled(reminderCheckBox.isSelected());
            rb3.setEnabled(reminderCheckBox.isSelected());
            rb4.setEnabled(reminderCheckBox.isSelected());
            reminderTimeFrameLabel.setEnabled(reminderCheckBox.isSelected());

            if(!reminderCheckBox.isSelected()) {
                selectedReminderInterval = 0;
            }
        });
        rb1.addActionListener(e -> {
            if(rb1.isSelected()) {
                selectedReminderInterval = 15;
            }
        });
        rb2.addActionListener(e -> {
            if(rb2.isSelected()) {
                selectedReminderInterval = 30;
            }
        });
        rb3.addActionListener(e -> {
            if(rb3.isSelected()) {
                selectedReminderInterval = 45;
            }
        });
        rb4.addActionListener(e -> {
            if(rb4.isSelected()) {
                selectedReminderInterval = 60;
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            win.change("calendar");
        });
        panel.add(cancelButton, BorderLayout.SOUTH);

        JButton createEventButton = new JButton("Create Event");
        createEventButton.addActionListener(e -> {
            boolean result = registerNewEvent(
                win.currentUserId,
                eventNameField.getText(),
                descriptionField.getText(),
                new Timestamp(((Date)startTimeSpinner.getValue()).getTime()),
                new Timestamp(((Date)endTimeSpinner.getValue()).getTime()),
                selectedReminderInterval,
                Integer.parseInt(reminderTimeFrameField.getText())
            );

            if(result) {
                win.calendarPanel.fillCalendar(win.currentUserId);
                win.change("calendar");
            } else {
                JOptionPane.showMessageDialog(
                    getParent(),
                "Error : 이벤트 생성에 실패했습니다.",
                    "Event Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        panel.add(createEventButton, BorderLayout.SOUTH);

        // SELECT MEMBERS 버튼 이벤트
        selectMembersButton.addActionListener(e -> {
            if(win.currentUserId != -1) {
                MemberSelectionPage memberSelectionPage = new MemberSelectionPage(
                    this,
                    win.currentUserId,
                    dateFormat.format((Date)startTimeSpinner.getValue()),
                    dateFormat.format((Date)endTimeSpinner.getValue())
                );
                memberSelectionPage.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(
                    getParent(),
                    "Error : 현재 유저 정보를 알 수 없습니다.",
                    "User Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        panel.setBounds(0, 0, 1000, 600);
        add(panel);
        setVisible(true);
    }

    public void setSelectedMembers(List<UserModel> availableMembers) {
        selectedMembersLabel.setText("Selected members: " + String.join(", ", availableMembers.stream().map(UserModel::getName).collect(Collectors.joining(", "))));
    }

    private boolean registerNewEvent(int hostId, String eventTitle, String description, Timestamp startDateTime, Timestamp endDateTime, int reminderInterval, int reminderTimeFrame) {
        if(reminderInterval > reminderTimeFrame) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Reminder interval must be less than or equal to reminder time frame.",
                "Event Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        // 생성하고자 하는 시간대에 내 다른 이벤트가 있는지 체크
        String SQL_CHECK_CAN_MAKE_NEW_EVENT = "SELECT events.id, event_infos.id, event_infos.event_title, event_infos.start_date_time, " +
                "event_infos.end_date_time, events.color FROM events " +
                "LEFT JOIN event_infos ON events.event_info_id = event_infos.id " +
                "WHERE events.user_id = ? AND (" +
                    "(event_infos.start_date_time >= ? AND event_infos.start_date_time < ?) " +
                    "OR (event_infos.end_date_time > ? AND event_infos.end_date_time <= ?) " +
                    "OR (event_infos.start_date_time <= ? AND event_infos.end_date_time >= ?)" +
                ") ORDER BY event_infos.start_date_time;";

        // 이벤트 정보 & 실제 이벤트 생성
        String SQL_INSERT_EVENT_INFO = "INSERT INTO event_infos (host_id, event_title, description, start_date_time, end_date_time, color) " +
                "VALUES(?, ?, ?, ?, ?, '#FF0000') RETURNING *;";
        String SQL_INSERT_EVENT = "INSERT INTO events (event_info_id, user_id, color, reminder_interval, reminder_time_frame) " +
                "VALUES(?, ?, '#FF0000', ?, ?) RETURNING *;";

        String SQL_INSERT_RSVPS = "";

        // 생성하고자 하는 시간대에 내 다른 이벤트가 있는지 체크
        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_CHECK_CAN_MAKE_NEW_EVENT)) {
            preparedStatement.setInt(1, hostId);
            preparedStatement.setTimestamp(2, startDateTime, timeZome);
            preparedStatement.setTimestamp(3, endDateTime, timeZome);
            preparedStatement.setTimestamp(4, startDateTime, timeZome);
            preparedStatement.setTimestamp(5, endDateTime, timeZome);
            preparedStatement.setTimestamp(6, startDateTime, timeZome);
            preparedStatement.setTimestamp(7, endDateTime, timeZome);

            String existEventTitle = "";
            String existEventStartDateTime = "";
            String existEventEndDateTime = "";
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                existEventTitle = resultSet.getString("event_title");
                existEventStartDateTime = resultSet.getString("start_date_time");
                existEventEndDateTime = resultSet.getString("end_date_time");
            }

            if(!existEventTitle.isEmpty() && !existEventStartDateTime.isEmpty() && !existEventEndDateTime.isEmpty()) {
                JOptionPane.showMessageDialog(
                    getParent(),
                    "Event exists : " + existEventTitle + "(" + existEventStartDateTime + " ~ " + existEventEndDateTime + ")",
                    "Event Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            else {
                // 이벤트 정보 & 실제 이벤트 생성
                try (PreparedStatement preparedStatement2 = conn.prepareStatement(SQL_INSERT_EVENT_INFO, Statement.RETURN_GENERATED_KEYS)) {
                    // insert student record
                    preparedStatement2.setInt(1, hostId);
                    preparedStatement2.setString(2, eventTitle);
                    preparedStatement2.setString(3, description);
                    preparedStatement2.setTimestamp(4, startDateTime, timeZome);
                    preparedStatement2.setTimestamp(5, endDateTime, timeZome);
                    preparedStatement2.executeUpdate();

                    ResultSet rs = preparedStatement2.getGeneratedKeys();
                    if(rs.next()) {
                        int eventInfoId = rs.getInt(1);

                        try (PreparedStatement preparedStatement3 = conn.prepareStatement(SQL_INSERT_EVENT, Statement.RETURN_GENERATED_KEYS)) {
                            // insert student record
                            preparedStatement3.setInt(1, eventInfoId);
                            preparedStatement3.setInt(2, hostId);
                            preparedStatement3.setInt(3, reminderInterval);
                            preparedStatement3.setInt(4, reminderTimeFrame);

                            if(reminderInterval == 0) {
                                return preparedStatement3.execute();
                            } else {
                                preparedStatement3.executeUpdate();
                                ResultSet rs2 = preparedStatement3.getGeneratedKeys();

                                if(rs2.next()) {
                                    int _eventId = rs2.getInt(1);
                                    return addReminders(_eventId, startDateTime, reminderInterval, reminderTimeFrame);
                                } else {
                                    return false;
                                }
                            }
                        } catch (SQLException e) {
                            JOptionPane.showMessageDialog(
                                getParent(),
                                "Error : " + e.getMessage(),
                                "Event Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(
                                getParent(),
                                "Error : " + e.getMessage(),
                                "Event Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                            e.printStackTrace();
                        }
                    } else {
                        return false;
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(
                        getParent(),
                        "Error : " + e.getMessage(),
                        "Event Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        getParent(),
                        "Error : " + e.getMessage(),
                        "Event Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Event Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }

        return false;
    }

    private boolean addReminders(int eventId, Timestamp startDateTime, int reminderInterval, int reminderTimeFrame) {
        final List<Timestamp> remindAtList = new ArrayList<>();

        for(int min = reminderTimeFrame; min > 0; min -= reminderInterval) {
            timeZome.setTime(startDateTime);
            timeZome.add(Calendar.MINUTE, -min);
            remindAtList.add(new Timestamp(timeZome.getTime().getTime()));
        }

        StringBuilder sb = new StringBuilder();
        for(Timestamp _ : remindAtList) {
            sb.append("(" + eventId + ", ?),");
        }
        String SQL_INSERT_REMINDER = "INSERT INTO reminders (event_id, remind_at) VALUES " + sb.substring(0, sb.length()-1);
        System.out.println(SQL_INSERT_REMINDER);

        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
            PreparedStatement preparedStatement = conn.prepareStatement(SQL_INSERT_REMINDER)) {
            for(int i=1; i<=remindAtList.size(); i++) {
                preparedStatement.setTimestamp(i, remindAtList.get(i-1), timeZome);
            }
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Event Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Event Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }

        return false;
    }
}

class MemberSelectionPage extends JFrame {
    private final EventBookingPanel eventBookingPage;
    private final JList<UserModel> memberList;
    private final List<UserModel> userList = new ArrayList<>();

    public MemberSelectionPage(EventBookingPanel eventBookingPage, int currentUserId, String startDateTime, String endDateTime) {
        this.eventBookingPage = eventBookingPage;
        loading(currentUserId);

        setTitle("Select Members");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose this frame when closed.

        memberList = new JList<>(userList.toArray(new UserModel[0]));
        memberList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane listScroller = new JScrollPane(memberList);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(listScroller, BorderLayout.CENTER); // And here

        JButton checkAvailabilityButton = new JButton("Check Availability");
        checkAvailabilityButton.addActionListener(e -> checkAvailability());
        panel.add(checkAvailabilityButton, BorderLayout.SOUTH);

        add(panel);
    }

    /*private boolean isMemberAvailable(UserModel memberName) {
        return Math.random() < 0.5;
    }*/

    private void checkAvailability() {
        List<UserModel> selectedMembers = memberList.getSelectedValuesList();

        JOptionPane.showMessageDialog(
            this,
            "Available Members:" + selectedMembers.stream().map(UserModel::getName).collect(Collectors.joining(", "))
        );

        eventBookingPage.setSelectedMembers(selectedMembers);
        dispose();
    }

    private void loading(int currentUserId) {
        String SQL_SELECT = "SELECT id, name FROM users WHERE id != ?;";

        // establishes database connection
        // auto closes connection and preparedStatement
        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {
            preparedStatement.setInt(1, currentUserId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");

                UserModel userModel = new UserModel();
                userModel.setId(id);
                userModel.setName(name);
                userList.add(userModel);
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
}
