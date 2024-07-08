import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class ViewConfirm {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ViewConfirm::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("View Confirm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel viewConfirmPanel = new JPanel(new BorderLayout());
        placeViewConfirmComponents(viewConfirmPanel);

        mainPanel.add(viewConfirmPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void placeViewConfirmComponents(JPanel panel) {
        panel.removeAll();

        String url = "http://localhost:8000/api/pat/confirm";
        try {
            String jsonData = getJSONData(url);
            System.out.println("Response JSON: " + jsonData); // Cetak JSON untuk debugging
            JSONObject jsonObject = new JSONObject(jsonData);

            // Pastikan kunci yang diharapkan ada dalam JSON sebelum mengaksesnya
            if (jsonObject.has("status") && jsonObject.getInt("status") == 200 && 
                jsonObject.has("response")) {
                JSONArray confirms = jsonObject.getJSONArray("response");

                String[] columnNames = {"Order ID", "Status", "Kode Booking"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);

                for (int i = 0; i < confirms.length(); i++) {
                    JSONObject confirm = confirms.getJSONObject(i);

                    int orderId = confirm.getInt("orderId");
                    String status = confirm.getString("status");
                    String kodeBooking = confirm.getString("kode_booking");

                    model.addRow(new Object[]{orderId, status, kodeBooking});
                }

                JTable table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);

                panel.add(scrollPane, BorderLayout.CENTER);

            } else {
                JOptionPane.showMessageDialog(panel, "Error: Data format is not as expected.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Error: Failed to fetch confirm details.");
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
