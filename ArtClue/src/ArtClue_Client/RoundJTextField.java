package ArtClue_Client;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JTextField;
//custom JTextfield  
public class RoundJTextField extends JTextField {
    private Shape shape;
    public RoundJTextField(int size) {
        super(size);
        setOpaque(false);
    }
    // 컴포넌트를 그릴 때 호출되는 메서드	
    @Override
	protected void paintComponent(Graphics g) {
         g.setColor(getBackground());
         g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
         super.paintComponent(g);
    }
    // 테두리를 그릴 때 호출되는 메서드
    @Override
	protected void paintBorder(Graphics g) {
         g.setColor(getForeground());
         // 둥근 테두리를 가진 사각형의 테두리를 그림
         g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
    }
    // 컴포넌트가 지정된 좌표를 포함하는지 확인하는 메서드
    @Override
	public boolean contains(int x, int y) {
         if (shape == null || !shape.getBounds().equals(getBounds())) {
             shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 15, 15);
         }
         return shape.contains(x, y);
    }
}