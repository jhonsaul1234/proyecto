package tienda.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.model.Usuario;
import tienda.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean authenticateUser(String email, String password) {
        return usuarioRepository.findByEmailAndPassword(email, password).isPresent();
    }
}
