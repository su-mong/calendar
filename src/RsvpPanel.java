import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RsvpPanel extends JPanel {
    private JPanels win;

    private List<RsvpModel> rsvpModelList = new ArrayList<>();

    public RsvpPanel(JPanels win) {
        setLayout(null);
        this.win = win;

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 10, 10)); // Grid layout with 2 columns and gaps

        // Add title at the top
        JLabel titleLabel = new JLabel("RSVP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Sample data. Replace with your actual list of events.
        for (int i = 1; i <= rsvpModelList.size(); i++) {
            JPanel eventPanel = new JPanel(new BorderLayout());
            eventPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // add margin

            if (i % 2 == 0) {
                eventPanel.setBackground(Color.LIGHT_GRAY);
            } else {
                eventPanel.setBackground(Color.WHITE);
            }

            JLabel userNameLabel = new JLabel("User " + i);
            eventPanel.add(userNameLabel,BorderLayout.WEST);

            String dateTimeEventNameText = "<html>2023-01-0" + i + " / 10:00<br/>Event " + i + "</html>";
            JLabel dateTimeEventNameLabel = new JLabel(dateTimeEventNameText);
            eventPanel.add(dateTimeEventNameLabel,BorderLayout.EAST);

            JButton checkButton;
            if (i % 2 == 0) {
                checkButton = new JButton("30min left check");
            } else {
                checkButton = new JButton("15min left check");
            }

            checkButton.addActionListener(e -> {
                // Add your action code here
            });

            String descriptionText = "<html>Description for Event " + i + "</html>";
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

    public void loadRsvps() {
        String SQL_SELECT_RSVPS = "SELECT rsvps.id, rsvps.event_id, rsvps.rsvp_sender_id, rsvps.received_at, rsvps.description, event_infos.event_title, " +
                "event_infos.description, event_infos.start_date_time, event_infos.end_date_time, users.name, event_infos.id " +
                "FROM rsvps " +
                "INNER JOIN events ON events.id = rsvps.event_id " +
                "INNER JOIN users ON users.id = rsvps.rsvp_sender_id " +
                "INNER JOIN event_infos ON events.event_info_id = event_infos.id " +
                "WHERE rsvp_receiver_id = ? " +
                    "AND received_at >= (NOW() - INTERVAL '10 minutes') " +
                "ORDER BY received_at;";

        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT_RSVPS)) {
            preparedStatement.setInt(1, win.currentUserId);
            ResultSet resultSet = preparedStatement.executeQuery();

            rsvpModelList.clear();
            while (resultSet.next()) {
                RsvpModel reminderModel = new RsvpModel();
                reminderModel.id = resultSet.getInt("id");
                reminderModel.eventId = resultSet.getInt("event_id");
                reminderModel.remindAt = resultSet.getString("remind_at");
                reminderModel.eventTitle = resultSet.getString("event_title");
                reminderModel.startDateTime = resultSet.getString("start_date_time");
                reminderModel.endDateTime = resultSet.getString("end_date_time");
                reminderModel.description = resultSet.getString("description");
                reminderModel.hostName = resultSet.getString("host");
                rsvpModelList.add(reminderModel);
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
