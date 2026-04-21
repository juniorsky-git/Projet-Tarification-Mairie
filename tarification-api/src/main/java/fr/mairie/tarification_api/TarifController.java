package fr.mairie.tarification_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TarifController {

    private List<Tarif> grilleCourante = DonneesTarifs.chargerTarifsReference();
    private String nomGrilleCourante = "Grille 2025 par défaut";

    @GetMapping("/grille-active")
    public ResponseEntity<?> getGrilleActive() {
        return ResponseEntity.ok(nomGrilleCourante);
    }

    @PostMapping("/reset-grille")
    public ResponseEntity<?> resetGrille() {
        this.grilleCourante = DonneesTarifs.chargerTarifsReference();
        this.nomGrilleCourante = "Grille 2025 par défaut";
        return ResponseEntity.ok("Retour à la grille 2025 par défaut.");
    }

    @GetMapping("/tarifs/complet")
    public ResponseEntity<List<Tarif>> getGrilleComplete() {
        return ResponseEntity.ok(this.grilleCourante);
    }

    @GetMapping("/tarif")
    public ResponseEntity<?> getTarif(@RequestParam double qf) {
        try {
            if (qf < 0) {
                return ResponseEntity.badRequest().body("Erreur : QF invalide.");
            }

            TarificationService service = new TarificationService();
            Tarif t = service.trouverTarif(qf, grilleCourante);

            TarifResponse r = new TarifResponse();
            r.tranche = t.getTranche();
            r.qfMin = t.getQfMin();
            r.qfMax = t.getQfMax();

            // Restauration
            r.repas = t.getRepas();

            // Accueil de loisirs
            r.accueilJournee = t.getAccueilJournee();
            r.accueilDemiRepas = t.getAccueilDemiRepas();

            // Périscolaire
            r.periscolaireMatinSoir = t.getPeriscolaireMatinSoir();
            r.periscolaireMatinOuSoir = t.getPeriscolaireMatinOuSoir();

            // Études surveillées
            r.etudesForfaitMensuel = t.getEtudesForfaitMensuel();
            r.etudesDemiForfait = t.getEtudesDemiForfait();
            r.tarifPostEtudes = t.getTarifPostEtudes();
            r.classeDecouverte = t.getClasseDecouverte();

            // Jeunesse / Espace ados
            r.adosVacJourneeRepas = t.getAdosVacJourneeRepas();
            r.adosVacJourneeSans = t.getAdosVacJourneeSans();
            r.adosVacDemiRepas = t.getAdosVacDemiRepas();
            r.adosVacDemiSans = t.getAdosVacDemiSans();
            r.adosSortieDemi = t.getAdosSortieDemi();
            r.adosSortieJournee = t.getAdosSortieJournee();

            // Séjours
            r.sejour5Jours = t.getSejour5Jours();
            r.sejour6Jours = t.getSejour6Jours();

            return ResponseEntity.ok(r);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return "Erreur : fichier vide.";
            }

            String nom = file.getOriginalFilename();
            if (nom == null || (!nom.toLowerCase().endsWith(".xlsx") && !nom.toLowerCase().endsWith(".xls"))) {
                return "Erreur : le fichier doit être un Excel (.xls ou .xlsx).";
            }

            File tempFile = File.createTempFile("grille-", nom);
            file.transferTo(tempFile);

            List<Tarif> nouvelleGrille = DonneesTarifs.chargerGrilleStandard(tempFile.getAbsolutePath());

            if (nouvelleGrille == null || nouvelleGrille.isEmpty()) {
                return "Erreur : aucune donnée tarifaire valide trouvée.";
            }

            this.grilleCourante = nouvelleGrille;
            this.nomGrilleCourante = file.getOriginalFilename();

            return "Fichier chargé avec succès : " + nouvelleGrille.size() + " tranche(s) détectée(s).";
        } catch (IllegalArgumentException e) {
            return "Erreur : " + e.getMessage();
        } catch (Exception e) {
            return "Erreur technique : " + e.getMessage();
        }
    }
}