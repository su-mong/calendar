import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class JPanels extends JFrame {
    public LoginPanel loginPanel = null;
    public JoinPanel joinPanel = null;
    public CalendarPanel calendarPanel = null;
    public EventBookingPanel eventBookingPanel = null;
    public EventDetailPanel eventDetailPanel = null;
    public EventReminderPanel eventReminderPanel = null;
    public RsvpPanel rsvpPanel = null;

    public int currentUserId = -1;
    public UserModel currentUserModel;

    public List<ReminderModel> reminderList = new ArrayList<>();

    public void change(String panelName) {
        switch(panelName) {
            case "login":
                getContentPane().removeAll();
                getContentPane().add(loginPanel);
                revalidate();
                repaint();
                break;
            case "join":
                getContentPane().removeAll();
                getContentPane().add(joinPanel);
                revalidate();
                repaint();
                break;
            case "calendar":
                calendarPanel.init(currentUserId);
                getContentPane().removeAll();
                getContentPane().add(calendarPanel);
                revalidate();
                repaint();
                break;
            case "eventBooking":
                getContentPane().removeAll();
                getContentPane().add(eventBookingPanel);
                revalidate();
                repaint();
                break;
            case "eventDetail":
                getContentPane().removeAll();
                getContentPane().add(eventDetailPanel);
                revalidate();
                repaint();
                break;
            case "eventReminder":
                getContentPane().removeAll();
                getContentPane().add(eventReminderPanel);
                revalidate();
                repaint();
                break;
            case "rsvp":
                getContentPane().removeAll();
                getContentPane().add(rsvpPanel);
                revalidate();
                repaint();
                break;
        }
    }

    public void loadReminders() {
        String SQL_SELECT_ACTIVE_REMINDERS = "SELECT DISTINCT ON(event_id) event_id, reminders.id, reminders.event_id, " +
                    "remind_at, event_title, start_date_time, end_date_time, description, users.name as host FROM reminders " +
                "LEFT JOIN events ON reminders.event_id = events.id " +
                "LEFT JOIN event_infos ON events.event_info_id = event_infos.id " +
                "LEFT JOIN users ON event_infos.host_id = users.id " +
                "WHERE events.user_id = ? " +
                "AND (NOW() + INTERVAL '30 seconds') >= reminders.remind_at "+
                "AND (NOW() - INTERVAL '30 seconds') <= reminders.remind_at "+
                    "AND reminders.showed_reminder = false " +
                "ORDER BY reminders.event_id, reminders.remind_at;";

        String SQL_NOTICED_REMINDER = "UPDATE reminders " +
                "SET showed_reminder = true " +
                "WHERE id = ?;";

        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT_ACTIVE_REMINDERS)) {
            preparedStatement.setInt(1, currentUserId);
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

            if(reminderList.size() == 1) {
                JOptionPane.showMessageDialog(
                    getParent(),
                    reminderList.get(0).eventTitle + "event will start at " + reminderList.get(0).startDateTime,
                    "There is 1 reminder!",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else if(reminderList.size() > 1) {
                JOptionPane.showMessageDialog(
                    getParent(),
                    reminderList.get(0).eventTitle + "event will start at " + reminderList.get(0).startDateTime + ", and there are other reminders...",
                    "There are " + reminderList.size() + " reminders!",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

            if(reminderList.size() >= 1) {
                try (PreparedStatement preparedStatement2 = conn.prepareStatement(SQL_NOTICED_REMINDER)) {
                    preparedStatement2.setInt(1, reminderList.get(0).id);
                    preparedStatement2.executeUpdate();
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

        System.out.println("loadReminders worked. reminderList size is " + reminderList.size());
    }
}