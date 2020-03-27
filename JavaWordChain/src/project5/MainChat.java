package project5;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class MainChat extends JFrame implements ActionListener, Runnable {

	JList<String> roomInfo, roomInwon, waitInfo;
	JScrollPane sp_roomInfo, sp_roomInwon, sp_waitInfo;
	JButton bt_create, bt_enter, bt_exit;
	JPanel p;
	ChatClient cc;
	String nickName;
	// 소켓 입출력객체
	BufferedReader in;
	OutputStream out;
	String selectedRoom;
	int turn = 0;
	boolean duplicated;
	Vector<String> used = new Vector<String>();
	Timer timer;
	int time = 10;
	public MainChat() {
		setTitle("대기실");
		cc = new ChatClient();
		roomInfo = new JList<String>();
		roomInfo.setBorder(new TitledBorder("방정보"));
		roomInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				String str = roomInfo.getSelectedValue(); // "자바방--1"
				if (str == null)
					return;
				System.out.println("방정보=" + str);
				selectedRoom = str.substring(0, str.indexOf("-"));
				// "자바방" <---- substring(0,3)
				// 대화방 내의 인원정보
				sendMsg("170|" + selectedRoom);
			}

		});

		roomInwon = new JList<String>();
		roomInwon.setBorder(new TitledBorder("인원정보"));
		waitInfo = new JList<String>();
		waitInfo.setBorder(new TitledBorder("대기실정보"));

		sp_roomInfo = new JScrollPane(roomInfo);
		sp_roomInwon = new JScrollPane(roomInwon);
		sp_waitInfo = new JScrollPane(waitInfo);

		bt_create = new JButton("방만들기");
		bt_enter = new JButton("방들어가기");
		bt_exit = new JButton("나가기");

		p = new JPanel();

		sp_roomInfo.setBounds(10, 10, 300, 300);
		sp_roomInwon.setBounds(320, 10, 150, 300);
		sp_waitInfo.setBounds(10, 320, 300, 130);

		bt_create.setBounds(320, 330, 150, 30);
		bt_enter.setBounds(320, 370, 150, 30);
		bt_exit.setBounds(320, 410, 150, 30);

		p.setLayout(null);
		p.setBackground(Color.orange);
		p.add(sp_roomInfo);
		p.add(sp_roomInwon);
		p.add(sp_waitInfo);
		p.add(bt_create);
		p.add(bt_enter);
		p.add(bt_exit);

		add(p);
		setBounds(300, 200, 500, 500);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		connect();// 서버연결시도 (in,out객체생성)
		new Thread(this).start();// 서버메시지 대기
		sendMsg("100|");// (대기실)접속 알림
		nickName = JOptionPane.showInputDialog(this, "대화명:");
		sendMsg("150|" + nickName);// 대화명 전달

		eventUp();

	}// 생성자

	private void eventUp() {// 이벤트소스-이벤트처리부 연결

		// 대기실(MainChat)

		bt_create.addActionListener(this);
		bt_enter.addActionListener(this);
		bt_exit.addActionListener(this);

		// 대화방(ChatClient)
		cc.sendTF.addActionListener(this);
		cc.bt_ready.addActionListener(this);
		cc.bt_start.addActionListener(this);
		cc.bt_exit.addActionListener(this);

	}

	@Override

	public void actionPerformed(ActionEvent e) {

		Object ob = e.getSource();

		if (ob == bt_create) {// 방만들기 요청

			String title = JOptionPane.showInputDialog(this, "방제목:");
			// 방제목을 서버에게 전달
			sendMsg("160|" + title);
			cc.setTitle("채팅방-[" + title + "]");
			sendMsg("175|");// 대화방내 인원정보 요청

			setVisible(false);
			cc.setVisible(true); // 대화방이동
		} else if (ob == bt_enter) {// 방들어가기 요청

			if (selectedRoom == null) {
				JOptionPane.showMessageDialog(this, "방을 선택!!");
				return;
			}
			sendMsg("200|" + selectedRoom);

			sendMsg("175|");// 대화방내 인원정보 요청

			setVisible(false);

			cc.setVisible(true);

		} else if (ob == cc.bt_exit) {// 대화방 나가기 요청

			sendMsg("400|");
			cc.setVisible(false);
			cc.ta.selectAll();
		    cc.ta.replaceSelection("");
			setVisible(true);
			used.clear(); //중복단어 리스트 초기화

		} else if (ob == cc.sendTF) {// (TextField입력)메시지 보내기 요청

			String msg = cc.sendTF.getText();

			if (turn == 1 && msg.length() > 0) {
				if(wordCheck(msg)==false) {
					cc.ta.append("올바르지 않은 단어입니다.\n");
					cc.ta.setCaretPosition(cc.ta.getText().length());	
				}
				else {
					if (msg.length() > 0) {
						sendMsg("301|" + msg); //정답
						cc.sendTF.setText("");
					}
					sendMsg("score|" + msg.length()*10);
					cc.sendTF.setText("");
					timer.cancel();
				}
			}
			else {
				if (msg.length() > 0) { 
					sendMsg("300|" + msg);
					cc.sendTF.setText("");
				}
			}
		} else if (ob == cc.bt_ready) {
			sendMsg("ready|");
		}

		else if (ob == cc.bt_start) {
			sendMsg("start|");
		}

		else if (ob == bt_exit) {// 나가기(프로그램종료) 요청

			System.exit(0);// 현재 응용프로그램 종료하기

		}

	}// actionPerformed

	public void connect() {// (소켓)서버연결 요청

		try {

			// Socket s = new Socket(String host<서버ip>, int port<서비스번호>);

			Socket s = new Socket("localhost", 5000);// 연결시도

			in = new BufferedReader(new InputStreamReader(s.getInputStream()));

			// in: 서버메시지 읽기객체 서버-----msg------>클라이언트

			out = s.getOutputStream();

			// out: 메시지 보내기, 쓰기객체 클라이언트-----msg----->서버

		} catch (UnknownHostException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}// connect
	
	
	public boolean wordCheck(String s) {
		if (used.contains(s)) {
			return false;
		}
		if(!used.isEmpty() && used.lastElement().charAt(used.lastElement().length()-1) != s.charAt(0)) {
	         return false;
	      }
		Connection conn = null;
        Statement stmt = null;
        //연결할 DBMS의 주소 (디비명까지);
        String dataBase = "s18057"; //디비명
        String url = "jdbc:mysql://dev.gsa.hs.kr:5060/" + dataBase + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        //접속할 mysql id와 password 입력.
        String id = "s18057"; //기본적으로 아이디는 root
        String pw = "1111"; //비번
        
        try {
            //클레스를 로딩 함.. 최신버전 자바에서는 생략 가능
            Class.forName("com.mysql.cj.jdbc.Driver"); 
            //DB와 연결을 갖는 Connection객체 획득
            conn = DriverManager.getConnection(url,id,pw);  
            //DB와 연결된 conn객체로부터 구문객체를 획득
            stmt = conn.createStatement();
            
            //쿼리 만들기
            StringBuilder sb = new StringBuilder();
            String sql = sb.append("create table if not exists wordtable(")    //if not exists = 존재하지 않을 때만 생성
                    .append("idx int,")
                    .append("word varchar(50),")
                    .append("len int")
                    .append(");").toString(); 
 
            //쿼리문 날리기
            stmt.execute(sql);
            
            // 구문객체를 던지는 3가지 방법
            //1. execute -> 테이블 생성, 수정,삭제 등 데이터베이스 관리 명령어 사용
            //2. excuteUpdate -> 레코드 삽입 수정 삭제등 데이터 조작 명령어 사용
            //3. excuteQuery -> 레코드 조회, 테이블 조회 등 조회 명령어 사용
            StringBuilder sb2 = new StringBuilder();
            String sql2 = sb2.append("select * from wordtable where")
                    .append(" word = \"")
                    .append(s)
                    .append("\";").toString();
            try {
                ResultSet rs = stmt.executeQuery(sql2);
                rs.last();
                System.out.println(rs.getRow());
                if(rs.getRow()==0) {
                	return false;
                }
                else {
                	return true;
                }
                
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {

            try {
                //사용한 connection 닫기
                if(conn != null && !conn.isClosed())
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
	}
	public void sendMsg(String msg) {// 서버에게 메시지 보내기

		try {
			out.write((msg + "\n").getBytes());

		} catch (IOException e) {

			e.printStackTrace();

		}

	}// sendMsg

	public void run() {// 서버가 보낸 메시지 읽기

		// 왜 run메소드 사용? GUI프로그램실행에 영향 미치지않는 코드 작성.

//메소드호출은 순차적인 실행!!  스레드메소드는 동시실행(기다리지 않는 별도 실행)!!

		try {

			while (true) {

				String msg = in.readLine();// msg: 서버가 보낸 메시지

				// msg==> "300|안녕하세요" "160|자바방--1,오라클방--1,JDBC방--1"

				String msgs[] = msg.split("\\|");

				String protocol = msgs[0];

				switch (protocol) {

				case "300":

					cc.ta.append(msgs[1] + "\n");

					cc.ta.setCaretPosition(cc.ta.getText().length());

					break;

				case "160":// 방만들기

					// 방정보를 List에 뿌리기

					if (msgs.length > 1) {

						// 개설된 방이 한개 이상이었을때 실행

						// 개설된 방없음 ----> msg="160|" 였을때 에러

						String roomNames[] = msgs[1].split(",");

						// "자바방--1,오라클방--1,JDBC방--1"

						roomInfo.setListData(roomNames);

					}

					break;

				case "170":// (대기실에서) 대화방 인원정보
					String roomInwons[] = msgs[1].split(",");
					roomInwon.setListData(roomInwons);
					break;

				case "175":// (대화방에서) 대화방 인원정보

					String myRoomInwons[] = msgs[1].split(",");

					
					cc.li_inwon.setListData(myRoomInwons);

					break;

				case "180":// 대기실 인원정보

					String waitNames[] = msgs[1].split(",");

					waitInfo.setListData(waitNames);

					break;

				case "200":// 대화방 입장

					cc.ta.append("=========[" + msgs[1] + "]님 입장=========\n");

					cc.ta.setCaretPosition(cc.ta.getText().length());

					break;

				case "400":// 대화방 퇴장

					cc.ta.append("=========[" + msgs[1] + "]님 퇴장=========\n");

					cc.ta.setCaretPosition(cc.ta.getText().length());

					break;

				case "202":// 개설된 방의 타이틀 제목 얻기
					cc.setTitle("채팅방-[" + msgs[1] + "]");
					break;
				case "debug":
					System.out.println(msgs[1]);
					break;
				case "turn":
					turn = Integer.parseInt(msgs[1]);
					cc.bt_ready.setText("10");
					
					if (turn == 1) {
						cc.ta.append("----My turn----\n");
						cc.ta.setCaretPosition(cc.ta.getText().length());		
						cc.bt_start.setText("내차례!");
						time = 10;
						 timer = new Timer(true);
		                  Task task = new Task();
		                  timer.schedule(task, 1000, 1000);

						
						
						
						
						
					}
					else {
						cc.bt_start.setText("내차례아님");
					}
					break;
				case "usedword":
					used.add(msgs[1]);
					break;
				case "start":
					cc.bt_start.setText("내차례아님");
					cc.bt_ready.setText(" ");
					//cc.bt_ready.setEnabled(false);
					cc.bt_start.setEnabled(false);
					
					cc.bt_exit.setEnabled(false);
					
					break;
				case "btchange":
					System.out.println(msgs[1].getClass());
					if (msgs[1].equals("1")) {
						cc.bt_ready.setText("준비완료");
						cc.bt_ready.setBackground(Color.YELLOW);
						System.out.println("ready");
					} else {
						cc.bt_ready.setText("준비");
						cc.bt_ready.setBackground(Color.WHITE);
						System.out.println("cancel");
					}
					cc.setVisible(true);
					break;
				}// 클라이언트 switch
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}// run

	public static void main(String[] args) {

		new MainChat();

	}
	class Task extends TimerTask{
	      public void run() {
	         time -= 1;
	         cc.bt_ready.setText(Integer.toString(time));
	         if (time == 0) {
	            cc.ta.append("시간초과!!\n");
	            cc.bt_ready.setText("시간초과!");
	            cc.ta.setCaretPosition(cc.ta.getText().length());   
	            sendMsg("score|0");
	            timer.cancel();
	         }
	      }
	   }

}

