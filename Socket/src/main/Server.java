package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
	ServerSocket providerSocket;
	Socket connection = null;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message;

	Server() {
	}

	boolean cont;

	void run() {
		try {
			// 1. szerver socket létrehozása
			providerSocket = new ServerSocket(8080, 10);
			// 2. kapcsolódásra várakozás
			connection = providerSocket.accept();
			// 3. Input és Output streamek megadása
			out = new ObjectOutputStream(connection.getOutputStream());
			in = new ObjectInputStream(connection.getInputStream());
			// 4. socket kommunikáció
			do {
				try {
					// fájlok listázása
					sendMessage("Files in the store directory: \n" + readFiles());

					// klienstõl fogadjuk, hogy u vagy d mûvelet lesz
					sendMessage("Download or upload? (d/u)");
					message = (String) in.readObject();
					System.out.println("client>" + message);

					if (message.equals("u")) { // nyitunk egy új fájlt és a klienstõl kapott adatokat kiírjuk a fájlba
						sendMessage("Please enter the filename with extension: ");
						String filename = message = (String) in.readObject();
						System.out.println("client>" + message);
						sendMessage("Please enter the content of the file: ");
						String text = message = (String) in.readObject();
						System.out.println("client>" + message);
						upload(filename, text);
						sendMessage("Uploaded.");
					}

					else if (message.equals("d")) { // felolvassuk a fájl tartalmát és elküldjük a kliensnek
						sendMessage("Please enter the filename with extension: ");
						String filename = message = (String) in.readObject();
						System.out.println("client>" + message);
						sendMessage(download(filename));
					} else {
						System.out.println("Invalid operation.");
					}

					sendMessage("Do you want to continue? (y/n)");
					message = (String) in.readObject();
					System.out.println("client>" + message);

					if (message.equals("y")) {
						cont = true;

					} else {
						cont = false;
					}
				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				}
			} while (cont);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// 4: kapcsolat lezárása
			try {
				in.close();
				out.close();
				providerSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("server>" + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Server server = new Server();
		while (true) {
			server.run();
		}
	}

	String readFiles() {
		File directory = new File("store");
		String files = "";
		for (File fileList : directory.listFiles()) {
			files += fileList + "\n";
		}
		return files;
	}

	void upload(String filename, String text) {
		File file = new File("store/" + filename);
		try {
			if (file.createNewFile()) {
				FileWriter fileWriter = new FileWriter("store/" + filename);
				fileWriter.write(text);
				fileWriter.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	String download(String filename) {
		File file = new File("store/" + filename);
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
}