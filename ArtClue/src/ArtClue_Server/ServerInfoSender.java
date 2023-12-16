package ArtClue_Server;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ServerInfoSender extends Thread{
	private ArrayList<PrintWriter> listWriters[] = null;
	ServerInfoSender(ArrayList<PrintWriter> listWriters[]){
		this.listWriters=listWriters;
	}
	public void run() {
		while(true) {
			try {
				sleep(1000);
				synchronized(listWriters) {
					String[] num = new String[5];
					for(int i=0;i<5;i++) {
						num[i]=Integer.toString(listWriters[i].size());
					}
					//방
					for(int i=0;i<5;i++) {
						for(PrintWriter a:listWriters[i]) {
							a.println("serverinfo:"+num[0]+":"+num[1]+":"+num[2]+":"+num[3]+":"+num[4]);
							a.flush();
						}
					}
				}
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}