package server;

import java.sql.*;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);
}