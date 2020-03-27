package project5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
 
 
//java코드로 min디비에 접속해서
//student 테이블을 만듬
//create table student(
//    id int,
//    name varchar(20),
//    grade int
//);
//----- 디비 라이브러리 사용.
//Class 로딩
//DBMS의 특정 DB와 연결을 갖는 Connection객체 획득.
//내가 날릴 쿼리문을 품은 Statement 객체 생성.
//Connection 객체에 Statement구문객체를 전달.
 
 
public class CreateTableTest {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        //연결할 DBMS의 주소 (디비명까지);
        String dataBase = //디비명
        String url = "";
        //접속할 mysql id와 password 입력.
        String id = "";//기본적으로 아이디는 root
        String pw = ""; //비번
        
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
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
        	SqlTest test = new SqlTest(conn,"wordtable");
        	test.select("마우스");
            try {
                //사용한 connection 닫기
                if(conn != null && !conn.isClosed())
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        

    }
 
}
