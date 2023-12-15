import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.List;

public class EventDetailPanel extends JPanel {
    private JPanels win;
    private int eventId;

    private String eventName = "Yoomin's Birthday";
    private String userName = "Yoomin";
    private String timeData = "2023-01-04";
    private String description = "00th birthday at fdfjdsfjdkslfjkslfjfkdfjdksjffjsdjff fdkfjdfjdkfjdkfjd";
    private int reminderInterval;
    private int reminderTimeFrame;
    private String hostName;

    public EventDetailPanel(JPanels win, int eventId) {
        this.win = win;
        this.eventId = eventId;
        loading();

        setLayout(null);

        // Create the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        // Add top margin to main panel
        mainPanel.setBorder(BorderFactory.createEmptyBorder(60, 30, 30, 30));

        // Create a panel for the event name and cancel button
        JPanel eventNamePanel = new JPanel(new BorderLayout());

        // Event Name
        JLabel eventNameLabel = new JLabel(eventName);
        eventNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        eventNamePanel.add(eventNameLabel, BorderLayout.WEST);

        JPanel editButtonPanel = new JPanel(new BorderLayout());
        editButtonPanel.setLayout(new BoxLayout(editButtonPanel, BoxLayout.PAGE_AXIS));

        // Cancel Event Button
        JButton cancelButton = new JButton("Cancel Event");
        cancelButton.addActionListener(e -> {
            deleteEvent();
        });
        editButtonPanel.add(cancelButton);

        // Modify event
        JButton modifyButton = new JButton("Modify Event");
        modifyButton.addActionListener(e -> {
            // Add your action code here
        });
        editButtonPanel.add(modifyButton);

        eventNamePanel.add(editButtonPanel, BorderLayout.EAST);


        mainPanel.add(eventNamePanel);

        // Username
        JLabel usernameLabel = new JLabel("Username: " + userName);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align username label
        mainPanel.add(usernameLabel);

        // Time
        JLabel timeLabel = new JLabel("Time: " + timeData);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align time label
        mainPanel.add(timeLabel);

        // Reminder + Time Frame
        JLabel reminderLabel = new JLabel("Reminder: " + reminderInterval + " interval, " + reminderTimeFrame + " time frame");
        reminderLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align time label
        mainPanel.add(reminderLabel);

        // Host Name
        JLabel hostLabel = new JLabel("Host Name: " + hostName);
        hostLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-align time label
        mainPanel.add(hostLabel);

        // Description
        JTextArea descriptionTextArea = new JTextArea(description, 10, 20);
        descriptionTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
        mainPanel.add(scrollPane);

        // Additional Description TextField
        /*
        JTextField additionalDescriptionTextField = new JTextField(4);
        mainPanel.add(additionalDescriptionTextField);

        // Add Description Button
        JButton addDescriptionButton = new JButton("Add Description");
        addDescriptionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String additionalDescription = additionalDescriptionTextField.getText();
                descriptionTextArea.append("\n" + additionalDescription);
                additionalDescriptionTextField.setText("");
            }
        });
        mainPanel.add(addDescriptionButton);
        */

        // Go to Monthly Page Button and Go to Daily Page Button
        JPanel buttonPanel = new JPanel();
        JButton monthlyPageButton = new JButton("Go to Monthly Page");
        JButton dailyPageButton = new JButton("Go to Daily Page");

        buttonPanel.add(monthlyPageButton);
        buttonPanel.add(dailyPageButton);

        monthlyPageButton.addActionListener(e -> {
            win.change("calendar");
            win.calendarPanel.fillCalendar(win.currentUserId);
        });

        dailyPageButton.addActionListener(e -> JOptionPane.showMessageDialog(EventDetailPanel.this, "Redirecting to Daily Page..."));

        mainPanel.add(buttonPanel);
        mainPanel.setBounds(0, 0, 1000, 600);
        add(mainPanel);
        setVisible(true);
    }

    void loading() {
        String SQL_GET_EVENT_INFO = "SELECT * FROM events LEFT JOIN event_infos ON events.event_info_id = event_infos.id " +
                "LEFT JOIN users ON event_infos.host_id = users.id WHERE events.id = ?;";

        try (Connection conn = DriverManager.getConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_GET_EVENT_INFO)) {
            preparedStatement.setInt(1, this.eventId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                eventName = resultSet.getString("event_title");
                userName = win.currentUserModel.name;
                timeData = resultSet.getString("start_date_time") + " ~ " + resultSet.getString("end_date_time");
                description = resultSet.getString("description");

                reminderInterval = resultSet.getInt("reminder_interval");
                reminderTimeFrame = resultSet.getInt("reminder_time_frame");
                hostName = resultSet.getString("name");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Event Detail Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.out.print(e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Event Detail Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // CalendarPanel 내의 CalendarOneDayPopup에 있는 코드와 거의 동일.
    private void deleteEvent() {
        String SQL_CHECK_HOST_IS_ME = "SELECT * FROM events LEFT JOIN event_infos ON events.event_info_id = event_infos.id " +
                "WHERE events.id = ? AND event_infos.host_id = ? AND events.user_id = ?;";
        String SQL_DELETE_NOT_MY_HOST = "DELETE FROM events WHERE id = ?;";
        String SQL_DELETE_MY_HOST = "DELETE FROM events WHERE event_info_id = ?; DELETE FROM event_infos WHERE id = ?;";

        // establishes database connection
        // auto closes connection and preparedStatement
        try (Connection conn = DriverManager.getConnection(Constants.dbUrl, Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_CHECK_HOST_IS_ME)) {
            preparedStatement.setInt(1, eventId);
            preparedStatement.setInt(2, win.currentUserId);
            preparedStatement.setInt(3, win.currentUserId);

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
                    preparedStatement2.setInt(1, eventId);
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

        win.change("calendar");
        win.calendarPanel.fillCalendar(win.currentUserId);
    }
}
