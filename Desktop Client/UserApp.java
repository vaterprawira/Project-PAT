import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UserApp {

    public static void main(String[] args) {
        showLoginFrame();
    }

    private static void showLoginFrame() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeLoginComponents(panel);

        frame.setVisible(true);
    }

    private static void showRegisterFrame() {
        JFrame frame = new JFrame("Register");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeRegisterComponents(panel);

        frame.setVisible(true);
    }

    private static void placeLoginComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 100, 80, 25);
        panel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(180, 100, 100, 25);
        panel.add(registerButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());

                try {
                    String url = "http://localhost:8000/api/pat/login";
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
                    conn.setRequestProperty("Content-Type", "application/json");

                    String newMenu = "{\"username\": \"" + username + "\", \"pass\": \"" + password + "\"}";

                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(newMenu.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();

                    if (responseCode == 200) {
                        JOptionPane.showMessageDialog(panel, "Login successful");
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
                        frame.dispose();
                        new Main();
                    } else {
                        JOptionPane.showMessageDialog(panel, "Invalid username or password");
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Error connecting to server: " + ex.getMessage());
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
                frame.dispose();
                showRegisterFrame();
            }
        });
    }

    private static void placeRegisterComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel nameLabel = new JLabel("Nama:");
        nameLabel.setBounds(10, 50, 80, 25);
        panel.add(nameLabel);

        JTextField nameText = new JTextField(20);
        nameText.setBounds(100, 50, 165, 25);
        panel.add(nameText);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(10, 80, 80, 25);
        panel.add(emailLabel);

        JTextField emailText = new JTextField(20);
        emailText.setBounds(100, 80, 165, 25);
        panel.add(emailText);

        JLabel phoneLabel = new JLabel("Telepon:");
        phoneLabel.setBounds(10, 110, 80, 25);
        panel.add(phoneLabel);

        JTextField phoneText = new JTextField(20);
        phoneText.setBounds(100, 110, 165, 25);
        panel.add(phoneText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 140, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 140, 165, 25);
        panel.add(passwordText);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(10, 200, 100, 25);
        panel.add(registerButton);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(180, 200, 80, 25);
        panel.add(loginButton);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                String nama = nameText.getText();
                String email = emailText.getText();
                String telepon = phoneText.getText();

                if (username.isEmpty() || password.isEmpty() || nama.isEmpty() || email.isEmpty() || telepon.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Please fill in all fields.");
                    return;
                }

                try {
                    URL url = new URL("http://localhost:8000/api/pat/register/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("User-Agent", "Chrome/51.0.2704.103");
                    conn.setRequestProperty("Content-Type", "application/json");

                    String jsonInputString = String.format("{\"username\": \"%s\", \"pass\": \"%s\", \"nama\": \"%s\", \"email\": \"%s\", \"telepon\": \"%s\"}",
                            username, password, nama, email, telepon);

                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();

                    if (responseCode == 200) {
                        JOptionPane.showMessageDialog(panel, "Registration successful. Redirecting to login page.");
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
                        frame.dispose();
                        showLoginFrame();
                    } else {
                        JOptionPane.showMessageDialog(panel, "Registration failed. Please try again.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Error connecting to server: " + ex.getMessage());
                }
            }
        });

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel);
                frame.dispose();
                showLoginFrame();
            }
        });
    }
}
