package ArtClue_Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
//파일 읽어오는 클래스
public class FileRead {

    private File file;
    private final String dir;

    private ArrayList<String> list;

    public FileRead(String filePath) {
        this.dir = filePath;
    }

	public void read() {
        makeList();
        readstart();
    }

    private void makeList() {
        list = new ArrayList<>();
    }

    private void readstart() {
        try {
            file = new File(dir);
            FileReader filereader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(filereader);
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                list.add(line);
            }
            bufReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + dir);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public ArrayList<String> getAnswer() {
        return this.list;
    }
}