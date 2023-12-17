package ArtClue_Client;

public class Main {

	public static final int SCREEN_WIDTH=1280;
	public static final int SCREEN_HEIGHT=720;
	public static final int Port=9999;

    public static void main(String[] args) {
        ArtClue artClue = new ArtClue();
        artClue.startApplication();
    }
}
