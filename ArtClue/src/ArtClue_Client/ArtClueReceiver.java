package ArtClue_Client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ArtClueReceiver extends Thread{
	Socket socket;
	JTextArea textArea;
	BufferedReader br;
	JScrollPane sp;
	ArtClue cm;
	
	ArtClueReceiver(Socket socket, JTextArea textArea, BufferedReader br, JScrollPane sp){
        this.socket = socket;
        this.textArea = textArea;
        this.br = br;
        this.sp=sp;
    }
	ArtClueReceiver(Socket socket, ArtClue cm){
		this.socket = socket;
		this.cm=cm;
        this.textArea = cm.chatArea;
        this.br = cm.br;
        this.sp=cm.chatSP;
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
			}
			else if(tokens[0].equals("draw")) {
				if(tokens[1].equals("erase")) {
					cm.gDrawing.setColor(Color.WHITE);
					cm.gDrawing.fillRect(0, 0, 550, 490);
					cm.repaint();
				}
				else {
					int sx = Integer.parseInt(tokens[1]);
					int sy = Integer.parseInt(tokens[2]);
					int ex = Integer.parseInt(tokens[3]);
					int ey = Integer.parseInt(tokens[4]);
					Graphics2D g2 = (Graphics2D)cm.gDrawing;
					g2.setColor(Color.BLACK);
					g2.setStroke(new BasicStroke(5,1,1));
					cm.gDrawing.drawLine(sx, sy, ex, ey);
					cm.repaint();
				}
			}
			else if(tokens[0].equals("roominfo")) {
				if(tokens[1].equals("0")) {
					cm.Status.setText("Waiting");
				}
				else if(tokens[1].equals("1")) {
					cm.Status.setText("Running");
				}
			}
			else {
				textArea.append(msg);
				textArea.append("\n");
				sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
			}

		}
	}
}