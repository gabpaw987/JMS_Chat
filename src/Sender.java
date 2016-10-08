import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Diese Klasse schickt eine Nachricht an einen gewollten Empfaenger <br>
 * der Benutzer kann nur eine Zeile abschicken und kann ausserdem nur eine IP <br>
 * angeben keinen Usernamen
 * 
 * @author Josef Sochovsky
 * @version 1.0
 */
public class Sender {
	// hier werden die notwendigen Standartkommandos fuer die Connection mit dem
	// ActiveMQ durchgefuehrt
	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	// die URL wird dann spaeter noch veraendert weil standartmaessig localhost
	// darinsteht
	private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	// Objekte von notwendigen Klassen um senden und empfangen zu koennen
	private Session session;
	private Connection connection;
	private MessageProducer producer;
	private Destination destination;

	/**
	 * Diese Klasse erzeugt persistente Nachrichten damit man auch zeitversetzt
	 * Nachrichten empfangen kann <br>
	 * Der Konstruktor erzeugt die Nachricht und erhaelt vom Benutzer die
	 * Informationen: <br>
	 * 
	 * @param sip
	 *            die IP des Servers
	 * @param an
	 *            die IP an die geschickt werden soll
	 */
	public Sender(String sip, String an) {
		// Create the connection.
		session = null;
		connection = null;
		producer = null;
		destination = null;
		// statt dem Standartmaessigen localhost wird dann die gewuenschte
		// Serverip eingetragen
		url = url.replace("localhost", sip);
		// Erstellen des Nachrichteninhalts mittels InputStreamReader man kann
		// nur eine Zeile eingeben
		String text = "";
		System.out
				.println("Bitte geben sie den Text ein den sie abschicken wollen, bei einem Enter wird der Text uebernommen");
		try {
			text = (new BufferedReader(new InputStreamReader(System.in)))
					.readLine();
		} catch (IOException e1) {
			System.out
					.println("Beim einlesen der Nachricht ist ein Fehler aufgetreten");
			e1.printStackTrace();
		}
		try {
			// create Connection via ConnectionFactory
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();
			connection.start();

			// Create the session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			// erstellen einer neuen Queue
			destination = session.createQueue(an);

			// Create the producer to send
			producer = session.createProducer(destination);

			// Setzen das die Nachricht persistent damit sie nicht nur temporaer
			// gespeichert wird
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			// absenden der Nachricht in die Queue
			producer.send(session.createTextMessage(text));

		} catch (Exception e) {
			System.out.println("[MessageProducer] Caught: " + e);
			e.printStackTrace();
			// am Ende der Methode werden dann die Verbindungen gekappt
		} finally {
			try {
				producer.close();
			} catch (Exception e) {
				System.out
						.println("Nachrichtensender konnte nicht geschlossen werden: "
								+ e);
			}
			try {
				session.close();
			} catch (Exception e) {
				System.out
						.println("Nachrichtensendesession konnte nicht geschlossen werden: "
								+ e);
			}
			try {
				connection.close();
			} catch (Exception e) {
				System.out
						.println("Nachrichtensendeconnection konnte nicht geschlossen werden: "
								+ e);
			}
			// Die Nachricht wird dann nocheinmal ausgegeben
			System.out.println("Ihre Nachricht wurde mit dem Inhalt:\" " + text
					+ "\" versand");
		}
	}
}
