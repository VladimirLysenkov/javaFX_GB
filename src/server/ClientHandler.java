package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    Server server;
    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    private String nick;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split("\\s");
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                sendMsg("/authok " + newNick);
                                nick = newNick;
                                login = token[1];
                                server.subscribe(this);
                                System.out.printf("Клиент %s подключился \n", nick);
                                break;
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }

                        server.broadcastMsg(str);
                    }
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        //отправка личного сообщения
                        if (str.startsWith("/w")) {
                            String to = str.split(" ")[1];
                            String msg = str.split(" ")[2];
                            privateMsg(ClientHandler.this, to, msg);
                        } else {
                            server.broadcastMsg(nick + ": " + str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Клиент отключился");
                    server.unsubscribe(this);
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void sendMsg(String str) {
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //метод отправка личного сообщения юзеру
    public void privateMsg(ClientHandler fromUser, String toUser, String msg) {
        if(server.isCanLogin(toUser)) {
            sendMsg(toUser + " не авторизован!");
        } else {
            for (ClientHandler client: server.clients) {
                if(client.getNick().equals(toUser)) {
                    client.sendMsg("Сообщение от " + fromUser.getNick() + ": " + msg);
                    break;
                }
            }
            fromUser.sendMsg("Сообщение для " + toUser + ": " + msg);
        }
    }

    public String getNick() {
        return nick;
    }
}
