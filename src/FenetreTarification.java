import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class FenetreTarification extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalGainLabel;
    private List<Tarif> currentTarifs;

    public FenetreTarification() {
        setTitle("Mairie - Simulateur de Tarification Pole Enfance");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrer la fenêtre sur l'écran
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- PANNEAU DU HAUT (Bouton d'import) ---
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(240, 240, 245));
        JButton btnCharger = new JButton("Ouvrir fichier Excel Ciril...");
        btnCharger.setFont(new Font("Arial", Font.BOLD, 14));
        btnCharger.addActionListener(e -> ouvrirFichier());
        topPanel.add(btnCharger);

        // --- TABLEAU CENTRAL ---
        String[] colonnes = {"Tranche", "Usagers", "Prix Actuel", "Dépense Mairie", "Recette Mairie", "Écart (Subvention)", "Couverture"};
        tableModel = new DefaultTableModel(colonnes, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // --- PANNEAU DU BAS (Simulation) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Moteur de Simulation Tarifaire"));
        
        JLabel lblInfo = new JLabel("Ajustement de prix (ex: 0.15) :");
        JTextField txtAjustement = new JTextField(5);
        txtAjustement.setText("0.15");
        
        JButton btnSimuler = new JButton("Simuler Gain/Perte");
        btnSimuler.setBackground(new Color(0, 153, 76));
        btnSimuler.setForeground(Color.WHITE);
        
        totalGainLabel = new JLabel("Gain total estimé : --- €");
        totalGainLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalGainLabel.setForeground(Color.BLUE);

        // Action de simulation
        btnSimuler.addActionListener(e -> {
            try {
                double ajustement = Double.parseDouble(txtAjustement.getText().replace(",", "."));
                if (currentTarifs != null) {
                    double gainTotal = 0;
                    for (Tarif t : currentTarifs) {
                        if (t.getUsagers() == 0) continue;
                        double nouvPrix = t.getRepas() + ajustement;
                        double nouvRecette = Calculator.simulerNouvelleRecette(t, nouvPrix);
                        gainTotal += (nouvRecette - t.getRecettes());
                    }
                    totalGainLabel.setText(String.format("Gain estimé pour la ville 1ère année : %+.2f €", gainTotal));
                } else {
                    JOptionPane.showMessageDialog(this, "Veuillez d'abord charger un fichier Excel.");
                }
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide.");
            }
        });

        bottomPanel.add(lblInfo);
        bottomPanel.add(txtAjustement);
        bottomPanel.add(btnSimuler);
        bottomPanel.add(totalGainLabel);

        // --- ASSEMBLAGE DE LA FENETRE ---
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void ouvrirFichier() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("Donnees/Tableau-grille"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chargerDonnees(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chargerDonnees(String chemin) {
        try {
            // Sélection automatique de l'extracteur selon le nom du fichier
            IExtracteurCiril extracteur = choisirExtracteur(chemin);
            List<Tarif> tarifs = extracteur.chargerTarifs(chemin);

            if (tarifs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune donnée trouvée. Fichier invalide ou format non reconnu.");
                return;
            }
            this.currentTarifs = tarifs;
            tableModel.setRowCount(0); // Vider le tableau
            
            for (Tarif t : tarifs) {
                tableModel.addRow(new Object[]{
                    t.getTranche(), 
                    t.getUsagers(), 
                    String.format("%.2f €", t.getRepas()),
                    String.format("%.2f €", t.getDepenses()), 
                    String.format("%.2f €", t.getRecettes()),
                    String.format("%.2f €", Calculator.calculerEcart(t)), 
                    String.format("%.2f %%", Calculator.calculerTauxCouverture(t))
                });
            }
            totalGainLabel.setText("Données chargées depuis l'Excel. Prêt pour simulation.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur de lecture du fichier :\n" + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Choisit automatiquement l'extracteur Ciril adapté selon le nom du fichier.
     * Si le nom du fichier contient "loisirs" ou "periscolaire" → ExtractionLoisirs.
     * Sinon → ExtractionRestaurateur par défaut.
     */
    private IExtracteurCiril choisirExtracteur(String chemin) {
        String nomFichier = chemin.toLowerCase();
        if (nomFichier.contains("loisirs") || nomFichier.contains("periscolaire")) {
            return new ExtractionLoisirs();
        }
        // Par défaut : restauration (format Classeur1.xlsx de la mairie)
        return new ExtractionRestaurateur();
    }
}
