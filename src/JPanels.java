import javax.swing.*;
import java.sql.*;

class JPanels extends JFrame {
    public LoginPanel loginPanel = null;
    public JoinPanel joinPanel = null;
    public CalendarPanel calendarPanel = null;
    public EventBookingPanel eventBookingPanel = null;
    public EventDetailPanel eventDetailPanel = null;

    public int currentUserId = -1;
    public UserModel currentUserModel;

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
        }
    }
}