import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventReminderPanel extends JPanel {
    final JPanels win;

    public List<ReminderModel> reminderList = new ArrayList<>();

    public EventReminderPanel(JPanels win) {
        this.win = win;
        _setLayout();
    }

    private void _setLayout() {
        setLayout(null);
        loadReminders();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 10, 10)); // Grid layout with 2 columns and gaps

        // Add title at the top
        JLabel titleLabel = new JLabel("Event Reminders", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Sample data. Replace with your actual list of events.
        for (int i = 1; i <= reminderList.size(); i++) {
            ReminderModel reminderModel = reminderList.get(i-1);

            JPanel eventPanel = new JPanel(new BorderLayout());
            eventPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // add margin

            if (i % 2 == 0) {
                eventPanel.setBackground(Color.LIGHT_GRAY);
            } else {
                eventPanel.setBackground(Color.WHITE);
            }

            JLabel userNameLabel = new JLabel("HOST : " + reminderModel.hostName);
            eventPanel.add(userNameLabel,BorderLayout.WEST);

            String dateTimeEventNameText = "<html>" + reminderModel.remindAt + "<br/>" + reminderModel.eventTitle + "</html>";
            JLabel dateTimeEventNameLabel = new JLabel(dateTimeEventNameText);
            eventPanel.add(dateTimeEventNameLabel,BorderLayout.EAST);

            JButton checkButton;
            checkButton = new JButton("remind!!!");

            checkButton.addActionListener(e -> {
                noticedReminder(reminderModel);
            });

            String descriptionText = "<html>" + reminderModel.description + "</html>";
            JLabel descriptionLabel = new JLabel(descriptionText);
            descriptionLabel.setFont(new Font("Arial", Font.PLAIN ,12));
            descriptionLabel.setBorder(BorderFactory.createEmptyBorder(5 ,5 ,5 ,5 ));

            JPanel southPanel= new JPanel();
            southPanel.setLayout(new BoxLayout(southPanel ,BoxLayout.Y_AXIS));

            southPanel.setOpaque(false);   // make it transparent

            southPanel.add(descriptionLabel);
            southPanel.add(checkButton);

            eventPanel.add(southPanel,BorderLayout.SOUTH);

            mainPanel.add(eventPanel);
        }

        titleLabel.setBounds(200, 0, 600, 40);
        add(titleLabel);

        JButton prevButton = new JButton("<");
        prevButton.setBounds(0, 0, 120, 40);
        prevButton.addActionListener(e -> {
            win.calendarPanel.fillCalendar(win.currentUserId);
            win.change("calendar");
        });
        add(prevButton);

        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setBounds(0, 40, 1000, 540);
        add(mainScrollPane);

        setVisible(true);
    }

    void noticedReminder(ReminderModel reminderModel) {
        String SQL_NOTICED_REMINDER = "UPDATE reminders " +
                "SET showed_reminder = true " +
                "WHERE id = ?;";

        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement2 = conn.prepareStatement(SQL_NOTICED_REMINDER)) {
            preparedStatement2.setInt(1, reminderModel.id);
            preparedStatement2.executeUpdate();

            loadReminders();

            this.removeAll();
            this.revalidate();
            this.repaint();
            _setLayout();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    getParent(),
                    "Error : " + e.getMessage(),
                    "Reminder Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    getParent(),
                    "Error : " + e.getMessage(),
                    "Reminder Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    public void loadReminders() {
        String SQL_SELECT_ACTIVE_REMINDERS = "SELECT DISTINCT ON(event_id) event_id, reminders.id, reminders.event_id, " +
                "remind_at, event_title, start_date_time, end_date_time, description, users.name as host FROM reminders " +
                "LEFT JOIN events ON reminders.event_id = events.id " +
                "LEFT JOIN event_infos ON events.event_info_id = event_infos.id " +
                "LEFT JOIN users ON event_infos.host_id = users.id " +
                "WHERE events.user_id = ? " +
                "AND (NOW() + INTERVAL '1 hours') >= reminders.remind_at " +
                "AND NOW() <= reminders.remind_at " +
                "AND reminders.showed_reminder = false " +
                "ORDER BY reminders.event_id, reminders.remind_at;";

        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT_ACTIVE_REMINDERS)) {
            preparedStatement.setInt(1, win.currentUserId);
            ResultSet resultSet = preparedStatement.executeQuery();

            reminderList.clear();
            while (resultSet.next()) {
                ReminderModel reminderModel = new ReminderModel();
                reminderModel.id = resultSet.getInt("id");
                reminderModel.eventId = resultSet.getInt("event_id");
                reminderModel.remindAt = resultSet.getString("remind_at");
                reminderModel.eventTitle = resultSet.getString("event_title");
                reminderModel.startDateTime = resultSet.getString("start_date_time");
                reminderModel.endDateTime = resultSet.getString("end_date_time");
                reminderModel.description = resultSet.getString("description");
                reminderModel.hostName = resultSet.getString("host");
                reminderList.add(reminderModel);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    getParent(),
                    "Error : " + e.getMessage(),
                    "Reminder Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    getParent(),
                    "Error : " + e.getMessage(),
                    "Reminder Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
}
