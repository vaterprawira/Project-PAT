import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class ViewOrder {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ViewOrder::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Order");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel viewOrderPanel = new JPanel(new BorderLayout());
        placeViewOrderComponents(viewOrderPanel);

        mainPanel.add(viewOrderPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void placeViewOrderComponents(JPanel panel) {
        panel.removeAll();

        String url = "http://localhost:8000/api/pat/order";
        try {
            String jsonData = getJSONData(url);
            System.out.println("Response JSON: " + jsonData);
            JSONObject jsonObject = new JSONObject(jsonData);

            if (jsonObject.has("status") && jsonObject.getInt("status") == 200 && 
                jsonObject.has("response")) {
                JSONArray orders = jsonObject.getJSONArray("response");

                String[] columnNames = {"Order ID", "Event ID", "Kategori Tiket", "Total Harga"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);

                for (int i = 0; i < orders.length(); i++) {
                    JSONObject order = orders.getJSONObject(i);

                    int orderId = order.getInt("id");
                    int eventId = order.getInt("eventId");
                    String kategoriTiketStr = order.getString("kategori_tiket");
                    double totalHarga = order.getDouble("total_harga");

                    // Parse kategori_tiket from JSON string
                    JSONArray kategoriTiketArray = new JSONArray(kategoriTiketStr);
                    StringBuilder kategoriTiketBuilder = new StringBuilder();
                    for (int j = 0; j < kategoriTiketArray.length(); j++) {
                        JSONObject kategori = kategoriTiketArray.getJSONObject(j);
                        kategoriTiketBuilder.append("Kategori: ").append(kategori.getString("kategori"));
                        if (j < kategoriTiketArray.length() - 1) {
                            kategoriTiketBuilder.append("; ");
                        }
                    }
                    String kategoriTiket = kategoriTiketBuilder.toString();

                    model.addRow(new Object[]{orderId, eventId, kategoriTiket, totalHarga});
                }

                JTable table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);

                panel.add(scrollPane, BorderLayout.CENTER);

            } else {
                JOptionPane.showMessageDialog(panel, "Error: Data format is not as expected.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Error: Failed to fetch order details.");
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
