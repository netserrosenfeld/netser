
import java.awt.Button;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CustomHttpServer {
	static JFrame frame1;
	static Container pane;
	static JButton btnConnect, btnDisconnect;
	static JLabel lblPort, lblError;
	static JTextArea  txtError;
	static JTextField txtPort;
	 
	static Insets insets;
	public static void buildFrame(){
		frame1 = new JFrame ("Sample GUI Application");
		//Set its size to 800x200 pixels
		frame1.setSize (800,200);
		//Prepare panel
		pane = frame1.getContentPane();
		insets = pane.getInsets();
		//Apply the null layout
		pane.setLayout (null);
		
		btnConnect = new JButton ("Connect");
		btnDisconnect = new JButton ("Disconnect");
		lblPort = new JLabel ("Port #:");
		lblError = new JLabel ("Errors:");
		
		txtPort = new JTextField (10);
		txtPort.setText( "8000");
		txtError = new JTextArea  (20,20);
		
		pane.add (lblPort); //Add component to panel
		lblPort.setBounds (insets.left + 5, insets.top + 5, lblPort.getPreferredSize().width, lblPort.getPreferredSize().height);
		frame1.setVisible (true);
		//Add all components to panel
		pane.add (lblPort);
		pane.add (lblError);
		
		pane.add (txtPort);
		pane.add (txtError);
		
		
		pane.add (btnConnect);
		pane.add (btnDisconnect);

		//Place all components
		lblPort.setBounds (insets.left + 5, insets.top + 5, lblPort.getPreferredSize().width, lblPort.getPreferredSize().height);
		txtPort.setBounds (lblPort.getX() + lblPort.getWidth() + 5, insets.top + 5, txtPort.getPreferredSize().width, txtPort.getPreferredSize().height);

		lblError.setBounds (txtPort.getX() + txtPort.getWidth() + 5, insets.top + 5, lblError.getPreferredSize().width, lblError.getPreferredSize().height);
		txtError.setBounds (lblError.getX() + lblError.getWidth() + 5, insets.top + 5, txtError.getPreferredSize().width, txtError.getPreferredSize().height);

		
		btnConnect.setBounds (txtError.getX() + txtError.getWidth() + 5, insets.top + 5, btnConnect.getPreferredSize().width, btnConnect.getPreferredSize().height);

		//Place disconnect button (start a new line!)
		btnDisconnect.setBounds (insets.left + 15, lblPort.getY() + lblPort.getHeight() + 5, btnDisconnect.getPreferredSize().width, btnDisconnect.getPreferredSize().height);
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (ClassNotFoundException e) {}
		catch (InstantiationException e) {}
		catch (IllegalAccessException e) {}
		catch (UnsupportedLookAndFeelException e) {}
		btnConnect.addActionListener(new btnConnectAction()); //Register action
		btnDisconnect.addActionListener(new btnDisconnectAction()); 
	}
	
	;
	
	public static class btnConnectAction implements ActionListener{
		public void actionPerformed (ActionEvent e){
			
			txtError.setText("in");//clean the error message
			int port=Integer.parseInt( txtPort.getText());
			 HttpServer server =null;
			try{
				txtError.setText("inside");
				 server = HttpServer.create(new InetSocketAddress(port), 0);
				 txtError.setText("2");
			// create custom handler per request
				 try{
					 server.createContext("/students/add", new SetDataHandler());
					 txtError.setText("/students/add");
				 }catch(Exception w){
					 txtError.setText("failed");
				 }
			server.createContext("/students/remove", new RemoveHandler());
			server.createContext("/students/getData", new GetDataHandler());
			server.createContext("/students/getAllStudents", new GetAllStudentsHandler());
			 txtError.setText("3");
			thread = Executors.newFixedThreadPool(1);
			server.setExecutor(thread); // creates a default executor
			txtError.setText("3");
			server.start();
			txtError.setText("Connect sussefully");
			}
			
			catch(Exception ee){
				txtError.setText(ee.getMessage());
				try (FileWriter file = new FileWriter(System.getProperty("user.dir") + "/errorLogs.txt")) {
					file.append(ee.getMessage());

				} catch (IOException er) {
					// TODO Auto-generated catch block
					er.printStackTrace();
				}
			}
		}
	}
	static ExecutorService thread;
	public static class btnDisconnectAction implements ActionListener{
		public void actionPerformed (ActionEvent e){
			try{
				//server.stop(0);
				thread.shutdownNow();
				txtError.setText("Disconnect sussefully");
			}catch(Exception err){
				txtError.setText(err.getMessage());
			}
			
		}
		}
	public static void main(String[] args) throws Exception {
		
		buildFrame();
		
		
		
	}

	static class GetDataHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			String response = "No data";
			String JsonText = helper.ReadJsonObjectFromStorage();

			// get the data from the query string of the request
			String queryString = t.getRequestURI().getQuery();
			Map<String, String> queryMap = helper.queryToMap(queryString);

			String studentID = queryMap.get("id");
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();

			try {
				json = (JSONObject) parser.parse(JsonText);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray studentDetails = (JSONArray) json.get(studentID);
			if (studentDetails != null) {
				response = studentDetails.toString();
			} else {
				response = "The student does not exist";
			}
			helper.sendResponse(response, t);

		}
	}

	static class GetAllStudentsHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			String response = "No data";
			String JsonText = helper.ReadJsonObjectFromStorage();

			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();

			try {
				json = (JSONObject) parser.parse(JsonText);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (json != null) {
				response = json.toString();
			} else {
				response = "0";
			}
			helper.sendResponse(response, t);

		}
	}

	static class SetDataHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {

			String response = "No data";
			String JsonText = helper.ReadJsonObjectFromStorage();

			// get the data from the query string of the request into Map
			String queryString = t.getRequestURI().getQuery();
			Map<String, String> queryMap = helper.queryToMap(queryString);

			String studentID = queryMap.get("id");

			final int Max_Student = 1000;
			if (studentID != null) {

				String name = queryMap.get("name");
				String gender = queryMap.get("gender");
				String grade = queryMap.get("grade");

				// create object of the student details
				JSONArray studentDetailsNew = new JSONArray();
				studentDetailsNew.add("name:" + name);
				studentDetailsNew.add("gender:" + gender);
				studentDetailsNew.add("grade:" + grade);

				JSONParser parser = new JSONParser();
				JSONObject json = new JSONObject();
				JSONArray studentDetailsArr = new JSONArray();
				int counter = 0;
				try {
					if (!JsonText.isEmpty()) {
						json = (JSONObject) parser.parse(JsonText);
						counter = Integer.parseInt(json.get("counter").toString());
						studentDetailsArr = (JSONArray) json.get(studentID);

					}

					if (studentDetailsArr != null && !studentDetailsArr.isEmpty()) {
						// update the student
						json.replace(studentID, studentDetailsArr, studentDetailsNew);
					} else {
						if (counter >= Max_Student) {
							response = "Not Enough Storage";
							helper.sendResponse(response, t);

						} else {
							// add new student
							json.put(studentID, studentDetailsNew);

							// append another student to the counter
							counter++;
							json.put("counter", counter);
						}
					}

					helper.WriteToFile(json.toString());

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				response = "The data was updated";
			} else {
				response = "Student id is missing!";
			}

			helper.sendResponse(response, t);

		}
	}

	static class RemoveHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {

			String JsonText = helper.ReadJsonObjectFromStorage();
			String queryString = t.getRequestURI().getQuery();
			Map<String, String> queryMap = helper.queryToMap(queryString);
			String studentID = queryMap.get("id");
			String response = "student Id required";
			if (!queryString.contains("id=undefined")) {
				JSONParser parser = new JSONParser();
				try {
					JSONObject json = (JSONObject) parser.parse(JsonText);
					int counter = Integer.parseInt(json.get("counter").toString());
					// delete the details of the student
					json.remove(studentID);

					// reduce the counter and update it in the json file
					counter--;
					json.put("counter", counter);

					helper.WriteToFile(json.toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// get student data

				response = "The student was delete successfully";
			}
			helper.sendResponse(response, t);
		}
	}

	public static class helper {
		private static Map<String, String> queryToMap(String query) {
			Map<String, String> result = new HashMap<String, String>();
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				if (pair.length > 1) {
					result.put(pair[0], pair[1]);
				} else {
					result.put(pair[0], "");
				}
			}
			return result;
		}

		private static String ReadJsonObjectFromStorage() throws IOException {

			File file = new File(System.getProperty("user.dir") + "/studentsStorage.txt");

			if (!file.exists()) {
				file.createNewFile(); // creating it
			}

			BufferedReader br = new BufferedReader(
					new FileReader(System.getProperty("user.dir") + "/studentsStorage.txt"));

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String FileText = sb.toString();

			return FileText;

		}

		private static void WriteToFile(String text) {

			try (FileWriter file = new FileWriter(System.getProperty("user.dir") + "/studentsStorage.txt")) {
				file.write(text);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private static void sendResponse(String response, HttpExchange t) {

			try {
				// for allow request per cross domain
				Headers headers = t.getResponseHeaders();
				headers.add("Access-Control-Allow-Headers", "x-prototype-version,x-requested-with");
				headers.add("Access-Control-Allow-Methods", "GET,POST");
				headers.add("Access-Control-Allow-Origin", "*");

				// send the response to the client
				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();

			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}
}
