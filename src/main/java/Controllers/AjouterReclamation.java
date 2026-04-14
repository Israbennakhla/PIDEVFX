package Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Reclamation;
import services.ServiceReclamation;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AjouterReclamation implements Initializable {

    @FXML private TextField  champSujet;
    @FXML private TextArea   champDescription;
    @FXML private ComboBox<String> champPriorite;
    @FXML private ComboBox<String> champStatut;
    @FXML private TextField  champNom;
    @FXML private TextField  champEmail;

    private final ServiceReclamation service = new ServiceReclamation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        champPriorite.setItems(FXCollections.observableArrayList(
                "basse", "moyenne", "haute"
        ));
        champStatut.setItems(FXCollections.observableArrayList(
                "en_attente", "en_cours", "resolue"
        ));
        champStatut.setValue("en_attente");
    }

    @FXML
    private void enregistrer() {
        // ── Validation champs vides ──
        if (champSujet.getText().trim().isEmpty() ||
                champDescription.getText().trim().isEmpty() ||
                champNom.getText().trim().isEmpty() ||
                champEmail.getText().trim().isEmpty() ||
                champPriorite.getValue() == null) {

            new Alert(Alert.AlertType.WARNING,
                    "❌ Veuillez remplir tous les champs obligatoires.")
                    .showAndWait();
            return;
        }

        Reclamation r = new Reclamation(
                champSujet.getText().trim(),
                champDescription.getText().trim(),
                LocalDateTime.now(),
                champStatut.getValue(),
                champPriorite.getValue(),
                champNom.getText().trim(),
                champEmail.getText().trim(),
                1  // userId par défaut
        );

        // ── Validation métier (sujet min 5 chars, desc min 10, email valide) ──
        String validation = service.valider(r);
        if (!validation.equals("OK")) {
            new Alert(Alert.AlertType.WARNING, validation).showAndWait();
            return;
        }

        service.add(r);
        new Alert(Alert.AlertType.INFORMATION, "✅ Réclamation ajoutée !").showAndWait();
        fermer();
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        ((Stage) champSujet.getScene().getWindow()).close();
    }
}