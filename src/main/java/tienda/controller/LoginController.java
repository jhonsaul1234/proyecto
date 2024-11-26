// controller/LoginController.java
package tienda.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tienda.service.UsuarioService;

@Component
public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Por favor complete todos los campos");
            return;
        }

        if (usuarioService.authenticateUser(email, password)) {
            openTiendaUI();
        } else {
            showAlert("Error de autenticación", "Email o contraseña incorrectos");
        }
    }

    private void openTiendaUI() {
        try {
            // Crear un nuevo FXMLLoader
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tienda/ui/main.fxml"));

            // Configurar el loader para usar el contexto de Spring
            loader.setControllerFactory(applicationContext::getBean);

            // Cargar la vista
            Parent root = loader.load();

            // Configurar y mostrar la nueva escena
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Sistema de Ventas");
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo abrir la tienda: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}