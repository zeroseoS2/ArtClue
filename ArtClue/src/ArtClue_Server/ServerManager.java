package ArtClue_Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ServerManager extends Thread{
	private String nickname;
    private Socket socket = null;
    private BufferedReader buffereedReader = null;
    private PrintWriter printWriter = null;
    private int WhereIam;
    private ArrayList<PrintWriter> listWriters[] = null;
    static int isAnswer=0;
    static int Answer=0;
    //추가
    private int currentPlayerIndex = 0;
    private int totalPlayers = 0;
    private boolean gameStarted = false;
    private boolean selectingDrawer = false;
    private String currentWord; // 현재 제시어를 저장할 변수

    
    public ServerManager(Socket socket, BufferedReader BR, ArrayList<PrintWriter> LW[]) {
        this.socket = socket;
        this.buffereedReader = BR;
        this.WhereIam=0;
        this.listWriters = LW;
    }
    
    public void run() {
    	try {
    		printWriter = 
	            new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    		String request = buffereedReader.readLine();
    		System.out.println("닉네임은"+request);
    		this.nickname = request;
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
        rubyChat();
    }
    
    private void rubyChat(){
    	try {
            while(true) {
                String request = buffereedReader.readLine();
                if( request == null) {
                	System.out.println("누군가 나갑니다");
                    doQuit(printWriter,this.WhereIam);
                    break;
                }
                
                String[] tokens = request.split(":");
                //join:이름:방번호
                if("join".equals(tokens[0])) {
                    doJoin(printWriter,Integer.parseInt(tokens[1]));
                    if(Integer.parseInt(tokens[1])==0)
                    	System.out.println("main chatroom entry");
                    else
                    	System.out.println("room "+tokens[1]+" entry");
                }
                else if ("rungame".equals(tokens[0])) {
                    runGame(Integer.parseInt(tokens[1]));
                }
             // 정답 메시지 처리
                else if ("answer".equals(tokens[0])) {
                    handleAnswer(tokens[1], Integer.parseInt(tokens[2]));
                }
                else if("message".equals(tokens[0])) {
                    doMessage(tokens[1],Integer.parseInt(tokens[2]));
                }
                else if("quit".equals(tokens[0])) {
                    doQuit(printWriter,Integer.parseInt(tokens[1]));
                }
                else if("draw".equals(tokens[0])) {
                	if("erase".equals(tokens[1])) 
                		doErase(Integer.parseInt(tokens[2]));
                	else
						doDraw(request, Integer.parseInt(tokens[5]));
				}
			}
		}
        catch(IOException e) {
            consoleLog(this.nickname + "님이 게임에서 나갔습니다.");
        }
    }

    private void doQuit(PrintWriter writer, int num) {
        removeWriter(writer, num);

        String data = "ㆍ[" + this.nickname + "]님이 퇴장했습니다.";
        synchronized (listWriters) {
        	System.out.println(listWriters[0]);
        }
        broadcast(data,num);
        //추가
        if (gameStarted && listWriters[WhereIam].size() <= 1) {
            endGame();
        }
    }

    private void removeWriter(PrintWriter writer, int num) {
        synchronized (listWriters) {
            listWriters[num].remove(writer);
        }
    }

    private void doJoin(PrintWriter writer, int num) {
        String data ="ㆍ["+ nickname + "]님이 입장하였습니다.";
        broadcast(data,num);
        WhereIam=num;
        
        addWriter(writer,num);
        //추가
        totalPlayers++;
        if (totalPlayers >= 2 && !gameStarted) {
            selectingDrawer = true;
            broadcast("ㆍ게임을 시작하려면 스타트 버튼을 눌러주세요.", num);
        }
        
        synchronized (listWriters) {
        	System.out.println(listWriters[0]);
        }
	    System.out.println(this.nickname+"이가 입장");
    }
    //추가
    private void startGame(int num) {
        gameStarted = true;
        selectingDrawer = false;
        currentPlayerIndex = 0;
        broadcast("ㆍ게임이 시작되었습니다. 술래를 선택합니다.", num);
    }
    private void runGame(int num) {
        // 게임 시작을 알리는 메시지를 클라이언트들에게 브로드캐스트
        System.out.println("Received rungame message from client " + this.nickname);
        broadcast("ㆍ게임이 시작되었습니다. 술래를 선택합니다.", WhereIam);

        // 술래 선택 로직 추가
        selectDrawer(WhereIam);
    }

    private void selectDrawer(int num) {
    	// 현재는 랜덤으로 술래를 선택하는 예시 코드
        if (listWriters[num].size() > 0) {
            int drawerIndex = new Random().nextInt(listWriters[num].size());
            PrintWriter drawer = listWriters[num].get(drawerIndex);
            String selectedWord = getRandomWord();
            drawer.println("ㆍ당신이 술래입니다. 제시어: " + selectedWord);
            drawer.flush();

            currentPlayerIndex = drawerIndex + 1;
            if (currentPlayerIndex >= listWriters[num].size()) {
                currentPlayerIndex = 0;
            }
        }
    }
    private String getRandomWord() {
        // 이 부분에서 answer.txt에서 랜덤으로 단어를 읽어오는 로직을 추가하세요.
        // 이 예시에서는 단순히 고정된 단어를 사용하고 있습니다.
        return "테스트";
    }
    
    private void doMessage(String data, int num) {
        // 게임이 시작되었고 술래 선택 중일 때만 정답 확인
        if (gameStarted && selectingDrawer) {
            if (data.equalsIgnoreCase(currentWord)) {
                handleAnswer(data, num);
                return;
            }
        }

        // 기존 채팅 메시지 처리
        broadcast("[" + this.nickname + "]: " + data, num);
    }
    
    private void handleAnswer(String answer, int num) {
        // 정답 처리 로직 추가
        if (answer.equalsIgnoreCase(currentWord)) {
            broadcast("ㆍ[" + this.nickname + "]님이 정답을 맞혔습니다!", num);

            // 다음 술래 선택
            selectNextDrawer(num);
            // 다음 술래에게 제시어 전송
            sendWordToDrawer(num);
            // 새로운 제시어 설정
            setCurrentWordFromRandomFile();
        } else {
            // 정답이 아닌 경우 일반 채팅으로 처리
            broadcast("[" + this.nickname + "]: " + answer, num);
        }
    }


    private String getRandomWordFromFile() {
        // TODO: answer.txt 파일에서 랜덤으로 단어를 읽어오는 로직을 구현하세요.
        // 이 예시에서는 단순히 고정된 단어를 사용하고 있습니다.
        return "테스트";
    }
    private void setCurrentWordFromRandomFile() {
        // answer.txt 파일에서 랜덤으로 제시어를 읽어와서 currentWord 변수에 설정
        // 이 부분은 실제 파일에서 읽어오는 로직으로 대체해야 합니다.
        currentWord = getRandomWordFromFile();
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
        // 게임이 종료되면 각 참가자의 정답 횟수를 계산하고 결과를 알려주는 로직을 추가하세요.
        // 이 예시에서는 결과를 콘솔에 출력하고 있습니다.
        System.out.println("게임이 종료되었습니다.");

        // 초기화
        gameStarted = false;
        totalPlayers = 0;
        currentPlayerIndex = 0;

        // TODO: 결과 계산 및 알림 로직 추가
    }
    
    
    
    
    private void doDraw(String points, int roomnum) {
    	synchronized (listWriters) {
    		for(PrintWriter writer : listWriters[roomnum]) {
                writer.println(points);
                writer.flush();
            }
    	}
    }
    
    private void doErase(int roomnum) {
    	synchronized (listWriters) {
    		for(PrintWriter writer : listWriters[roomnum]) {
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
            for(PrintWriter writer : listWriters[num]) {
                writer.println(data);
                writer.flush();
            }
        }
    }

    private void consoleLog(String log) {
        System.out.println(log);
    }
}