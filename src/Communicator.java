import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

/**
 * Diese Klasse ist die MainKlasse <br>
 * sie steuert den ablauf des Programms je nachdem was der Benutzer aufruft<br>
 * Der Benutzer kann zwischen seinen IP´s entscheiden wenn er mehre Interfaces
 * hat <br>
 * dies funktioniert jedoch nur einmal und zwar am Anfang <br>
 * Ausserdem setzt man dann die IP des MessageBrokers und hat dann die
 * Moeglichkeit <br>
 * Nachrichten zu schreiben/empfangen oder in einen Chat zu joinen <br>
 * <br>
 * Nach dem festlegen der IP´s wird dem Benutzer gezeigt wie er etwas eingeben
 * soll <br>
 * wenn er das falsch macht passiert entweder garnichts fuer eine nicht bekannte
 * Eingabe <br>
 * oder eine Fehlermeldung fuer eine bekannte falsche Eingabe
 * 
 * @author Josef Sochovsky
 * @version 1.0
 */
public class Communicator {
	public static void main(String[] args) throws IOException {
		// Festlegen der IP des Benutzers
		InetAddress inet = InetAddress.getLocalHost();
		String myIP = inet.getHostAddress();
		System.out
				.println("Momentan ist dies ihre gespeicherte IP : "
						+ myIP
						+ " wollen sie diese aendern?\n geben sie die gewuenschte IP ein! \n Wenn nicht einfach enter druecken");
		String eingabe = (new BufferedReader(new InputStreamReader(System.in)))
				.readLine();
		if (!eingabe.equals(""))
			myIP = eingabe;

		// Festlegen der IP des Servers
		System.out
				.println("Bitte geben sie nun die IP des Messagebrokers ein!");
		String serverIP = (new BufferedReader(new InputStreamReader(System.in)))
				.readLine();
		// Anzeigen der Moeglichkeiten des Benutzers
		System.out
				.println("\n\n"
						+ "Sie koennen nun folgende Operationen durchfuehren: "
						+ "\n"
						+ "vsdbchat <benutzername> <chatroom> um in einen Chatroom zu gelangen"
						+ "\n"
						+ "SEND <ip_des_benutzers> um an einen beliebigen Benutzer eine Mail zu schreiben"
						+ "\n" + "MAILBOX um ihre eigenen Mails abzurufen");
		System.out
				.println("Mittels \"exit\" koennen sie die Applikation beenden");

		while (true) {
			eingabe = "";
			// einlesen des gewuenschten Verhaltens
			eingabe = (new BufferedReader(new InputStreamReader(System.in)))
					.readLine();
			try {
				// wenn der benutzer das Programm beenden moechte hat er exit
				// eingegeben
				if (eingabe.equals("exit")) {
					break;
				} else if (eingabe.split(" ")[0].equalsIgnoreCase("vsdbchat")) {
					// moeglichkeit Chat
					String[] eingaben = eingabe.split(" ");
					new Groupchat(eingaben[2], eingaben[1], myIP, serverIP);
				} else if (eingabe.split(" ")[0].equalsIgnoreCase("SEND")) {
					// moeglichkeit Nachrichtensenden
					String[] eingaben = eingabe.split(" ");
					new Sender(serverIP, eingaben[1]);
				} else if (eingabe.equalsIgnoreCase("MAILBOX")) {
					// moeglichkeit Nachrichtenanzeige
					new Mailbox(myIP, serverIP);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				// dies wird ausgegeben fals der Benutzer eine halbrichtige
				// Eingabe taetigt
				System.err
						.println("Sie haben eine falsche Menge an Eingaben getaetigt bitte halten sie sich an die Richtlinien");
			}

		}

	}
}
