import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ElectionAppGUI extends JFrame {
    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private static final int PORT = 9876;
    private static final int BUFFER_SIZE = 1024;
    private static final int MAX_VOTES = 5;

    private JTextField voteField;
    private JTextArea resultArea;
    private JButton voteButton;
    private JButton showWinnerButton; // Button to show winner
    private int totalVotes;
    private int voteCountA;
    private int voteCountB;

    public ElectionAppGUI() {
        setTitle("Election Vote Counting App");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());

        JLabel voteLabel = new JLabel("Enter the vote for Electorate 1 (A or B):");
        inputPanel.add(voteLabel);

        voteField = new JTextField(5);
        inputPanel.add(voteField);

        voteButton = new JButton("Cast Vote");
        voteButton.addActionListener(new VoteButtonListener());
        inputPanel.add(voteButton);

        showWinnerButton = new JButton("Show Winner"); // Button to show winner
        showWinnerButton.setEnabled(false); // Initially disabled
        showWinnerButton.addActionListener(new ShowWinnerButtonListener()); // Action listener for show winner button

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(showWinnerButton, BorderLayout.SOUTH); // Add show winner button to the south

        totalVotes = 0; // Initialize total votes
        voteCountA = 0;
        voteCountB = 0;

        setVisible(true);
    }

    private class VoteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (totalVotes < MAX_VOTES) {
                String inputVote = voteField.getText().toUpperCase().trim();
                if (inputVote.equals("A") || inputVote.equals("B")) {
                    totalVotes++; // Increment total votes
                    castVote(inputVote.charAt(0)); // Cast vote
                    updateVoteLabel(); // Update vote label for next electorate
                    if (totalVotes == MAX_VOTES) {
                        showWinnerButton.setEnabled(true); // Enable show winner button after all votes are cast
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid vote! Enter A or B.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Maximum votes reached!");
            }
        }
    }

    private void castVote(char vote) {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            byte[] buffer = (vote + "").getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);

            resultArea.append("You cast vote: " + vote + "\n");

            buffer = new byte[BUFFER_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String receivedVote = new String(packet.getData(), 0, packet.getLength());

            if (receivedVote.equals("A")) {
                voteCountA++;
            } else if (receivedVote.equals("B")) {
                voteCountB++;
            }

            resultArea.append("Received vote from other electorates: " + receivedVote + "\n");
            resultArea.append("Vote count for A: " + voteCountA + "\n");
            resultArea.append("Vote count for B: " + voteCountB + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void updateVoteLabel() {
        int nextElectorate = totalVotes + 1;
        String labelText = "Enter the vote for Electorate " + nextElectorate + " (A or B):";
        voteField.setText(""); // Clear the vote field for the next input
        voteField.requestFocus(); // Set focus on the vote field
        JLabel voteLabel = (JLabel) ((JPanel) getContentPane().getComponent(0)).getComponent(0); // Get the vote label
        voteLabel.setText(labelText); // Update the vote label
    }

    private class ShowWinnerButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showElectorateWinners(); // Invoke method to show electorate winners
        }
    }

    private void showElectorateWinners() {
        resultArea.append("Winners by Electorate:\n");
        for (int i = 1; i <= MAX_VOTES; i++) {
            char winner = (i % 2 == 0) ? 'A' : 'B'; // Example logic to determine winner for each electorate
            resultArea.append("Electorate " + i + " Winner: Candidate " + winner + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ElectionAppGUI());
    }
}
