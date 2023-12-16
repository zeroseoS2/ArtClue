package ArtClue_Client;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class RoundButton extends JButton {
	private Image image;
    public RoundButton(ImageIcon img) {
        super();
        this.image=img.getImage();
        setOpaque(false);
        setBorderPainted(false);
       
    }

    @Override 
    protected void paintComponent(Graphics g) {
    	 int width = getWidth();
         int height = getHeight();
         Graphics2D graphics = (Graphics2D) g;
         graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

         // 그림자 효과를 위한 설정
         graphics.setColor(new Color(0, 0, 0, 50));
         graphics.fillRoundRect(3, 3, width - 6, height - 6, 10, 10);

         // 실제 이미지 그리기
         graphics.drawImage(image, 0, 0, width, height, this);

         FontMetrics fontMetrics = graphics.getFontMetrics();
         Rectangle stringBounds = fontMetrics.getStringBounds(this.getText(), graphics).getBounds();
         int textX = (width - stringBounds.width) / 2;
         int textY = (height - stringBounds.height) / 2 + fontMetrics.getAscent();
         graphics.drawString(getText(), textX, textY);

         graphics.dispose();
         super.paintComponent(g);
       }
}
