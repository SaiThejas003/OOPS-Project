import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Modern-styled Swing front end for the voting blockchain. */
public class VotingGUI extends JFrame {
    // ---- theme ----
    static final Color BG = new Color(0xF3F4F6);
    static final Color CARD = Color.WHITE;
    static final Color SIDEBAR = new Color(0x111827);
    static final Color ACCENT = new Color(0x4F46E5);
    static final Color ACCENT_DARK = new Color(0x4338CA);
    static final Color TEXT = new Color(0x111827);
    static final Color MUTED = new Color(0x6B7280);
    static final Color OK = new Color(0x16A34A);
    static final Color BAD = new Color(0xDC2626);
    static final Color BORDER = new Color(0xE5E7EB);

    static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    static JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static JTextField field() {
        JTextField f = new JTextField();
        f.setFont(font(Font.PLAIN, 15));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return f;
    }

    // ---- custom components ----
    static class RoundedButton extends JButton {
        private final Color base, hover;
        private boolean over;

        RoundedButton(String text, Color base, Color hover, Color fg) {
            super(text);
            this.base = base;
            this.hover = hover;
            setForeground(fg);
            setFont(font(Font.BOLD, 14));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(11, 24, 11, 24));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(over ? hover : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class NavButton extends JToggleButton {
        private boolean over;

        NavButton(String text) {
            super(text);
            setFont(font(Font.PLAIN, 15));
            setForeground(new Color(0xD1D5DB));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            addItemListener(e -> setForeground(isSelected() ? Color.WHITE : new Color(0xD1D5DB)));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected()) {
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            } else if (over) {
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class ChoiceButton extends JToggleButton {
        ChoiceButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 15));
            setForeground(TEXT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(14, 26, 14, 26));
            addItemListener(e -> setForeground(isSelected() ? Color.WHITE : TEXT));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected() ? ACCENT : BORDER);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class Card extends JPanel {
        Card() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class BarPanel extends JPanel {
        private Map<String, Integer> data = new LinkedHashMap<>();

        BarPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(280, 200));
        }

        void set(Map<String, Integer> d) {
            data = d;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int total = 0, max = 1;
            for (int v : data.values()) {
                total += v;
                max = Math.max(max, v);
            }
            int w = getWidth(), y = 0;
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int v = e.getValue();
                g2.setFont(font(Font.BOLD, 14));
                g2.setColor(TEXT);
                g2.drawString(e.getKey(), 0, y + 16);
                String right = v + "  (" + (total == 0 ? 0 : v * 100 / total) + "%)";
                g2.setFont(font(Font.PLAIN, 13));
                g2.setColor(MUTED);
                g2.drawString(right, w - g2.getFontMetrics().stringWidth(right), y + 16);
                g2.setColor(BORDER);
                g2.fillRoundRect(0, y + 24, w, 12, 12, 12);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, y + 24, v == 0 ? 0 : Math.max(12, w * v / max), 12, 12, 12);
                y += 56;
            }
            g2.dispose();
        }
    }

    // ---- app state ----
    private static final String[] CANDIDATES = {"Alice", "Bob", "Charlie"};

    private final Blockchain bc = new Blockchain(new LinkedHashSet<>(List.of(CANDIDATES)));
    private final Map<String, Voter> voters = new LinkedHashMap<>();
    private final DefaultListModel<String> voterListModel = new DefaultListModel<>();

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    private final JTextField voteIdField = field();
    private final JLabel voterInfo = label(" ", Font.PLAIN, 13, MUTED);
    private String selectedCandidate = CANDIDATES[0];

    private final JLabel votesValue = label("0", Font.BOLD, 30, TEXT);
    private final JLabel statusValue = label("Valid", Font.BOLD, 30, OK);
    private final BarPanel barPanel = new BarPanel();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"#", "Voter ID", "Candidate", "Hash", "Prev hash"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private String tamperedOriginal = null;

    public VotingGUI() {
        super("VoteChain - Decentralised Voting");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        content.setBackground(BG);
        content.add(buildRegisterPage(), "register");
        content.add(buildVotePage(), "vote");
        content.add(buildResultsPage(), "results");

        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        setSize(1100, 700);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        refresh();
    }

    // ---- layout helpers ----
    private JPanel buildSidebar() {
        JPanel s = new JPanel();
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBackground(SIDEBAR);
        s.setBorder(BorderFactory.createEmptyBorder(30, 16, 24, 16));
        s.setPreferredSize(new Dimension(230, 0));

        s.add(left(label("VoteChain", Font.BOLD, 24, Color.WHITE)));
        s.add(Box.createVerticalStrut(30));

        ButtonGroup group = new ButtonGroup();
        String[][] items = {{"Register", "register"}, {"Vote", "vote"}, {"Results & Ledger", "results"}};
        for (String[] item : items) {
            NavButton b = left(new NavButton(item[0]));
            group.add(b);
            b.addActionListener(e -> {
                cards.show(content, item[1]);
                if (item[1].equals("results")) refresh();
            });
            if (item[1].equals("register")) b.setSelected(true);
            s.add(b);
            s.add(Box.createVerticalStrut(6));
        }

        s.add(Box.createVerticalGlue());
        s.add(left(label("Hash-chained ledger", Font.PLAIN, 12, new Color(0x9CA3AF))));
        return s;
    }

    private JPanel page(String title, String subtitle, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(0, 22));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));
        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        head.add(left(label(title, Font.BOLD, 26, TEXT)));
        head.add(Box.createVerticalStrut(4));
        head.add(left(label(subtitle, Font.PLAIN, 14, MUTED)));
        p.add(head, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel top(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.NORTH);
        return p;
    }

    private Card verticalCard() {
        Card c = new Card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        return c;
    }

    private static RoundedButton primary(String text) {
        return new RoundedButton(text, ACCENT, ACCENT_DARK, Color.WHITE);
    }

    private static void small(JButton b) {
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    private static void feedback(JLabel l, String text, boolean ok) {
        l.setForeground(ok ? OK : BAD);
        l.setText("<html><body style='width:300px'>" + esc(text) + "</body></html>");
    }

    // ---- pages ----
    private JPanel buildRegisterPage() {
        JTextField nameField = field();
        JTextField idField = field();
        JLabel fb = label(" ", Font.PLAIN, 13, MUTED);
        RoundedButton btn = primary("Register voter");

        btn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String id = idField.getText().trim();
            if (name.isEmpty() || id.isEmpty()) {
                feedback(fb, "Please enter both a name and a voter ID.", false);
                return;
            }
            if (voters.containsKey(id)) {
                feedback(fb, "Voter ID '" + id + "' is already taken. Choose another.", false);
                return;
            }
            try {
                Voter v = new Voter(id, name); // generates this voter's key pair
                bc.register(id, v.publicKey);
                voters.put(id, v);
                voterListModel.addElement(name + "   |   " + id);
                nameField.setText("");
                idField.setText("");
                feedback(fb, "Registered " + name + " with ID " + id + ".", true);
            } catch (Exception ex) {
                feedback(fb, "Error: " + ex.getMessage(), false);
            }
        });

        Card form = verticalCard();
        form.add(left(label("New voter", Font.BOLD, 18, TEXT)));
        form.add(Box.createVerticalStrut(18));
        form.add(left(label("Full name", Font.PLAIN, 13, MUTED)));
        form.add(Box.createVerticalStrut(6));
        form.add(left(nameField));
        form.add(Box.createVerticalStrut(16));
        form.add(left(label("Voter ID (any unique ID you like)", Font.PLAIN, 13, MUTED)));
        form.add(Box.createVerticalStrut(6));
        form.add(left(idField));
        form.add(Box.createVerticalStrut(22));
        form.add(left(btn));
        form.add(Box.createVerticalStrut(14));
        form.add(left(fb));

        JList<String> list = new JList<>(voterListModel);
        list.setFont(font(Font.PLAIN, 14));
        list.setFixedCellHeight(34);
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);

        Card listCard = new Card();
        listCard.setLayout(new BorderLayout(0, 14));
        listCard.add(label("Registered voters", Font.BOLD, 18, TEXT), BorderLayout.NORTH);
        listCard.add(sp, BorderLayout.CENTER);

        JPanel body = new JPanel(new GridLayout(1, 2, 18, 0));
        body.setOpaque(false);
        body.add(top(form));
        body.add(listCard);
        return page("Register", "Create a voter with your own name and ID.", body);
    }

    private JPanel buildVotePage() {
        JLabel fb = label(" ", Font.PLAIN, 13, MUTED);
        RoundedButton cast = primary("Cast vote");

        voteIdField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateVoterInfo(); }
            @Override public void removeUpdate(DocumentEvent e) { updateVoterInfo(); }
            @Override public void changedUpdate(DocumentEvent e) { updateVoterInfo(); }
        });

        JPanel choices = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        choices.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (String c : CANDIDATES) {
            ChoiceButton cb = new ChoiceButton(c);
            cb.addActionListener(e -> selectedCandidate = c);
            group.add(cb);
            choices.add(cb);
            if (c.equals(selectedCandidate)) cb.setSelected(true);
        }

        cast.addActionListener(e -> {
            String id = voteIdField.getText().trim();
            Voter v = voters.get(id);
            if (v == null) {
                feedback(fb, "No voter found with this ID. Register first.", false);
                return;
            }
            try {
                boolean ok = bc.castVote(id, selectedCandidate, v.sign(selectedCandidate));
                if (ok) {
                    feedback(fb, "Thank you, " + v.name + "! Your vote for " + selectedCandidate + " was recorded.", true);
                    voteIdField.setText("");
                } else {
                    feedback(fb, "Vote rejected: " + bc.getLastError(), false);
                }
            } catch (Exception ex) {
                feedback(fb, "Error: " + ex.getMessage(), false);
            }
        });

        Card form = verticalCard();
        form.add(left(label("Your ballot", Font.BOLD, 18, TEXT)));
        form.add(Box.createVerticalStrut(18));
        form.add(left(label("Your voter ID", Font.PLAIN, 13, MUTED)));
        form.add(Box.createVerticalStrut(6));
        form.add(left(voteIdField));
        form.add(Box.createVerticalStrut(6));
        form.add(left(voterInfo));
        form.add(Box.createVerticalStrut(18));
        form.add(left(label("Choose a candidate", Font.PLAIN, 13, MUTED)));
        form.add(Box.createVerticalStrut(8));
        form.add(left(choices));
        form.add(Box.createVerticalStrut(24));
        form.add(left(cast));
        form.add(Box.createVerticalStrut(14));
        form.add(left(fb));

        JLabel info = new JLabel("<html><body style='width:280px'>"
                + "<b>How your vote is protected</b><br><br>"
                + "Each voter gets a private key at registration. Your vote is signed with it, "
                + "so nobody can vote on your behalf.<br><br>"
                + "Votes are stored in a chain of blocks. Every block holds the hash of the one before it, "
                + "so changing a past vote breaks the chain.<br><br>"
                + "Each voter ID can vote only once.</body></html>");
        info.setFont(font(Font.PLAIN, 14));
        info.setForeground(MUTED);
        Card infoCard = new Card();
        infoCard.setLayout(new BorderLayout());
        infoCard.add(info, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 18, 0));
        body.setOpaque(false);
        body.add(top(form));
        body.add(top(infoCard));
        return page("Vote", "Enter your voter ID and pick a candidate.", body);
    }

    private JPanel buildResultsPage() {
        Card votesCard = verticalCard();
        votesCard.add(left(votesValue));
        votesCard.add(left(label("Votes cast", Font.PLAIN, 13, MUTED)));
        Card statusCard = verticalCard();
        statusCard.add(left(statusValue));
        statusCard.add(left(label("Chain integrity", Font.PLAIN, 13, MUTED)));

        JPanel stats = new JPanel(new GridLayout(1, 2, 18, 0));
        stats.setOpaque(false);
        stats.add(votesCard);
        stats.add(statusCard);

        Card tallyCard = new Card();
        tallyCard.setLayout(new BorderLayout(0, 16));
        tallyCard.setPreferredSize(new Dimension(340, 0));
        tallyCard.add(label("Live tally", Font.BOLD, 18, TEXT), BorderLayout.NORTH);
        tallyCard.add(barPanel, BorderLayout.CENTER);

        RoundedButton refreshBtn = new RoundedButton("Refresh", BORDER, new Color(0xD1D5DB), TEXT);
        RoundedButton tamperBtn = new RoundedButton("Tamper / Restore block 1",
                new Color(0xFEE2E2), new Color(0xFECACA), BAD);
        small(refreshBtn);
        small(tamperBtn);
        refreshBtn.addActionListener(e -> refresh());
        tamperBtn.addActionListener(e -> toggleTamper());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(refreshBtn);
        buttons.add(tamperBtn);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(label("Ledger", Font.BOLD, 18, TEXT), BorderLayout.WEST);
        titleRow.add(buttons, BorderLayout.EAST);

        JTable table = new JTable(tableModel);
        table.setFont(font(Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xEEF2FF));
        table.setSelectionForeground(TEXT);
        table.getColumnModel().getColumn(0).setPreferredWidth(36);
        JTableHeader header = table.getTableHeader();
        header.setFont(font(Font.BOLD, 12));
        header.setForeground(MUTED);
        header.setBackground(CARD);
        header.setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD);

        Card ledgerCard = new Card();
        ledgerCard.setLayout(new BorderLayout(0, 14));
        ledgerCard.add(titleRow, BorderLayout.NORTH);
        ledgerCard.add(sp, BorderLayout.CENTER);

        JPanel lower = new JPanel(new BorderLayout(18, 0));
        lower.setOpaque(false);
        lower.add(tallyCard, BorderLayout.WEST);
        lower.add(ledgerCard, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.add(stats, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);
        return page("Results & Ledger", "Live counts and the tamper-evident chain of votes.", body);
    }

    // ---- behaviour ----
    private void updateVoterInfo() {
        String id = voteIdField.getText().trim();
        if (id.isEmpty()) {
            voterInfo.setText(" ");
            return;
        }
        Voter v = voters.get(id);
        if (v == null) {
            voterInfo.setForeground(BAD);
            voterInfo.setText("No voter found with this ID");
        } else {
            voterInfo.setForeground(OK);
            voterInfo.setText("Voting as " + v.name);
        }
    }

    /** Edits an already-recorded vote to show the chain detects it. Click again to undo. */
    private void toggleTamper() {
        List<Block> chain = bc.getChain();
        if (chain.size() < 2) {
            JOptionPane.showMessageDialog(this, "Cast at least one vote first.");
            return;
        }
        Block b = chain.get(1);
        if (tamperedOriginal == null) {
            tamperedOriginal = b.candidate;
            b.candidate = b.candidate.equals("Alice") ? "Bob" : "Alice";
        } else {
            b.candidate = tamperedOriginal;
            tamperedOriginal = null;
        }
        refresh();
    }

    private static String shortHash(String s) {
        return s.length() > 12 ? s.substring(0, 12) + "..." : s;
    }

    private void refresh() {
        barPanel.set(bc.tally());
        votesValue.setText(String.valueOf(bc.getChain().size() - 1));

        boolean valid = bc.isValid();
        statusValue.setText(valid ? "Valid" : "Tampered");
        statusValue.setForeground(valid ? OK : BAD);

        tableModel.setRowCount(0);
        for (Block b : bc.getChain()) {
            tableModel.addRow(new Object[]{b.index, b.voterId, b.candidate,
                    shortHash(b.hash), shortHash(b.prevHash)});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VotingGUI().setVisible(true));
    }
}
