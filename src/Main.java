import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Appliquer le design natif de Windows/Mac si disponible
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) { 
            // Ignoré, on garde le design Java classique
        }

        // Lancement direct de la fenêtre logicielle en tant que programme principal
        java.awt.EventQueue.invokeLater(() -> {
            new FenetreTarification().setVisible(true);
        });
    }
}