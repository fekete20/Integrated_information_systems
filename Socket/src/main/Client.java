package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	Socket requestSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message;

	Client() {
	}

	boolean cont;

	void run() {
		try {
			// 1. socket kapcsolat létrehozása
			requestSocket = new Socket("localhost", 8080);
			// 2. Input and Output streamek
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			in = new ObjectInputStream(requestSocket.getInputStream());
			// 3: Kommunikáció
			do {
				try {
					// fájllista fogadása
					System.out.println((String) in.readObject());

					// írás vagy olvasás?
					System.out.println((String) in.readObject());
					Scanner scanner = new Scanner(System.in);
					String op = scanner.next();
					sendMessage(op); // billentyû elmegy a szervernek
					if (op.equals("u")) { // a kliens oldali állományt elküldjük a szervernek
						// fájlnév bekérése
						System.out.println((String) in.readObject());
						String filename = scanner.next();
						sendMessage(filename);
						// tartalom bekérése
						System.out.println((String) in.readObject());
						String content = scanner.next();
						writeToLocalFile(filename, content);
						sendMessage(upload(filename)); // fájl tartalmának elküldése a szervernek
						System.out.println((String) in.readObject()); // sikeres volt-e a feltöltés
					} else if (op.equals("d")) { // létrehozzuk a fájlt a kliens oldalon és beírjuk a szerverrõl kapott
													// adatokat

						// fájlnév bekérése
						System.out.println((String) in.readObject());
						String filename = scanner.next();
						sendMessage(filename);

						// kontent kiírása
						String content = (String) in.readObject();
						System.out.println(content);
						writeToLocalFile(filename, content);
					} else {
						System.err.println("Invalid operation.");
					}

					System.out.println((String) in.readObject());
					String c = scanner.next();
					if (c.equals("y")) {
						sendMessage("y");
						cont = true;
					} else {
						scanner.close();
						cont = false;
						sendMessage("n");
					}
				} catch (Exception e) {
					System.err.println("data received in unknown format");
				}
			} while (cont);
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// 4: Kapcsolat zárása
			try {
				in.close();
				out.close();
				requestSocket.close();

			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("client>" + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Client client = new Client();
		client.run();
	}

	String upload(String filename) {
		File file = new File("files/" + filename);
		Scanner scanner;
		String content = "";
		try {
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				content += scanner.nextLine() + "\n";
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	void writeToLocalFile(String filename, String text) {
		File file = new File("files/" + filename);
		try {
			if (file.createNewFile()) {
				FileWriter fileWriter = new FileWriter("files/" + filename);
				fileWriter.write(text);
				fileWriter.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}