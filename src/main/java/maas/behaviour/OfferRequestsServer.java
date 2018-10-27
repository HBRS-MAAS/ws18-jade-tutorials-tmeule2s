package maas.behaviour;

import java.util.Hashtable;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import maas.Book;

public class OfferRequestsServer extends CyclicBehaviour {
	private static final long serialVersionUID = -3863996398471466048L;
	private Hashtable<String, Book> catalogue;

	public OfferRequestsServer(Hashtable<String, Book> catalogue) {
		this.catalogue = catalogue;
	}

	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
// Message received. Process it
			String title = msg.getContent();
			System.out.println("Got request for title " + title);
			ACLMessage reply = msg.createReply();
			if (catalogue.get(title) != null) {
				Double price = catalogue.get(title).getPrice();
				if (price != null) {
// The requested book is available for sale. Reply with the price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
				} else {
// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
			} else {
				// The requested book is NOT available for sale.
				reply.setPerformative(ACLMessage.REFUSE);
				reply.setContent("not-available");
			}
			myAgent.send(reply);
			System.out.println("Send reply " + ACLMessage.getPerformative(reply.getPerformative())
					+ " to request on title " + title);
		} else {
			block();
		}
	}
}