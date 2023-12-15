import javax.swing.*;
import java.sql.*;

public class JoinPanel extends JPanel {
    public JoinPanel(JPanels win) {
        setLayout(null);

        JLabel usernameLable = new JLabel("Username:");
        usernameLable.setBounds(350, 150, 100, 25);
        add(usernameLable);
        JTextField usernameField = new JTextField();
        usernameField.setBounds(450, 150, 200, 25);
        usernameField.setBorder(BorderFactory.createLineBorder(null));
        add(usernameField);

        JLabel userIdLable = new JLabel("User ID:");
        userIdLable.setBounds(350, 200, 100, 25);
        add(userIdLable);
        JTextField userIdField = new JTextField();
        userIdField.setBounds(450, 200, 200, 25);
        userIdField.setBorder(BorderFactory.createLineBorder(null));
        add(userIdField);

        JLabel passwordLable = new JLabel("Password:");
        passwordLable.setBounds(350, 250, 100, 25);
        add(passwordLable);
        JTextField passwordField = new JTextField();
        passwordField.setBounds(450, 250, 200, 25);
        passwordField.setBorder(BorderFactory.createLineBorder(null));
        add(passwordField);

        JLabel emailLable = new JLabel("Email:");
        emailLable.setBounds(350, 300, 100, 25);
        add(emailLable);
        JTextField emailField = new JTextField();
        emailField.setBounds(450, 300, 200, 25);
        emailField.setBorder(BorderFactory.createLineBorder(null));
        add(emailField);


        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(400, 400, 100, 30);
        cancelButton.addActionListener(e -> {
            win.change("login");
        });
        add(cancelButton);

        JButton joinButton = new JButton("Join");
        joinButton.setBounds(550, 400, 100, 30);
        joinButton.addActionListener(e -> {
            boolean joinSucceed = join(usernameField.getText(), userIdField.getText(), passwordField.getText(), emailField.getText());
            if(joinSucceed) {
                JOptionPane.showMessageDialog(
                    getParent(),
                    "Hello " + usernameField.getText() + "!",
                    "New User",
                    JOptionPane.PLAIN_MESSAGE
                );
                win.change("login");
            } else {
                JOptionPane.showMessageDialog(
                    getParent(),
                    "Failed to create new User.",
                    "Join Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        add(joinButton);
        setVisible(true);
    }

    boolean join(String userName, String userId, String password, String email) {
        String SQL_INSERT = "INSERT INTO users (name, user_id, password, email, notification_channel) " +
                "VALUES(?, ?, ?, ?, 'pop_up_message_box') RETURNING id;";

        // establishes database connection
        // auto closes connection and preparedStatement
        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_INSERT)) {
            // insert student record
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, userId);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, email);

            return preparedStatement.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    getParent(),
                    "Error : " + e.getMessage(),
                    "Join Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    getParent(),
                    "Error : " + e.getMessage(),
                    "Join Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
        return false;
    }
}
