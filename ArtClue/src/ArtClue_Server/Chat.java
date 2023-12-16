package ArtClue_Server;

import java.io.PrintWriter;
import java.util.*;

public class Chat extends Thread{
	ArrayList<PrintWriter>[] listWriters;
	public Chat(ArrayList<PrintWriter>[] listWriters) {
		// TODO Auto-generated constructor stub
		this.listWriters = listWriters;
	}

	public void run() {
		Scanner sc = new Scanner(System.in);
		while(true) {
			String str = sc.nextLine();
			str = "*" + str;
			for (int i = 0; i < 5; i++) {
				synchronized (listWriters) {
					for (PrintWriter writer : listWriters[i]) {
						writer.println(str);
						writer.flush();
					}
				}
			}
		}
	}
}