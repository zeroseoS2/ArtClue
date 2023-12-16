package ArtClue_Client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ArtClueReceiver extends Thread {
    Socket socket;
    JTextArea textArea;
    BufferedReader br;
    JScrollPane sp;
    ArtClue cm;
    public Color currentColor;


    ArtClueReceiver(Socket socket, JTextArea textArea, BufferedReader br, JScrollPane sp) {
        this.socket = socket;
        this.textArea = textArea;
        this.br = br;
        this.sp = sp;
    }

    ArtClueReceiver(Socket socket, ArtClue cm) {
        this.socket = socket;
        this.cm = cm;
        this.textArea = cm.chatArea;
        this.br = cm.br;
        this.sp = cm.chatSP;
    }

    public void run() {
        while (true) {
            String msg = null;
            try {
                msg = cm.br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(msg);
            String[] tokens = msg.split(":");
            if (tokens[0].equals("serverinfo")) {
                cm.refreshInfo(tokens);
            } else if (tokens[0].equals("draw")) {
                if (tokens[1].equals("erase")) {
                    cm.gDrawing.setColor(Color.WHITE);
                    cm.gDrawing.fillRect(0, 0, 550, 490);
                    cm.repaint();
                } else {
                    int sx = Integer.parseInt(tokens[1]);
                    int sy = Integer.parseInt(tokens[2]);
                    int ex = Integer.parseInt(tokens[3]);
                    int ey = Integer.parseInt(tokens[4]);
                    Graphics2D g2 = (Graphics2D) cm.gDrawing;
                    g2.setColor(cm.getCurrentColor()); // 현재 설정된 색상 사용
                    g2.setStroke(new BasicStroke(5, 1, 1));
                    cm.gDrawing.drawLine(sx, sy, ex, ey);
                    cm.repaint();
                }
            } else if (tokens[0].equals("changeColor")) {
                handleColorChange(tokens);
            } else {
                textArea.append(msg);
                textArea.append("\n");
                sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
            }
        }
    }

    private void handleColorChange(String[] tokens) {
        if (tokens.length > 1) {
            String[] colorValues = tokens[1].split(",");
            if (colorValues.length == 3) {
                int red = Integer.parseInt(colorValues[0]);
                int green = Integer.parseInt(colorValues[1]);
                int blue = Integer.parseInt(colorValues[2]);

                // 변경된 색상 정보를 사용하여 UI 업데이트
                cm.updateColor(new Color(red, green, blue));
            } else {
                System.out.println("부적절한 색상 정보 수신");
            }
        } else {
            System.out.println("색상 정보가 없습니다.");
        }
    }


    // 서버로 변경된 색상 정보 전송
    private void sendColorToServer(Color newColor) {
        try {
            // 서버로 전달하는 코드 추가
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("changeColor:" + newColor.getRed() + "," + newColor.getGreen() + "," + newColor.getBlue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
