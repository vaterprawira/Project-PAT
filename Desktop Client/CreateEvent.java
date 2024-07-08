import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CreateEvent extends JFrame {
    public CreateEvent() {
        setTitle("Create Event");
        setSize(800, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel createEventPanel = new JPanel(new GridLayout(0, 1));
        placeCreateEventComponents(createEventPanel);
        add(createEventPanel);

        setVisible(true);
    }

    private void placeCreateEventComponents(JPanel panel) {
        panel.setLayout(new GridLayout(0, 1));

        panel.add(new JLabel("Nama Konser:"));
        JTextField namaKonserText = new JTextField(20);
        panel.add(namaKonserText);

        panel.add(new JLabel("Artis:"));
        JTextField artisText = new JTextField(20);
        panel.add(artisText);

        panel.add(new JLabel("Deskripsi:"));
        JTextArea deskripsiText = new JTextArea(2, 20);
        deskripsiText.setLineWrap(true);
        deskripsiText.setWrapStyleWord(true);
        panel.add(new JScrollPane(deskripsiText));

        // Kategori Tiket
        panel.add(new JLabel("Kategori Tiket:"));
        List<JPanel> kategoriPanels = new ArrayList<>();
        JPanel kategoriContainer = new JPanel();
        kategoriContainer.setLayout(new BoxLayout(kategoriContainer, BoxLayout.Y_AXIS));
        panel.add(kategoriContainer);
        panel.add(new JScrollPane(kategoriContainer));
        JButton addKategoriButton = new JButton("Add Kategori Tiket");
        panel.add(addKategoriButton);

        addKategoriButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel kategoriPanel = new JPanel(new GridLayout(1, 5));
                kategoriPanel.add(new JLabel("Kategori:"));
                JTextField kategoriTiketText = new JTextField(10);
                kategoriPanel.add(kategoriTiketText);
                kategoriPanel.add(new JLabel("Harga:"));
                JTextField hargaText = new JTextField(10);
                kategoriPanel.add(hargaText);
                kategoriPanel.add(new JLabel("Jumlah:"));
                JTextField jumlahTiketText = new JTextField(10);
                kategoriPanel.add(jumlahTiketText);

                JButton removeButton = new JButton("Remove");
                kategoriPanel.add(removeButton);

                removeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        kategoriContainer.remove(kategoriPanel);
                        kategoriPanels.remove(kategoriPanel);
                        kategoriContainer.revalidate();
                        kategoriContainer.repaint();
                    }
                });

                kategoriPanels.add(kategoriPanel);
                kategoriContainer.add(kategoriPanel);
                kategoriContainer.revalidate();
                kategoriContainer.repaint();
            }
        });

        // Lokasi
        panel.add(new JLabel("Lokasi:"));
        JTextField lokasiText = new JTextField(20);
        panel.add(lokasiText);

        // Tanggal
        panel.add(new JLabel("Tanggal:"));
        JTextField tanggalText = new JTextField(20);
        panel.add(tanggalText);

        // Metode Pembayaran
        panel.add(new JLabel("Metode Pembayaran:"));
        List<JPanel> pembayaranPanels = new ArrayList<>();
        JPanel pembayaranContainer = new JPanel();
        pembayaranContainer.setLayout(new BoxLayout(pembayaranContainer, BoxLayout.Y_AXIS));
        panel.add(pembayaranContainer);
        panel.add(new JScrollPane(pembayaranContainer));
        JButton addPembayaranButton = new JButton("Add Metode Pembayaran");
        panel.add(addPembayaranButton);

        addPembayaranButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel pembayaranPanel = new JPanel(new GridLayout(1, 4));
                pembayaranPanel.add(new JLabel("Bank:"));
                JTextField bankText = new JTextField(10);
                pembayaranPanel.add(bankText);
                pembayaranPanel.add(new JLabel("Kode:"));
                JTextField kodeText = new JTextField(10);
                pembayaranPanel.add(kodeText);

                JButton removeButton = new JButton("Remove");
                pembayaranPanel.add(removeButton);

                removeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        pembayaranContainer.remove(pembayaranPanel);
                        pembayaranPanels.remove(pembayaranPanel);
                        pembayaranContainer.revalidate();
                        pembayaranContainer.repaint();
                    }
                });

                pembayaranPanels.add(pembayaranPanel);
                pembayaranContainer.add(pembayaranPanel);
                pembayaranContainer.revalidate();
                pembayaranContainer.repaint();
            }
        });

        // Submit Button
        JButton submitButton = new JButton("Create Event");
        panel.add(submitButton);

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String namaKonser = namaKonserText.getText();
                String artis = artisText.getText();
                String deskripsi = deskripsiText.getText();

                // Convert kategoriPanels to JSON
                List<String> kategoriList = new ArrayList<>();
                int totalJumlahTiket = 0;
                for (JPanel kategoriPanel : kategoriPanels) {
                    Component[] components = kategoriPanel.getComponents();
                    String kategori = ((JTextField) components[1]).getText();
                    String harga = ((JTextField) components[3]).getText();
                    String jumlah = ((JTextField) components[5]).getText();
                    totalJumlahTiket += Integer.parseInt(jumlah);
                    kategoriList.add(String.format("{\"kategori\":\"%s\", \"harga\":%s, \"jumlah\":%s}", kategori, harga, jumlah));
                }
                String kategori = "[" + String.join(", ", kategoriList) + "]";

                String lokasi = lokasiText.getText();
                String tanggal = tanggalText.getText();

                // Convert pembayaranPanels to JSON
                List<String> pembayaranList = new ArrayList<>();
                for (JPanel pembayaranPanel : pembayaranPanels) {
                    Component[] components = pembayaranPanel.getComponents();
                    String bank = ((JTextField) components[1]).getText();
                    String kode = ((JTextField) components[3]).getText();
                    pembayaranList.add(String.format("{\"bank\":\"%s\", \"kode\":\"%s\"}", bank, kode));
                }
                String pembayaran = "[" + String.join(", ", pembayaranList) + "]";

                if (namaKonser.isEmpty() || deskripsi.isEmpty() || kategori.isEmpty() || lokasi.isEmpty() || tanggal.isEmpty() || pembayaran.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Please fill in all fields.");
                    return;
                }

                try {
                    URL url = new URL("http://localhost:8000/api/pat/event");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    String jsonInputString = String.format(
                            "{\"nama_konser\": \"%s\", \"artis\": \"%s\", \"deskripsi\": \"%s\", \"kategori_tiket\": %s, " +
                                    "\"jumlah_tiket\": \"%d\", \"lokasi\": \"%s\", \"tanggal\": \"%s\", \"metode_pembayaran\": %s}",
                            namaKonser, artis, deskripsi, kategori, totalJumlahTiket, lokasi, tanggal, pembayaran);

                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(panel, "Event created successfully.");
                    } else {
                        JOptionPane.showMessageDialog(panel, "Failed to create event. Please try again.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {
        new CreateEvent();
    }
}
