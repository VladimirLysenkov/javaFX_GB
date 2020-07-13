package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {
    public List<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();

        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился!");
                System.out.println("socket.getRemoteSocketAddress(): " + socket.getRemoteSocketAddress());
                System.out.println("socket.getLocalSocketAddress() " + socket.getLocalSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert server != null;
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void broadcastMsg(String msg){
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    //проверка авторизован ли юзер
    boolean isCanLogin(String nick) {
        if (clients.isEmpty()) {
            return true;
        }
        for (ClientHandler cl: clients) {
            if (cl.getNick().equals(nick)) {
                return false;
            }
        } return true;
    }

    //метод отправка личного сообщения юзеру
    public void privateMsg(ClientHandler fromUser, String toUser, String msg) {
        if(isCanLogin(toUser)) {
            fromUser.sendMsg(toUser + " не авторизован!");
        } else {
            for (ClientHandler client: clients) {
                if(client.getNick().equals(toUser)) {
                    client.sendMsg("Сообщение от " + fromUser.getNick() + ": " + msg);
                    break;
                }
            }
            fromUser.sendMsg("Сообщение для " + toUser + ": " + msg);
        }
    }

}