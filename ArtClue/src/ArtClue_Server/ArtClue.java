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
    ArrayList<String> answerList[] = new ArrayList[5]; // 정답 리스트 선언

    public ArtClue() {
        for (int i = 0; i < 5; i++) {
            listWriters[i] = new ArrayList<PrintWriter>();
            // FileRead 클래스를 사용하여 정답 목록을 읽어옴
	        FileRead fileRead = new FileRead("src/ArtClue_Server/answer.txt");
	        ArrayList<String> answerList = fileRead.getAnswer();
        }
        new Chat(listWriters, answerList).start(); // Chat 클래스에 정답 리스트를 전달
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
        new ServerManager(socket, buffereedReader, listWriters, answerList).start();
    }



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
