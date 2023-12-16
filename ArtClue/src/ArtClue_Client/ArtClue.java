package ArtClue_Client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ArtClue extends JFrame{
	public Image screenImage;
	public Graphics screenGraphic;
	public ArtClue thisclass=this;
    private Color newColor = Color.BLACK; // 색상변경을 위함
    
	private int mouseX, mouseY;//창 이동
	
	//drawing board member
	public Image iDrawing = null;
	public Graphics gDrawing = null;
	public int x,y;
	public Image BackGroundImage = new ImageIcon(Main.class.getResource("/Image/BackGround.png")).getImage();
	
	public JButton exit = new JButton(new ImageIcon(Main.class.getResource("/Image/exit.png")));
	
	public JLabel title = new JLabel(new ImageIcon(Main.class.getResource("/Image/title.png")));
	
	//login and connection
	public JTextField name = new JTextField();
	public JButton loginButton = new JButton("Login");
	public JPasswordField password = new JPasswordField();
	public JTextField ServerIP = new JTextField("127.0.0.1");
	
	//Loby group
	public JLabel myInfo = new JLabel();
	public JLabel lobyInfototal = new JLabel();
	public JLabel lobyInfo = new JLabel();
	public JTextField chatInput = new JTextField();
	public JTextArea chatArea = new JTextArea();
	public JButton Room[] = new JButton[4];
	public JScrollPane chatSP = new JScrollPane(chatArea,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	public JButton colorButton = new JButton("Change");
	//Communication
	public Socket socket=null;
	public PrintWriter pw=null;
	public BufferedReader br = null;
	
	//game member group
	public JPanel DrawArea = new JPanel();
	public JLabel Roomnum = new JLabel();
	public JLabel Roompeople = new JLabel();
	public JButton Erase = new JButton(new ImageIcon(Main.class.getResource("/Image/Erase.png")));
	public JButton GoBack = new JButton(new ImageIcon(Main.class.getResource("/Image/GoBack.png")));
	public String nickname=null;
	public int WhereIAm=0;
	public JButton Start = new JButton("Start");
	public JLabel Status = new JLabel("?");
	
	int i;
	public ArtClue() {
		//Music introMusic = new Music("CandyLand.mp3", true);
		//introMusic.start();		
		this.setUndecorated(true);
		this.setTitle("Catch Mind");
		this.setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setBackground(new Color(0, 0, 0, 0));
		getContentPane().setLayout(null);
		
		//창 이동
		addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int newX = e.getXOnScreen() - mouseX;
                int newY = e.getYOnScreen() - mouseY;

                setLocation(newX, newY);
            }
        });
		// 그림판 초기화
		iDrawing = createImage(550, 490);
		gDrawing = iDrawing.getGraphics();
		gDrawing.setColor(Color.WHITE);
		gDrawing.fillRect(0, 0, 550, 490);
		repaint();
		

        // 색상 변경 버튼 추가
		colorButton.setVisible(false);
		colorButton.setBounds(90, 638, 100, 30);
		colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // 색상 선택 다이얼로그 띄우기
                Color newColor = JColorChooser.showDialog(thisclass, "Choose Color", Color.BLACK);
                
                // 색상이 선택되었을 때만 색상 변경
                if (newColor != null) {
                    setColor(newColor);
                    sendColorToOtherClients(newColor);
                }
            }
        });
        getContentPane().add(colorButton);
		
		//제목
		title.setVisible(true);
		title.setBounds(340, 100, 700, 120);
		getContentPane().add(title);
		
		name.setVisible(true);
		name.setText("");
		name.setBounds(500,220,100,30);
		name.setOpaque(false);
		name.setFont(new Font("맑은 고딕",Font.BOLD,15));
		name.setForeground(Color.WHITE);
		name.requestFocus();
		getContentPane().add(name);
		
		password.setVisible(true);
		password.setText("");
		password.setBounds(500,250,100,30);
		password.setOpaque(false);
		password.setFont(new Font("맑은 고딕",Font.BOLD,15));
		password.setForeground(Color.WHITE);
		password.requestFocus();
		getContentPane().add(password);
		
		ServerIP.setVisible(true);
		ServerIP.setBounds(0,0,100,30);
		getContentPane().add(ServerIP);
		
		loginButton.setVisible(true);
		loginButton.setBounds(600, 220, 70, 60);
		loginButton.addActionListener(new ActionListener() {
			//Process after clicked LoginButton
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String S_IP = ServerIP.getText();
				try {
					//server connect try
					if(socket == null||socket.toString().equals("Socket[unconnected]")) {
						socket = new Socket();
						socket.connect(new InetSocketAddress(S_IP, Main.Port));
						pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
						br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
					}
		            System.out.println(socket);
					
		            //this is login process
		            //send my nickname and join to ruby chat room
		            pw.println("login");
		            String request = "join:" + "0" +"\r\n";
		            System.out.println(name.getText());
		            pw.println(name.getText());
		            pw.println(request);
		            nickname=name.getText();
		            myInfo.setText(nickname);
		            
					JOptionPane.showMessageDialog(null, "대기실로 이동합니다", "서버 접속 완료", JOptionPane.INFORMATION_MESSAGE);
					
					goLoby();
					
					new ArtClueReceiver(socket,thisclass).start();
				}
				catch(Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "서버로 부터 반응이 없습니다.\n다시 시도해주세요.", "메롱", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		getContentPane().add(loginButton);
		
		//////////// LobyButton From Here
		////////////
		////////////
		lobyInfo.setVisible(false);
		lobyInfo.setText("?");
		lobyInfo.setBounds(600,70,100,30);
		lobyInfo.setFont(new Font("맑은 고딕",Font.BOLD,30));
		lobyInfo.setForeground(Color.WHITE);
		getContentPane().add(lobyInfo);
		
		lobyInfototal.setVisible(false);
		lobyInfototal.setText("?");
		lobyInfototal.setBounds(600,20,100,30);
		lobyInfototal.setFont(new Font("맑은 고딕",Font.BOLD,30));
		lobyInfototal.setForeground(Color.WHITE);
		getContentPane().add(lobyInfototal);
		
		chatInput.setVisible(false);
		chatInput.setBounds(880,689,400,30);
		chatInput.setOpaque(false);
		chatInput.setFont(new Font("맑은 고딕",Font.BOLD,15));
		chatInput.setForeground(Color.WHITE);
		chatInput.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				char keyCode = e.getKeyChar();
                if (keyCode == KeyEvent.VK_ENTER) {
                	if(chatInput.getText().length()>50) {
                		chatInput.setText(chatInput.getText().substring(0, 50));
                	}
                    sendMessage();
                }
			}
		});
		getContentPane().add(chatInput);
		
		
		chatArea.setBounds(880,150,400,570);
		chatArea.setOpaque(false);
		chatArea.setFont(new Font("맑은 고딕",Font.BOLD,15));
		chatArea.setForeground(Color.WHITE);
		chatArea.setText("");
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		
		chatSP.setOpaque(false);
		chatSP.getViewport().setOpaque(false);
		chatSP.setVisible(false);
		chatSP.setBounds(880, 120, 400, 570);
		chatSP.getVerticalScrollBar().setValue(chatSP.getVerticalScrollBar().getMaximum());
		getContentPane().add(chatSP);

		for(i=0;i<4;i++) {
			Room[i] = new JButton(new ImageIcon(Main.class.getResource("/Image/Room"+Integer.toString(i+1)+".png")));
			Room[i].setVisible(false);
			Room[i].setBorderPainted(false);
			Room[i].setContentAreaFilled(false);
			Room[i].setFocusPainted(false);
			Room[i].addActionListener(new ActionListener() {
				int temp = i+1;
				public void actionPerformed(ActionEvent arg0) {	
					pw.println("quit:" + WhereIAm);
					pw.println("join:" + Integer.toString(temp) + ":" + nickname);
					WhereIAm = temp;
					Roomnum.setText(Integer.toString(WhereIAm));
					goGame();
					
					//drawing board show
					iDrawing=createImage(550,490);
					gDrawing=iDrawing.getGraphics();
					gDrawing.setColor(Color.WHITE);
					gDrawing.fillRect(0, 0, 550, 490);
					repaint();
					
				}
			});
			getContentPane().add(Room[i]);
		}
		Room[0].setBounds(110,250,300,150);
		Room[1].setBounds(450,250,300,150);
		Room[2].setBounds(110,440,300,150);
		Room[3].setBounds(450,440,300,150);
		
		myInfo.setBounds(900,42,300,50);
		myInfo.setVisible(false);
		myInfo.setFont(new Font("맑은 고딕",Font.BOLD,40));
		myInfo.setForeground(Color.WHITE);
		getContentPane().add(myInfo);
		
		//gameroom
		/////////////////////////
		//////////////////////////
		Roomnum.setVisible(false);
		Roomnum.setBounds(500,10,100,40);
		Roomnum.setFont(new Font("맑은 고딕",Font.BOLD,30));
		Roomnum.setForeground(Color.WHITE);
		getContentPane().add(Roomnum);
		
		Roompeople.setVisible(false);
		Roompeople.setBounds(500,55,100,40);
		Roompeople.setFont(new Font("맑은 고딕",Font.BOLD,30));
		Roompeople.setForeground(Color.WHITE);
		Roompeople.setText("?");
		getContentPane().add(Roompeople);
		
		DrawArea.setVisible(false);
		DrawArea.setOpaque(false);
		DrawArea.setBounds(25,146,550,490);
		DrawArea.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent me) {
				x = me.getX();
				y = me.getY();
			}
			public void mouseDragged(MouseEvent me) {
				if (me.getModifiersEx() != MouseEvent.BUTTON1_DOWN_MASK)
					return;
				String msg = "draw:"+x+":"+y+":"+me.getX()+":"+me.getY()+":"+WhereIAm;
				pw.println(msg);
				//System.out.println(msg);

				x = me.getX();
				y = me.getY();
				//drawing is not allowed in client self
			}
		});
		getContentPane().add(DrawArea);
		
		GoBack.setVisible(false);
		GoBack.setBounds(1120, 20, 64, 64);
		GoBack.setBorderPainted(false);
		GoBack.setContentAreaFilled(false);
		GoBack.setFocusPainted(false);
		GoBack.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				pw.println("quit:"+WhereIAm);
				pw.println("join:0");
				WhereIAm=0;
				goLoby();
			}
		});
		getContentPane().add(GoBack);

		Start.setBounds(550,10, 100, 30);
		Start.setVisible(false);
		Start.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
		        System.out.println("Start button clicked!");
				pw.println("rungame:"+WhereIAm);
		        pw.println("answer:" + chatInput.getText() + ":" + WhereIAm);

			}
		});
		getContentPane().add(Start);

	
		Erase.setVisible(false);
		Erase.setBounds(25,636, 60,60);
		Erase.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				pw.println("draw:erase:" + WhereIAm);
				pw.flush();
			}
		});
		getContentPane().add(Erase);
		
		Status.setVisible(false);
		Status.setFont(new Font("맑은 고딕",Font.BOLD,40));
		Status.setForeground(Color.WHITE);
		Status.setBounds(660, 50, 200, 50);
		getContentPane().add(Status);
		
		//exit button
		exit.setVisible(true);
		exit.setBounds(1200, 20, 64, 64);
		exit.setBorderPainted(false);
		exit.setContentAreaFilled(false);
		exit.setFocusPainted(false);
		exit.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				try {
					pw.println("quit:"+WhereIAm);
				}
				catch(Exception e1) {
					
				}
				System.exit(0);
			}
		});
		getContentPane().add(exit);
	}

    // 메소드 추가: 색상 변경 메소드
    public void setColor(Color color) {
        this.newColor = color;
        gDrawing.setColor(color);
        repaint();
    }
    public void sendColorToOtherClients(Color color) {
        // 클라이언트들에게 색상 변경 정보를 전송하는 로직 추가
        pw.println("color:" + color.getRGB() + ":" + WhereIAm);
    }
    
	public void goLoby(){
		//main memver invisible
		title.setVisible(false);
		name.setVisible(false);
		ServerIP.setVisible(false);
		loginButton.setVisible(false);
		password.setVisible(false);
		colorButton.setVisible(false);
		
		//ruby member visible
		lobyInfo.setVisible(true);
		lobyInfototal.setVisible(true);
		chatInput.setVisible(true);
		chatSP.setVisible(true);
		myInfo.setVisible(true);
		for(int i=0;i<4;i++)
			Room[i].setVisible(true);
		
		//game member invisible
		Roomnum.setVisible(false);
		Roompeople.setVisible(false);
		GoBack.setVisible(false);
		Erase.setVisible(false);
		DrawArea.setVisible(false);
		Start.setVisible(false);
		Status.setVisible(false);
		
		//change backGround
		BackGroundImage = new ImageIcon(Main.class.getResource("/Image/LobyImage.png")).getImage();
	}
	
	public void goGame() {
		//main memver invisible
		title.setVisible(false);
		name.setVisible(false);
		ServerIP.setVisible(false);
		loginButton.setVisible(false);
		password.setVisible(false);

		
		//ruby member invisible
		lobyInfo.setVisible(false);
		lobyInfototal.setVisible(false);
		chatInput.setVisible(true);
		chatSP.setVisible(true);
		myInfo.setVisible(true);

		for(int i=0;i<4;i++) 
			Room[i].setVisible(false);
		
		//game member visible
		Roomnum.setVisible(true);
		Roompeople.setVisible(true);
		GoBack.setVisible(true);
		DrawArea.setVisible(true);
		Erase.setVisible(true);
		Start.setVisible(true);
		Status.setVisible(true);
		colorButton.setVisible(true);

		
		//change backGround
		BackGroundImage = new ImageIcon(Main.class.getResource("/Image/InGameve2.png")).getImage();
	}
	
	public void sendMessage() {
		try {
			String request = "message:"+chatInput.getText()+":"+Integer.toString(WhereIAm);
			pw.println(request);
	        //pw.flush(); // 추가된 부분

			
			chatInput.setText("");
			chatInput.requestFocus();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void refreshInfo(String info[]) {
		int total = 0;
		for(int i=1;i<6;i++) {
			total+=Integer.parseInt(info[i]);
		}
		lobyInfo.setText(info[1]);
		lobyInfototal.setText(Integer.toString(total));
		
		Roompeople.setText(info[WhereIAm+1]);
	}
	
	public void paint(Graphics g) {
		screenImage = createImage(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		screenGraphic = screenImage.getGraphics();
		screenDraw(screenGraphic);
		g.drawImage(screenImage, 0, 0, null);
	}
	public void screenDraw(Graphics g) {
		g.drawImage(BackGroundImage, 0, 0, null);
		if(WhereIAm!=0) {
			g.drawImage(iDrawing,25,146,null);
		}
		paintComponents(g);
		this.repaint();
	}
}