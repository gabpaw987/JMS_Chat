import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Diese Klasse soll alle verfuegbaren Mails fuer eine IP Addresse abrufen damit<br>
 * dies funktioniert wird so lange receive aufgerufen bis die erhaltene<br>
 * Nachricht null ist<br>
 * 
 * @author Josef Sochovsky
 * @version 1.0
 */
public class Mailbox {
	// hier werden die notwendigen Standartkommandos fuer die Connection mit dem
	// ActiveMQ durchgefuehrt
	private static String user = ActiveMQConnection.DEFAULT_USER;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	// die URL wird dann spaeter noch veraendert weil standartmaessig localhost
	// darinsteht
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

	/**
	 * Der Konstruktor baut eine Verbindung mit dem MessageBroker auf und ruft <br>
	 * dann alle Nachrichten die fuer den Benutzer verfuegbar sind ab <br>
	 * 
	 * @param ip
	 *            Die ip die dem Namen der Queue entspricht
	 * @param sip
	 *            Die ip des MessageBrokers
	 */
	public Mailbox(String ip, String sip) {
		// statt dem Standartmaessigen localhost wird dann die gewuenschte
		// Serverip eingetragen
		url = url.replace("localhost", sip);
		// Create the connection. with most of the Objects
		Session session = null;
		Connection connection = null;
		MessageConsumer consumer = null;
		Destination destination = null;
		try {
			// erstellen einer Connection mittels der ConnectionFactory
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();
			// starten der Connection
			connection.start();

			// Create the session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			// Schleifenvariable
			destination = session.createQueue(ip);
			// Create the consumer
			consumer = session.createConsumer(destination);

			// es ist wichtig das man am Anfang also beim ersten receiven normal
			// receive(long time) ausfuehrt
			// und dann nur noch receiveNoWait() weil sonst die Methode so lange
			// warten wuerde bis eine Nachricht kommt

			TextMessage message = (TextMessage) consumer.receive(20);
			// wenn es keine Nachrichten gibt soll dem Benutzer dies auch gesagt
			// werden, wenn er welche hat wird oben und unten eine Nachricht
			// hinzugefuegt
			if (message != null) {
				System.out
						.println("Hier sehen sie nun die Nachrichten die ihnen zugesandt wurden"
								+ "\n");
				// es wird so lange die Nachricht angezeigt bis keine mehr da
				// ist
				while (message != null) {
					// Start receiving

					System.out
							.println("Message received: " + message.getText());
					// damit die Nachricht nicht drinnbleibt
					message.acknowledge();
					// oben erklaert warum receiveNoWait()
					message = (TextMessage) consumer.receiveNoWait();
				}
				System.out.println("Ende deiner Nachrichten");
			} else {
				System.out
						.println("Sie haben momentan leider keine Nachrichten in ihrer Mailbox");
			}
		} catch (Exception e) {
			System.out.println("[MessageConsumer] Caught: " + e);
			e.printStackTrace();
		} finally {
			try {
				consumer.close();
			} catch (Exception e) {
				System.out
						.println("Mailempaenger konnte nicht geschlossen werden: "
								+ e);
			}
			try {
				session.close();
			} catch (Exception e) {
				System.out
						.println("Mailsession konnte nicht geschlossen werden: "
								+ e);
			}
			try {
				connection.close();
			} catch (Exception e) {
				System.out
						.println("Mailconnection konnte nicht geschlossen werden: "
								+ e);
			}
		}
	}
}