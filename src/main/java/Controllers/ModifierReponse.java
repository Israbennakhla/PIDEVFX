package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Reponse;
import services.ServiceReponse;

public class ModifierReponse {

    @FXML private TextField champAuteur;
    @FXML private TextArea  champContenu;
    @FXML private Label     errAuteur;
    @FXML private Label     errContenu;

    private final ServiceReponse service = new ServiceReponse();
    private Reponse reponse;

    public void setReponse(Reponse r) {
        this.reponse = r;
        champAuteur.setText(r.getAuteur());
        champContenu.setText(r.getContenu());
    }

    @FXML
    private void enregistrer() {
        errAuteur.setText("");
        errContenu.setText("");

        boolean ok = true;

        if (champAuteur.getText().trim().isEmpty()) {
            errAuteur.setText("⚠️ L'auteur est obligatoire.");
            ok = false;
        } else if (champAuteur.getText().trim().length() < 3) {
            errAuteur.setText("⚠️ Minimum 3 caractères.");
            ok = false;
        }

        if (champContenu.getText().trim().isEmpty()) {
            errContenu.setText("⚠️ Le contenu est obligatoire.");
            ok = false;
        } else if (champContenu.getText().trim().length() < 10) {
            errContenu.setText("⚠️ Minimum 10 caractères.");
            ok = false;
        }

        if (!ok) return;

        reponse.setAuteur(champAuteur.getText().trim());
        reponse.setContenu(champContenu.getText().trim());

        service.update(reponse);
        new Alert(Alert.AlertType.INFORMATION, "✅ Réponse modifiée !").showAndWait();
        fermer();
    }

    @FXML
    private void annuler() { fermer(); }

    private void fermer() {
        ((Stage) champAuteur.getScene().getWindow()).close();
    }
}