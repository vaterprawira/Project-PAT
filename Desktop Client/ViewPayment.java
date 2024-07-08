import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class ViewPayment {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ViewPayment::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Payment");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel viewPaymentPanel = new JPanel(new BorderLayout());
        placeViewPaymentComponents(viewPaymentPanel);

        mainPanel.add(viewPaymentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void placeViewPaymentComponents(JPanel panel) {
        panel.removeAll();

        String url = "http://localhost:8000/api/pat/payment";
        try {
            String jsonData = getJSONData(url);
            System.out.println("Response JSON: " + jsonData); // Cetak JSON untuk debugging
            JSONObject jsonObject = new JSONObject(jsonData);

            // Pastikan kunci yang diharapkan ada dalam JSON sebelum mengaksesnya
            if (jsonObject.has("status") && jsonObject.getInt("status") == 200 && 
                jsonObject.has("response")) {
                JSONArray payments = jsonObject.getJSONArray("response");

                String[] columnNames = {"Payment ID", "Order ID", "Metode Pembayaran", "Virtual Account", "Status"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);

                for (int i = 0; i < payments.length(); i++) {
                    JSONObject payment = payments.getJSONObject(i);

                    int paymentId = payment.getInt("id");
                    int orderId = payment.getInt("orderId");
                    String metodePembayaran = payment.getString("metode_pembayaran");
                    String virtualAccount = payment.getString("virtual_account");
                    String status = payment.getString("status");

                    model.addRow(new Object[]{paymentId, orderId, metodePembayaran, virtualAccount, status});
                }

                JTable table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);

                panel.add(scrollPane, BorderLayout.CENTER);

            } else {
                JOptionPane.showMessageDialog(panel, "Error: Data format is not as expected.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Error: Failed to fetch payment details.");
        }

        panel.revalidate();
        panel.repaint();
    }

    private static String getJSONData(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        conn.disconnect();
        return sb.toString();
    }
}
