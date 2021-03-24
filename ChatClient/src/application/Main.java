package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;


public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	
	// 프로그램 동작 
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP,port);
					receive();
				}catch(Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();
					}
					e.getStackTrace();
				}
			}
		};
		thread.start();
	}
	
	//프로그램 종료 
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//서버로 부터 메세지를 전달
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] bf = new byte[512];
				int lh = in.read(bf);
				if(lh == -1) {
					throw new IOException();
				}
				String message = new String(bf,0,lh,"UTF-8");
				Platform.runLater(()-> {
					textArea.appendText(message);
				});
			}catch(Exception e) {
				stopClient();
				break;
			}
		}
			
	}
	
	//서버로 메시지를 전송
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] bf = message.getBytes("UTF-8");
					out.write(bf);
					out.flush();
				}catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	//실행
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입려하세요");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField();
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName,IPText,portText);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(evect ->{
			send(userName.getText() + " : " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText() + " : " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button conbutton = new Button("접속하기");
		conbutton.setOnAction(event ->{
			if(conbutton.getText().equals("접속하기")) {
				int port = 9876;
				try {
					
				} catch(Exception e) {
					e.printStackTrace();
				}
				startClient(IPText.getText(), port);
				Platform.runLater(()->{
					textArea.appendText("[채팅방 접속]\n");
				});
				conbutton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(()-> {
					textArea.appendText("[채팅방 퇴장]\n");
				});
				conbutton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);
				
			}
			
		});
		
		BorderPane pane = new BorderPane();
		pane.setLeft(conbutton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		Scene scene = new Scene(root,400,400);
		primaryStage.setTitle("[채팅 클라이언트]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		conbutton.requestFocus();
		
		
	}
	
	//프로그램 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
