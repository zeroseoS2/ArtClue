package ArtClue_Client;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Music extends Thread {
    private String musicFilePath;
    private boolean loop;
    private Clip clip;

    public Music(String musicFilePath, boolean loop) {
        this.musicFilePath = musicFilePath; //ArtClue 클래스에서 음악 경로 받아오기
        this.loop = loop; //반복재생
    }

    @Override
    public void run() {
        try {
            File musicFile = new File(musicFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
                clip.drain();
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // 음악 중지 메서드
    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}