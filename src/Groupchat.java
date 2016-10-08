import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Diese Klasse erstellt eine neue Connection zu einem Topic und hat die<br>
 * Faehigkeiten Nachrichten zu senden und zu empfangen<br>
 * damit man immer die neuen Nachrichten empfangen kann wird hier ein Thread<br>
 * erstellt der alle 200ms nachsieht ob eine neue Nachricht verfuegbar ist <br>
 * 
 * @author Josef Sochovsky
 * @version 1.0
 */
public class Groupchat {
	// hier werden die notwendigen Standartkommandos fuer die Connection mit dem
	// ActiveMQ durchgefuehrt
	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	// die URL wird dann spaeter noch veraendert weil standartmaessig localhost
	// darinsteht
	private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	// hier wird der Name des Benutzers gespeichert
	private String name;
	// hier wird die ip des Benutzers gespeichert
	private String ip;
	// Objekte von notwendigen Klassen um senden und empfangen zu koennen
	private Session session;
	private Connection connection;
	private MessageProducer producer;
	private Destination destination;
	private MessageConsumer consumer;
	// hier wird dann ein Objekt des Threads erstellt damit man es immer beenden
	// und starten kann
	private Receive r;

	/**
	 * Diese Methode soll Nachrichten schicken, dazu wird einfach nach der <br>
	 * Angabe der Name des Benutzers und dann die ip in [] vorher geschrieben <br>
	 * und dann die eigentliche Nachricht <br>
	 * Mittels message.send() wird dann die Nachricht an den Topic gesendet und<br>
	 * laesst sich von allen anderen Chatteilnehmern empfangen<br>
	 * 
	 * @param nachricht
	 *            In diesem Parameter steht die Nachricht die versendet werden<br>
	 *            soll<br>
	 * @throws JMSException
	 *             Diese Exception wird im Konstruktor behandelt und zeigt dem<br>
	 *             Benutzer dann moeglicherweise das beim <br>
	 *             senden der Nachricht ein Fehler aufgetreten ist<br>
	 */
	public void send(String nachricht) throws JMSException {
		// Create the message
		TextMessage message = session.createTextMessage(name + "[" + ip + "]"
				+ ": " + nachricht);
		producer.send(message);
	}

	/**
	 * Diese Methode startet den Empfaengerprozess
	 */
	public void startEmp() {
		r = new Receive();
		r.start();
	}

	/**
	 * Diese Methode beendet den Empfaengerprozess
	 */
	public void beendeEmp() {
		r.setAktiv();
	}

	/**
	 * In dem Konstruktor wird die Connection aufgebaut und auf Benutzereingaben <br>
	 * gewartet nun kann der Benutzer von einem topic empfangen und senden<br>
	 * 
	 * @param topic
	 *            Name des Topics
	 * @param name
	 *            Name des momentanen Benutzers
	 * @param hip
	 *            ip des Benutzers
	 * @param sip
	 *            ip des MessageBrokers
	 */
	public Groupchat(String topic, String name, String hip, String sip) {
		// Informationen fuer den Benutzer
		System.out.println("Sie befinden sich in dem Gruppenchat: " + topic);
		System.out
				.println("mit dem Kommando \"exit\" koenn sie den Chat verlassen");
		System.out
				.println("mittels Enter koennen sie Nachrichten absetzen leere Nachrichten sind nicht zulaessig");
		// Create the connection.
		session = null;
		connection = null;
		producer = null;
		destination = null;
		this.name = name;
		this.ip = hip;
		// statt dem Standartmaessigen localhost wird dann die gewuenschte
		// Serverip eingetragen
		url = url.replace("localhost", sip);
		try {
			// create Connection via ConnectionFactory
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();
			connection.start();

			// Create the session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createTopic(topic);

			// Create the producer to send
			producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// create the consumer to receive
			consumer = session.createConsumer(destination);

			// nun wird der Empaenger gestartet
			startEmp();
			// diese Schleife wird mit dem Kommando exit beendet
			// mittels der Eingabe des Benutzers wird dann eine Nachricht
			// gesendet
			while (true) {
				String eingabe = "";

				eingabe = (new BufferedReader(new InputStreamReader(System.in)))
						.readLine();
				if (eingabe.equals("exit")) {
					beendeEmp();
					break;
				}
				// leere Nachrichten werden nicht abgesetzt
				if (!eingabe.equals(""))
					send(eingabe);
			}
		} catch (Exception e) {
			System.out.println("[MessageProducer] Caught: " + e);
			// am Ende der Methode werden dann die Verbindungen gekappt
		} finally {
			try {
				producer.close();
			} catch (Exception e) {
				System.out
						.println("Chatsender konnte nicht geschlossen werden: "
								+ e);
			}
			try {
				consumer.close();
			} catch (Exception e) {
				System.out
						.println("Chatempaenger konnte nicht geschlossen werden: "
								+ e);
			}
			try {
				session.close();
			} catch (Exception e) {
				System.out
						.println("Chatsession konnte nicht geschlossen werden: "
								+ e);
			}
			try {
				connection.close();
			} catch (Exception e) {
				System.out
						.println("Chatconnection konnte nicht geschlossen werden: "
								+ e);
			}
		}
	}

	/**
	 * Diese Klasse soll Nachrichten empfangen und damit der Benutzer nicht<br>
	 * andauernd auf consumer.receive warten muss wird hier ein Thread erstellt
	 * 
	 * @author Josef Sochovsky
	 * @version 1.0
	 */
	private class Receive extends Thread {
		// mit der Variable wird der Thread dann beendet
		private boolean aktiv;

		/**
		 * Die Methode wird alle 200 ms aufgerufen und ueberprueft ob neue <br>
		 * Nachrichten zum abpruefen vorhanden sind
		 * 
		 * @throws JMSException
		 *             es kann passieren das ein Fehler mit der Verbindung zum <br>
		 *             MessageBroker ein Fehler auftritt <br>
		 */
		public void anzeigen() throws JMSException {
			// Start receiving
			TextMessage message = (TextMessage) consumer.receive();
			if (message != null) {
				System.out.println(message.getText());
			}
		}

		/**
		 * Diese Methode switcht den Zustand vom Thread auf aus weil dann die
		 * Schleife abgeschlossen werden kann
		 */
		public void setAktiv() {
			aktiv = false;
		}

		/**
		 * Diese Methode wird von start() aufgerufen und ruft alle 200 ms die
		 * anzeige() Methode auf <br>
		 */
		public void run() {
			aktiv = true;
			while (aktiv) {
				try {
					sleep(200);
					anzeigen();
				} catch (InterruptedException e) {
					System.out
							.println("Chatthread wurde ploetzlich unterbrochen: "
									+ e);
				} catch (JMSException e) {
					System.out
							.println("Chatempaenger hatte einen Fehler beim Verbindungsaufbau: "
									+ e);
				}
			}
		}
	}
}
