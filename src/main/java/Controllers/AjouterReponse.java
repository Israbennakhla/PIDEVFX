package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Reponse;
import services.ServiceReponse;

import java.time.LocalDateTime;

public class AjouterReponse {

    @FXML private TextField champAuteur;
    @FXML private TextArea  champContenu;
    @FXML private Label     errAuteur;
    @FXML private Label     errContenu;

    private final ServiceReponse service = new ServiceReponse();
    private int reclamationId;

    public void setReclamationId(int id) {
        this.reclamationId = id;
    }

    @FXML
    private void enregistrer() {
        errAuteur.setText("");
        errContenu.setText("");

        boolean ok = true;

        // Validation auteur
        if (champAuteur.getText().trim().isEmpty()) {
            errAuteur.setText("⚠️ Le nom de l'auteur est obligatoire.");
            ok = false;
        } else if (champAuteur.getText().trim().length() < 3) {
            errAuteur.setText("⚠️ Minimum 3 caractères.");
            ok = false;
        }

        // Validation contenu
        if (champContenu.getText().trim().isEmpty()) {
            errContenu.setText("⚠️ Le contenu est obligatoire.");
            ok = false;
        } else if (champContenu.getText().trim().length() < 10) {
            errContenu.setText("⚠️ Minimum 10 caractères.");
            ok = false;
        }

        if (!ok) return;

        Reponse rep = new Reponse(
                champContenu.getText().trim(),
                LocalDateTime.now(),
                champAuteur.getText().trim(),
                reclamationId
        );

        service.add(rep);
        new Alert(Alert.AlertType.INFORMATION, "✅ Réponse ajoutée !").showAndWait();
        fermer();
    }

    @FXML
    private void annuler() { fermer(); }

    private void fermer() {
        ((Stage) champAuteur.getScene().getWindow()).close();
    }
}