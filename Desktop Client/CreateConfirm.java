import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CreateConfirm {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Confirm Order");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        
        JPanel panel = new JPanel(new GridLayout(4, 2));
        
        JLabel orderIdLabel = new JLabel("Order ID:");
        JTextField orderIdField = new JTextField();
        
        JLabel paymentIdLabel = new JLabel("Payment ID:");
        JTextField paymentIdField = new JTextField();
        
        JButton submitButton = new JButton("Submit");
        JLabel responseLabel = new JLabel();
        
        panel.add(orderIdLabel);
        panel.add(orderIdField);
        panel.add(paymentIdLabel);
        panel.add(paymentIdField);
        panel.add(new JLabel()); // empty cell
        panel.add(submitButton);
        panel.add(new JLabel()); // empty cell
        panel.add(responseLabel);
        
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String orderId = orderIdField.getText();
                String paymentId = paymentIdField.getText();
                
                if (orderId.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Order ID is required.");
                    return;
                }
                
                try {
                    URL url = new URL("http://localhost:8000/api/pat/confirm");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    
                    String postData = String.format("{\"orderId\": \"%s\", \"paymentId\": %s}", orderId, paymentId.isEmpty() ? "null" : paymentId);
                    
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);           
                    }
                    
                    int responseCode = conn.getResponseCode();
                    StringBuilder response = new StringBuilder();
                    
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    } catch (Exception ex) {
                        // Read the error response
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                        }
                    }

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        responseLabel.setText("Order confirmed successfully! Response: " + response.toString());
                    } else {
                        responseLabel.setText("Failed to confirm order: " + response.toString());
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    responseLabel.setText("Error connecting to server: " + ex.getMessage());
                }
            }
        });
        
        frame.add(panel);
        frame.setVisible(true);
    }

    public void setVisible(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setVisible'");
    }
}
