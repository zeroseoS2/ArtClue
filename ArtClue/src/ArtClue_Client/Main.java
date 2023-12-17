package ArtClue_Client;

public class Main {

	public static final int SCREEN_WIDTH=1280;
	public static final int SCREEN_HEIGHT=720;
	public static final int Port=9999;

    public static void main(String[] args) {
        ArtClue artClue = new ArtClue();
        //음악 재생을 위한 메소드 호출
        artClue.startApplication();
    }
}
