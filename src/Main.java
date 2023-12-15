import javax.swing.*;

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
    }
}