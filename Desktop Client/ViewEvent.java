import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class ViewEvent {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ViewEvent::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Detail Konser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel viewEventPanel = new JPanel(new BorderLayout());
        placeViewEventComponents(viewEventPanel);

        mainPanel.add(viewEventPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void placeViewEventComponents(JPanel panel) {
        panel.removeAll();

        String url = "http://localhost:8000/api/pat/event/desktop";
        try {
            String jsonData = getJSONData(url);
            System.out.println("Response JSON: " + jsonData);

            JSONObject jsonObject = new JSONObject(jsonData);
            if (jsonObject.has("status") && jsonObject.getInt("status") == 200 &&
                jsonObject.has("response")) {
                JSONObject response = jsonObject.getJSONObject("response");

                if (response.has("events")) {
                    JSONArray events = response.getJSONArray("events");

                    if (events.length() > 0) {
                        String[] columnNames = {"ID", "Nama Konser", "Artist", "Jumlah Tiket", "Lokasi", "Tanggal", "Kategori Tiket", "Harga", "Jumlah", "Bank", "Kode", "Update", "Delete"};
                        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                            @Override
                            public Class<?> getColumnClass(int columnIndex) {
                                if (columnIndex == getColumnCount() - 2 || columnIndex == getColumnCount() - 1) {
                                    return JButton.class;
                                }
                                return super.getColumnClass(columnIndex);
                            }

                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return column == getColumnCount() - 2 || column == getColumnCount() - 1;
                            }
                        };

                        for (int e = 0; e < events.length(); e++) {
                            JSONObject event = events.getJSONObject(e);

                            int id = event.getInt("id");
                            String namaKonser = event.getString("nama_konser");
                            String artist = event.getString("artist");
                            int jumlahTiket = event.getInt("jumlah_tiket");
                            String lokasi = event.getString("lokasi");
                            String tanggal = event.getString("tanggal");

                            JSONArray kategoriTiketArray = event.getJSONArray("kategori_tiket");
                            JSONArray metodePembayaranArray = event.getJSONArray("metode_pembayaran");

                            int maxRows = Math.max(kategoriTiketArray.length(), metodePembayaranArray.length());

                            for (int i = 0; i < maxRows; i++) {
                                String kategoriNama = "";
                                int harga = 0;
                                int jumlah = 0;

                                if (i < kategoriTiketArray.length()) {
                                    JSONObject kategori = kategoriTiketArray.getJSONObject(i);
                                    kategoriNama = kategori.getString("kategori");
                                    harga = kategori.getInt("harga");
                                    jumlah = kategori.getInt("jumlah");
                                }

                                String bank = "";
                                int kode = 0;

                                if (i < metodePembayaranArray.length()) {
                                    JSONObject metode = metodePembayaranArray.getJSONObject(i);
                                    bank = metode.getString("bank");
                                    kode = metode.getInt("kode");
                                }

                                JButton updateButton = new JButton("Update");
                                updateButton.addActionListener(new UpdateActionListener(id, panel));

                                JButton deleteButton = new JButton("Delete");
                                deleteButton.addActionListener(new DeleteActionListener(id, panel));

                                Object[] rowData = {id, namaKonser, artist, jumlahTiket, lokasi, tanggal, kategoriNama, harga, jumlah, bank, kode, updateButton, deleteButton};
                                model.addRow(rowData);
                            }
                        }

                        JTable table = new JTable(model);
                        table.getColumnModel().getColumn(columnNames.length - 2).setCellRenderer(new ButtonRenderer());
                        table.getColumnModel().getColumn(columnNames.length - 2).setCellEditor(new ButtonEditor(new JCheckBox(), true));
                        table.getColumnModel().getColumn(columnNames.length - 1).setCellRenderer(new ButtonRenderer());
                        table.getColumnModel().getColumn(columnNames.length - 1).setCellEditor(new ButtonEditor(new JCheckBox(), false));

                        JScrollPane scrollPane = new JScrollPane(table);
                        panel.add(scrollPane, BorderLayout.CENTER);

                    } else {
                        JOptionPane.showMessageDialog(panel, "Error: No events found in response.");
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "Error: 'events' key not found in response.");
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Error: Data format is not as expected.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Error: Failed to fetch event details.");
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

    private static class UpdateActionListener implements ActionListener {
        private int eventId;
        private JPanel panel;
    
        public UpdateActionListener(int eventId, JPanel panel) {
            this.eventId = eventId;
            this.panel = panel;
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            JDialog dialog = new JDialog((Frame) null, "Update Event", true);
            dialog.setSize(600, 500);
            dialog.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5); // Adding padding
    
            JTextField namaKonserField = new JTextField(20);
            JTextField artistField = new JTextField(20);
            JTextArea deskripsiField = new JTextArea(5, 20);
            JPanel kategoriPanel = new JPanel(new GridBagLayout()); // Panel for kategori_tiket
            JPanel metodePanel = new JPanel(new GridBagLayout()); // Panel for metode_pembayaran
            JTextField lokasiField = new JTextField(20);
            JTextField tanggalField = new JTextField(20);
    
            int y = 0;
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            gbc.anchor = GridBagConstraints.WEST;
            dialog.add(new JLabel("Nama Konser:"), gbc);
            gbc.gridx = 1;
            dialog.add(namaKonserField, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            dialog.add(new JLabel("Artist:"), gbc);
            gbc.gridx = 1;
            dialog.add(artistField, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            dialog.add(new JLabel("Deskripsi:"), gbc);
            gbc.gridx = 1;
            dialog.add(new JScrollPane(deskripsiField), gbc);
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            dialog.add(new JLabel("Kategori Tiket:"), gbc);
            gbc.gridx = 1;
            JButton addKategoriButton = new JButton("Add Kategori");
            dialog.add(addKategoriButton, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            gbc.gridwidth = 2;
            dialog.add(new JScrollPane(kategoriPanel), gbc);
    
            addKategoriButton.addActionListener(e1 -> {
                GridBagConstraints kgbc = new GridBagConstraints();
                kgbc.insets = new Insets(5, 5, 5, 5); // Adding padding
                kgbc.gridx = 0;
                kgbc.gridy = kategoriPanel.getComponentCount() / 4; // Assuming 4 components per row
                kgbc.anchor = GridBagConstraints.WEST;
    
                kategoriPanel.add(new JLabel("Kategori:"), kgbc);
                kgbc.gridx++;
                JTextField kategoriField = new JTextField(10);
                kategoriPanel.add(kategoriField, kgbc);
                kgbc.gridx++;
                kategoriPanel.add(new JLabel("Harga:"), kgbc);
                kgbc.gridx++;
                JTextField hargaField = new JTextField(10);
                kategoriPanel.add(hargaField, kgbc);
                kgbc.gridx++;
                kategoriPanel.add(new JLabel("Jumlah:"), kgbc);
                kgbc.gridx++;
                JTextField jumlahField = new JTextField(10);
                kategoriPanel.add(jumlahField, kgbc);
    
                dialog.pack(); // Adjust dialog size after adding components
            });
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            gbc.gridwidth = 1;
            dialog.add(new JLabel("Metode Pembayaran:"), gbc);
            gbc.gridx = 1;
            JButton addMetodeButton = new JButton("Add Metode");
            dialog.add(addMetodeButton, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            gbc.gridwidth = 2;
            dialog.add(new JScrollPane(metodePanel), gbc);
    
            addMetodeButton.addActionListener(e1 -> {
                GridBagConstraints mgbc = new GridBagConstraints();
                mgbc.insets = new Insets(5, 5, 5, 5); // Adding padding
                mgbc.gridx = 0;
                mgbc.gridy = metodePanel.getComponentCount() / 2; // Assuming 2 components per row
                mgbc.anchor = GridBagConstraints.WEST;
    
                metodePanel.add(new JLabel("Bank:"), mgbc);
                mgbc.gridx++;
                JTextField bankField = new JTextField(10);
                metodePanel.add(bankField, mgbc);
                mgbc.gridx++;
                metodePanel.add(new JLabel("Kode:"), mgbc);
                mgbc.gridx++;
                JTextField kodeField = new JTextField(10);
                metodePanel.add(kodeField, mgbc);
    
                dialog.pack(); // Adjust dialog size after adding components
            });
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            gbc.gridwidth = 1;
            dialog.add(new JLabel("Lokasi:"), gbc);
            gbc.gridx = 1;
            dialog.add(lokasiField, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = y++;
            dialog.add(new JLabel("Tanggal:"), gbc);
            gbc.gridx = 1;
            dialog.add(tanggalField, gbc);
    
            gbc.gridx = 1;
            gbc.gridy = y++;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JButton submitButton = new JButton("Submit");
            dialog.add(submitButton, gbc);
    
            submitButton.addActionListener(e1 -> {
                String updateUrl = "http://localhost:8000/api/pat/event/desktop/" + eventId;
                try {
                    URL url = new URL(updateUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
    
                    JSONObject eventData = new JSONObject();
                    eventData.put("nama_konser", namaKonserField.getText());
                    eventData.put("artist", artistField.getText());
                    eventData.put("deskripsi", deskripsiField.getText());
                    eventData.put("lokasi", lokasiField.getText());
                    eventData.put("tanggal", tanggalField.getText());
    
                    JSONArray kategoriArray = new JSONArray();
                    for (int i = 0; i < kategoriPanel.getComponentCount(); i += 6) { // Adjusted for labels
                        JTextField kategoriField = (JTextField) kategoriPanel.getComponent(i + 1);
                        JTextField hargaField = (JTextField) kategoriPanel.getComponent(i + 3);
                        JTextField jumlahField = (JTextField) kategoriPanel.getComponent(i + 5);
                        JSONObject kategoriObject = new JSONObject();
                        kategoriObject.put("kategori", kategoriField.getText());
                        kategoriObject.put("harga", Integer.parseInt(hargaField.getText()));
                        kategoriObject.put("jumlah", Integer.parseInt(jumlahField.getText()));
                        kategoriArray.put(kategoriObject);
                    }
                    eventData.put("kategori_tiket", kategoriArray);
    
                    JSONArray metodeArray = new JSONArray();
                    for (int i = 0; i < metodePanel.getComponentCount(); i += 4) { // Adjusted for labels
                        JTextField bankField = (JTextField) metodePanel.getComponent(i + 1);
                        JTextField kodeField = (JTextField) metodePanel.getComponent(i + 3);
                        JSONObject metodeObject = new JSONObject();
                        metodeObject.put("bank", bankField.getText());
                        metodeObject.put("kode", kodeField.getText());
                        metodeArray.put(metodeObject);
                    }
                    eventData.put("metode_pembayaran", metodeArray);
    
                    OutputStream os = conn.getOutputStream();
                    os.write(eventData.toString().getBytes());
                    os.flush();
    
                    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                    }
    
                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }
    
                    conn.disconnect();
    
                    JOptionPane.showMessageDialog(dialog, "Event updated successfully!");
                    dialog.dispose();
                    placeViewEventComponents((JPanel) SwingUtilities.getRoot((Component) e.getSource())); // Refresh the table after update
    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Error: Failed to update event.");
                }
            });
    
            dialog.setVisible(true);
        }
    }
    

    private static class DeleteActionListener implements ActionListener {
        private int eventId;
        private JPanel panel;

        public DeleteActionListener(int eventId, JPanel panel) {
            this.eventId = eventId;
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this event?", "Delete Event", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String deleteUrl = "http://localhost:8000/api/pat/event/desktop/" + eventId;
                try {
                    URL url = new URL(deleteUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("DELETE");

                    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    conn.disconnect();

                    JOptionPane.showMessageDialog(null, "Event deleted successfully!");
                    placeViewEventComponents(panel); // Refresh the table after delete

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error: Failed to delete event.");
                }
            }
        }
    }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof JButton) {
                return (JButton) value;
            } else {
                setText((value == null) ? "" : value.toString());
                return this;
            }
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isUpdate;

        public ButtonEditor(JCheckBox checkBox, boolean isUpdate) {
            super(checkBox);
            this.isUpdate = isUpdate;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof JButton) {
                button = (JButton) value;
            } else {
                label = (value == null) ? "" : value.toString();
                button.setText(label);
            }
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button;
        }

        @Override
        public boolean stopCellEditing() {
            if (isUpdate) {
                button.doClick();
            } else {
                button.doClick();
            }
            return super.stopCellEditing();
        }
    }
}
