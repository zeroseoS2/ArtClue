package ArtClue_Server;

import java.awt.Color;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private ArrayList<String> answerList[];
    private ArrayList<Socket> clients;  // 혹은 List<Socket> clients;로 선언돼 있을 것입니다.
    private Color currentColor = Color.BLACK;


    // ServerManager 클래스의 필드 선언 부분에 추가
    private int[] playerPoints; 
    private static String currentWord;

    public ServerManager(Socket socket, BufferedReader bufferedReader, ArrayList<PrintWriter> listWriters[], ArrayList<String> answerList[]) {
        this.socket = socket;
        this.bufferedReader = bufferedReader;
        this.listWriters = listWriters;
        this.answerList = answerList;

        // 생성자에서 각 클라이언트의 playerPoints 배열 초기화
        this.playerPoints = new int[4];

        // 생성자에서 클라이언트 리스트 초기화
        this.clients = new ArrayList<>();
        this.clients.add(socket);
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
                    case "changeColor":
                        handleColorChange(tokens, socket); // 수정된 부분
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

    
 // ServerManager 클래스 내부에 추가된 broadcastMessage 메서드
    private void broadcastMessage(String message) {
        for (Socket client : clients) {
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
                pw.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // ServerManager 클래스의 필드 선언 부분에 추가
    private List<ClientColor> clientColors = new ArrayList<>();

    // ClientColor 클래스 추가
    private static class ClientColor {
        private Socket socket;
        private Color color;

        public ClientColor(Socket socket, Color color) {
            this.socket = socket;
            this.color = color;
        }

        public Socket getSocket() {
            return socket;
        }

        public Color getColor() {
            return color;
        }

        // setColor 메서드 추가
        public void setColor(Color color) {
            this.color = color;
        }
    }

    // handleColorChange 메서드 수정
    private void handleColorChange(String[] parts, Socket clientSocket) {
        if (parts.length > 1) {
            String[] colorValues = parts[1].split(",");
            if (colorValues.length == 3) {
                int red = Integer.parseInt(colorValues[0]);
                int green = Integer.parseInt(colorValues[1]);
                int blue = Integer.parseInt(colorValues[2]);

                // 범위를 벗어나는 값 방지
                red = Math.max(0, Math.min(255, red));
                green = Math.max(0, Math.min(255, green));
                blue = Math.max(0, Math.min(255, blue));

                // 추출한 색상 정보로 처리
                Color newColor = new Color(red, green, blue);

                // 해당 클라이언트의 정보와 함께 다른 클라이언트에게 전파
                broadcastColorChange(newColor);
                // 변경된 색상 정보를 서버에 저장
                setCurrentColor(newColor, clientSocket);
            } else {
                System.out.println("부적절한 색상 정보 수신");
            }
        } else {
            System.out.println("색상 정보가 없습니다.");
        }
    }

    // setCurrentColor 메서드 수정
    private void setCurrentColor(Color newColor, Socket clientSocket) {
        for (ClientColor clientColor : clientColors) {
            if (clientColor.getSocket() == clientSocket) {
                clientColor.setColor(newColor);
                break;
            }
        }
    }

    // broadcastColorChange 메서드 수정
    private void broadcastColorChange(Color newColor) {
        for (PrintWriter writer : listWriters[whereIAm]) {
            writer.println("changeColor:" + newColor.getRed() + "," + newColor.getGreen() + "," + newColor.getBlue());
            writer.flush();
        }
    }

 // 현재 색상을 반환하는 메서드
    public Color getCurrentColor() {
        return currentColor;
    }

    // 현재 색상을 변경하는 메서드
    public void setCurrentColor(Color color) {
        this.currentColor = color;

        // 변경된 색상 정보를 다른 클라이언트에게 전파
        broadcastColorChange(color);
    }

    private void doQuit(PrintWriter writer, int num) {
        removeWriter(writer, num);
        String data = "ㆍ[" + this.nickname + "]님이 퇴장했습니다.";
        broadcast(data, num);
        
        endGame();
        
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
            // 모든 클라이언트가 방에 들어왔을 때만 게임 시작
            selectingDrawer = true;
            broadcast("ㆍ게임을 시작하려면 스타트 버튼을 눌러주세요.", num);
        }

        System.out.println(nickname + "이가 입장");
    }

    private void startGame(int num) {
        gameStarted = true;
        selectingDrawer = false;
        currentPlayerIndex = 0;

        resetPlayerPoints();

        broadcast("ㆍ게임이 시작되었습니다. 술래를 선택합니다.", num);
    }

    // 모든 플레이어의 포인트를 0으로 초기화하는 메서드
    private void resetPlayerPoints() {
        for (int i = 0; i < playerPoints.length; i++) {
            playerPoints[i] = 0;
        }
    }

    private void runGame(int num) {
        System.out.println("Received rungame message from client " + nickname);
        broadcast("ㆍ게임이 시작되었습니다. 술래를 선택합니다.", whereIAm);
        selectDrawer(whereIAm);
    }

    private void selectDrawer(int num) {
        if (listWriters[num].size() > 0) {
            // 모든 클라이언트에 대해 술래를 정하지 않았다면 술래를 정함
            if (!selectingDrawer) {
                currentPlayerIndex = totalPlayers - 1;
                selectingDrawer = true;
            } else {
                // 현재 술래 다음 순서로 설정
                currentPlayerIndex = (currentPlayerIndex + 1) % totalPlayers;
            }

            PrintWriter drawer = listWriters[num].get(currentPlayerIndex);
            currentWord = getRandomWordFromFile();
            drawer.println("ㆍ당신이 술래입니다. 제시어: " + currentWord);
            drawer.flush();

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

    private void setCurrentWordFromRandomFile() {
        currentWord = getRandomWordFromFile();
    }

    private void handleAnswer(String answer) {
        System.out.println("Received answer from client " + nickname + ": " + answer);

        if (currentWord == null) {
            System.out.println("currentWord is null. Initializing...");
            setCurrentWordFromRandomFile();
            return;
        }

        if (answer.trim().equalsIgnoreCase(currentWord.trim())) {
            System.out.println("Correct answer!");

            // 점수 증가 및 모든 클라이언트에게 알림
            increasePlayerPoint(whereIAm); // 현재 클라이언트의 인덱스를 전달
            broadcast("ㆍ[" + nickname + "]님이 정답을 맞혔습니다!\n" + ">" + nickname + "님의 현재 포인트: " + playerPoints[whereIAm], whereIAm);

            if (playerPoints[whereIAm] >= 3) {
                broadcast("ㆍ[" + nickname + "]님이 3점을 달성하여 이겼습니다!\n    --게임종료--", whereIAm);
                endGame();
            } else {
                setCurrentWordFromRandomFile();
                selectNextDrawer(whereIAm);
                
                eraseDrawing(whereIAm);
            }
        } else {
            System.out.println("Incorrect answer." + currentWord + " " + answer);
            broadcast("[" + nickname + "]: " + answer, whereIAm);
        }
    }

    // 플레이어 점수 증가 메서드
    public void increasePlayerPoint(int playerIndex) {
        // playerIndex가 유효한 범위인지 확인
        if (playerIndex >= 0 && playerIndex < playerPoints.length) {
            // 올바른 인덱스에 접근
            playerPoints[playerIndex] += 1;
            System.out.println("Player " + playerIndex + "'s points: " + playerPoints[playerIndex]);
        } else {
            // 유효하지 않은 인덱스에 대한 처리
            System.out.println("Invalid player index");
        }
    }

	private void selectNextDrawer(int num) {
        currentPlayerIndex++;
        if (currentPlayerIndex >= listWriters[num].size()) {
            currentPlayerIndex = 0;
        }

        // 술래 선택 후에 새로운 술래에게만 제시어를 전송
        sendWordToDrawer(num);
    }
	
	private void eraseDrawing(int num) {
	    synchronized (listWriters) {
	        for (PrintWriter writer : listWriters[num]) {
	            writer.println("changeColor:0,0,0");
	            writer.flush();
	            
	            writer.println("draw:erase");
	            writer.flush();
	        }
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
        
        Arrays.fill(playerPoints, 0);

        resetPlayerPoints();
        setCurrentWordFromRandomFile();
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
