package ArtClue_Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ServerManager extends Thread {
    private String nickname;
    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private int whereIAm;
    private ArrayList<PrintWriter> listWriters[];
    private int currentPlayerIndex = 0;
    private int totalPlayers = 0;
    private boolean gameStarted = false;
    private boolean selectingDrawer = false;
    private String currentWord;
    private ArrayList<String> answerList[];

    public ServerManager(Socket socket, BufferedReader bufferedReader, ArrayList<PrintWriter> listWriters[], ArrayList<String> answerList[]) {
        this.socket = socket;
        this.bufferedReader = bufferedReader;
        this.listWriters = listWriters;
        this.answerList = answerList;
    }

    public void run() {
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            String request = bufferedReader.readLine();
            System.out.println("닉네임은 " + request);
            this.nickname = request;
        } catch (Exception e) {
            e.printStackTrace();
        }

        rubyChat();
    }

    private void rubyChat() {
        try {
            while (true) {
                String request = bufferedReader.readLine();
                if (request == null) {
                    System.out.println("누군가 나갑니다");
                    doQuit(printWriter, this.whereIAm);
                    break;
                }

                String[] tokens = request.split(":");
                switch (tokens[0]) {
                    case "join":
                        doJoin(printWriter, Integer.parseInt(tokens[1]));
                        System.out.println(Integer.parseInt(tokens[1]) == 0 ? "main chatroom entry" : "room " + tokens[1] + " entry");
                        break;
                    case "rungame":
                        runGame(Integer.parseInt(tokens[1]));
                        break;
                    case "answer":
                        handleAnswer(tokens[1]);
                        break;
                    case "message":
                        doMessage(tokens[1], Integer.parseInt(tokens[2]));
                        break;
                    case "quit":
                        doQuit(printWriter, Integer.parseInt(tokens[1]));
                        break;
                    case "draw":
                        if ("erase".equals(tokens[1]))
                            doErase(Integer.parseInt(tokens[2]));
                        else
                            doDraw(request, Integer.parseInt(tokens[5]));
                        break;
                }
            }
        } catch (IOException e) {
            consoleLog(this.nickname + "님이 게임에서 나갔습니다.");
        }
    }

    private void doQuit(PrintWriter writer, int num) {
        removeWriter(writer, num);
        String data = "ㆍ[" + this.nickname + "]님이 퇴장했습니다.";
        broadcast(data, num);

        if (gameStarted && listWriters[whereIAm].size() <= 1) {
            endGame();
        }
    }

    private void removeWriter(PrintWriter writer, int num) {
        synchronized (listWriters) {
            listWriters[num].remove(writer);
        }
    }

    private void doJoin(PrintWriter writer, int num) {
        String data = "ㆍ[" + nickname + "]님이 입장하였습니다.";
        broadcast(data, num);
        whereIAm = num;
        addWriter(writer, num);

        totalPlayers++;
        if (totalPlayers >= 2 && !gameStarted) {
            selectingDrawer = true;
            broadcast("ㆍ게임을 시작하려면 스타트 버튼을 눌러주세요.", num);
        }

        System.out.println(nickname + "이가 입장");
    }

    private void startGame(int num) {
        gameStarted = true;
        selectingDrawer = false;
        currentPlayerIndex = 0;
        broadcast("ㆍ게임이 시작되었습니다. 술래를 선택합니다.", num);
    }

    private void runGame(int num) {
        System.out.println("Received rungame message from client " + nickname);
        broadcast("ㆍ게임이 시작되었습니다. 술래를 선택합니다.", whereIAm);
        selectDrawer(whereIAm);
    }

    private void selectDrawer(int num) {
        if (listWriters[num].size() > 0) {
            int drawerIndex = new Random().nextInt(listWriters[num].size());
            PrintWriter drawer = listWriters[num].get(drawerIndex);
            String selectedWord = getRandomWordFromFile();
            drawer.println("ㆍ당신이 술래입니다. 제시어: " + selectedWord);
            drawer.flush();

            currentPlayerIndex = drawerIndex + 1;
            if (currentPlayerIndex >= listWriters[num].size()) {
                currentPlayerIndex = 0;
            }
        }
    }

    private void doMessage(String data, int num) {
        if (gameStarted && selectingDrawer) {
            if (data.equalsIgnoreCase(currentWord)) {
                handleAnswer(data); // 방 번호(num)를 사용하지 않음
                return;
            }
        }
        broadcast("[" + nickname + "]: " + data, num);
    }

    private String getRandomWordFromFile() {
        try {
            String filePath = "src/ArtClue_Server/answer.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
            ArrayList<String> words = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                words.add(line);
            }

            if (!words.isEmpty()) {
                Collections.shuffle(words);
                return words.get(0);
            } else {
                System.out.println("단어 파일이 비어있습니다.");
                return "기본단어";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "기본단어";
        }
    }

    private void handleAnswer(String answer) {
        System.out.println("Received answer from client " + nickname + ": " + answer);

        if (answer.equalsIgnoreCase(currentWord)) {
            System.out.println("Correct answer!");

            broadcast("ㆍ[" + nickname + "]님이 정답을 맞혔습니다!", whereIAm);
            selectNextDrawer(whereIAm);
            sendWordToDrawer(whereIAm);
            currentWord = getRandomWordFromFile();

            System.out.println("New word: " + currentWord);
            broadcast("ㆍ새로운 제시어: " + currentWord, whereIAm);
        } else {
            System.out.println("Incorrect answer.");

            broadcast("[" + nickname + "]: " + answer, whereIAm);
        }
    }

    private void setCurrentWordFromRandomFile() {
        currentWord = getRandomWordFromFile();
        broadcast("ㆍ새로운 제시어: " + currentWord, whereIAm);
    }

    private void selectNextDrawer(int num) {
        currentPlayerIndex++;
        if (currentPlayerIndex >= listWriters[num].size()) {
            currentPlayerIndex = 0;
        }
    }

    private void sendWordToDrawer(int num) {
        if (listWriters[num].size() > 0) {
            PrintWriter drawer = listWriters[num].get(currentPlayerIndex);
            drawer.println("ㆍ당신이 술래입니다. 제시어: " + currentWord);
            drawer.flush();
        }
    }

    private void endGame() {
        System.out.println("게임이 종료되었습니다.");
        gameStarted = false;
        totalPlayers = 0;
        currentPlayerIndex = 0;
    }

    private void doDraw(String points, int roomNum) {
        synchronized (listWriters) {
            for (PrintWriter writer : listWriters[roomNum]) {
                writer.println(points);
                writer.flush();
            }
        }
    }

    private void doErase(int roomNum) {
        synchronized (listWriters) {
            for (PrintWriter writer : listWriters[roomNum]) {
                writer.println("draw:erase");
                writer.flush();
            }
        }
    }

    private void addWriter(PrintWriter writer, int num) {
        synchronized (listWriters) {
            listWriters[num].add(writer);
        }
    }

    private void broadcast(String data, int num) {
        synchronized (listWriters) {
            for (PrintWriter writer : listWriters[num]) {
                writer.println(data);
                writer.flush();
            }
        }
    }

    private void consoleLog(String log) {
        System.out.println(log);
    }
}
