import javax.swing.SwingUtilities;

/** Entry point for the decentralised voting application. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VotingGUI().setVisible(true));
    }
}
