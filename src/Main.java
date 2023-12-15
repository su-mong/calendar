import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        JPanels win = new JPanels();

        win.setTitle("Family Calendar");
        win.setSize(1000, 600);
        win.loginPanel = new LoginPanel(win);
        win.joinPanel = new JoinPanel(win);
        win.calendarPanel = new CalendarPanel(win);
        win.eventBookingPanel = new EventBookingPanel(win, null, -1);

        win.add(win.loginPanel);
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        win.setLocationRelativeTo(null);
        win.setLocationRelativeTo(null);
        win.setVisible(true);

        // 1분 단위로 리마인더 테스크를 작동시켜서, 리마인더를 갱신한다.
        Timer reminderTimer = new Timer();
        TimerTask reminderTask = new TimerTask() {
            @Override
            public void run() {
                if(win.currentUserId != -1) {
                    win.loadReminders();
                }
            }
        };
        reminderTimer.schedule(reminderTask, 30000, 60000);







        String createT = null;
        Statement smt = null;

        try {
            // Class.forName("com.postgresql.cj.jdbc.Driver");
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword)) {System.out.println("Connection established successfully");

            // PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT)) {
            createT = "CREATE TABLE IF NOT EXISTS zzz_users (id SERIAL PRIMARY KEY,name VARCHAR(100) NOT NULL,user_id VARCHAR(20) UNIQUE NOT NULL,password VARCHAR(100) NOT NULL,email VARCHAR(100) UNIQUE NOT NULL,notification_channel VARCHAR(100));";
            smt = connection.createStatement();
            smt.executeUpdate(createT);

            createT = "CREATE TABLE IF NOT EXISTS zzz_event_infos (id SERIAL PRIMARY KEY,host_id SERIAL NOT NULL,CONSTRAINT fk_events_host_id FOREIGN KEY(host_id) REFERENCES users(id),event_title VARCHAR(255) NOT NULL,start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,color VARCHAR(7) NOT NULL,description TEXT);";
            smt = connection.createStatement();
            smt.executeUpdate(createT);


            createT = "CREATE TABLE IF NOT EXISTS zzz_events (id SERIAL PRIMARY KEY,event_info_id SERIAL NOT NULL,CONSTRAINT fk_event_info_id FOREIGN KEY(event_info_id) REFERENCES event_infos(id),user_id SERIAL NOT NULL,CONSTRAINT fk_events_user_id FOREIGN KEY(user_id) REFERENCES users(id),color VARCHAR(7) NOT NULL,reminder_interval INT DEFAULT 0,  reminder_time_frame INT );";
            smt = connection.createStatement();
            smt.executeUpdate(createT);

            createT = "CREATE TABLE IF NOT EXISTS zzz_reminders (id SERIAL PRIMARY KEY,event_id SERIAL,CONSTRAINT fk_reminders_event_id FOREIGN KEY(event_id) REFERENCES events(id),remind_at TIMESTAMP WITH TIME ZONE NOT NULL,showed_reminder BOOLEAN DEFAULT FALSE);";
            smt = connection.createStatement();
            smt.executeUpdate(createT);


            createT = "CREATE TABLE IF NOT EXISTS zzz_rsvps (id SERIAL PRIMARY KEY,event_id SERIAL,CONSTRAINT fk_rsvps_event_id FOREIGN KEY(event_id) REFERENCES events(id),rsvp_sender_id SERIAL,CONSTRAINT fk_rsvps_sender_id FOREIGN KEY(rsvp_sender_id) REFERENCES users(id),rsvp_receiver_id SERIAL,CONSTRAINT fk_rsvps_receiver_id FOREIGN KEY(rsvp_receiver_id) REFERENCES users(id),  received_at TIMESTAMP WITH TIME ZONE NOT NULL,is_participated BOOLEAN DEFAULT FALSE,description TEXT);";
            smt = connection.createStatement();
            smt.executeUpdate(createT);

            connection.close();

        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
    }
}