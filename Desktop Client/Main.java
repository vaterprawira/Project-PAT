import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {

    public Main() {
        setTitle("Main Page");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(6, 1)); // Tambahkan satu baris lagi untuk tombol baru

        JButton createEventButton = new JButton("Create Event");
        JButton viewEventButton = new JButton("View Event");
        JButton viewOrderButton = new JButton("View Order");
        JButton viewPaymentButton = new JButton("View Payment");
        // JButton createConfirmButton = new JButton("Create Confirm");
        JButton viewConfirmButton = new JButton("View Confirm"); // Tambahkan tombol View Confirm

        createEventButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateEvent();
            }
        });

        viewEventButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ViewEvent.main(new String[]{});
            }
        });

        viewOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ViewOrder.main(new String[]{});
            }
        });

        viewPaymentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ViewPayment.main(new String[]{});
            }
        });

        // createConfirmButton.addActionListener(new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         CreateConfirm.main(new String[]{});
        //     }
        // });

        viewConfirmButton.addActionListener(new ActionListener() { // Tambahkan listener untuk tombol View Confirm
            @Override
            public void actionPerformed(ActionEvent e) {
                ViewConfirm.main(new String[]{});
            }
        });

        mainPanel.add(createEventButton);
        mainPanel.add(viewEventButton);
        mainPanel.add(viewOrderButton);
        mainPanel.add(viewPaymentButton);
        // mainPanel.add(createConfirmButton);
        mainPanel.add(viewConfirmButton); // Tambahkan tombol ke panel

        add(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }
}
