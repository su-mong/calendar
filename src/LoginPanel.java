import javax.swing.*;
import java.sql.*;

class LoginPanel extends JPanel {
    public LoginPanel(JPanels win) {
        setLayout(null);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(450,200,200,25);
        usernameField.setBorder(BorderFactory.createLineBorder(null));
        add(usernameField);

        JTextField passwordField = new JTextField();
        passwordField.setBounds(450,250,200,25);
        passwordField.setBorder(BorderFactory.createLineBorder(null));
        add(passwordField);

        JLabel usernameLable = new JLabel("Username:");
        usernameLable.setBounds(350,200,100,25);
        add(usernameLable);

        JLabel passwordLable = new JLabel("Password:");
        passwordLable.setBounds(350,250,100,25);
        add(passwordLable);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(400, 300, 100, 30);
        loginButton.addActionListener(e -> {
            UserModel currentUser = login(usernameField.getText(), passwordField.getText());
            if(currentUser.id != -1) {
                win.currentUserId = currentUser.id;
                win.currentUserModel = currentUser;
                win.loadReminders();
                win.change("calendar");
            } else {
                JOptionPane.showMessageDialog(
                    getParent(),
                    "The userID and password are not matched!",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        add(loginButton);

        JButton joinButton = new JButton("Join");
        joinButton.setBounds(550, 300, 100, 30);
        joinButton.addActionListener(e -> {
            win.change("join");
        });
        add(joinButton);
    }

    UserModel login(String userName, String password) {
        String SQL_INSERT = "SELECT * FROM users WHERE user_id = ? AND password = ?;";

        // establishes database connection
        // auto closes connection and preparedStatement
        try (Connection conn = DriverManager.getConnection(Constants.dbUrl,Constants.dbUser, Constants.dbPassword);
            PreparedStatement preparedStatement = conn.prepareStatement(SQL_INSERT)) {
            // insert student record
            preparedStatement.setString(1, userName); //1 specifies the first parameter in the query
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            UserModel user = new UserModel();
            while (resultSet.next()) {
                user = new UserModel();
                user.id = resultSet.getInt("id");
                user.name = resultSet.getString("name");
            }
            return user;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Login Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                getParent(),
                "Error : " + e.getMessage(),
                "Login Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }

        return new UserModel();
    }
}