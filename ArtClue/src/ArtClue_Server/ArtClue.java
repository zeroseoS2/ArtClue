package ArtClue_Server;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ArtClue {
    ServerSocket serverSocket = null;
    ArrayList<PrintWriter> listWriters[] = new ArrayList[5];

    public ArtClue() {
        for (int i = 0; i < 5; i++) {
            listWriters[i] = new ArrayList<PrintWriter>();
        }
        new Chat(listWriters).start();
        new ServerInfoSender(listWriters).start();
        access();
    }

    public void access() {
        try {
            serverSocket = new ServerSocket();

            String hostAddress = "127.0.0.1";
            serverSocket.bind(new InetSocketAddress(hostAddress, Main.Port));
            consoleLog("연결 기다림 - " + hostAddress + ":" + Main.Port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("someone tried connect");
                BufferedReader buffereedReader =
                        new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                // read for what to do
                String request = buffereedReader.readLine();
                if (request.equals("login")) {
                    login(socket, buffereedReader);
                    System.out.println("someone tried login");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void login(Socket socket, BufferedReader buffereedReader) {
        System.out.println("로그인함");
        new ServerManager(socket, buffereedReader, listWriters).start();
    }

 // 메소드 추가: 색상 변경 메소드
	/*
	 * public void setColor(int rgb, String nickname) { Color color = new
	 * Color(rgb); this.newColor = color;
	 * 
	 * // 해당 방의 클라이언트들에게만 색상 변경 메시지 전송 String colorMsg = "color:" + rgb + ":" +
	 * nickname; broadcast(colorMsg, getRoomNumberByNickname(nickname)); }
	 * 
	 * // 메소드 추가: 닉네임으로부터 방 번호를 얻는 메소드 public int getRoomNumberByNickname(String
	 * nickname) { for (int i = 0; i < 5; i++) { if
	 * (listWriters[i].containsKey(nickname)) { return i; } } return -1; // 방 번호를 찾지
	 * 못한 경우 }
	 * 
	 * // 메소드 추가: 메시지를 특정 방에 브로드캐스트하는 메소드 public void broadcast(String message, int
	 * roomNumber) { try { // 특정 방에 브로드캐스트 ArrayList<PrintWriter> writers =
	 * listWriters[roomNumber]; for (PrintWriter writer : writers) {
	 * writer.println(message); writer.flush(); } } catch (Exception e) {
	 * e.printStackTrace(); } }
	 */

    private static void consoleLog(String log) {
        System.out.println("[server " + Thread.currentThread().getId() + "] " + log);
    }

    // 메소드 추가: 메시지를 특정 방 또는 전체에 브로드캐스트하는 메소드
    public void broadcast(String message, int roomNumber) {
        try {
            ArrayList<PrintWriter> writers = listWriters[roomNumber];
            for (PrintWriter writer : writers) {
                writer.println(message);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
