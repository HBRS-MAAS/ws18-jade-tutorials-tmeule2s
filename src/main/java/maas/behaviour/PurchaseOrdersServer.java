package maas.behaviour;

import java.io.IOException;
import java.util.Hashtable;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import maas.Book;
import maas.BookType;

public class PurchaseOrdersServer extends CyclicBehaviour {

	private static final long serialVersionUID = 7938556330444711767L;

	private Hashtable<String, Book> catalogue;

	public PurchaseOrdersServer(Hashtable<String, Book> catalogue) {
		this.catalogue = catalogue;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			String title = msg.getContent();
			System.out.println("Got accept proposal for title " + title);
			Book book = catalogue.get(title);

			ACLMessage reply = msg.createReply();
			if (book != null) {
				reply.setPerformative(ACLMessage.CONFIRM);
				try {
					reply.setContentObject(book);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				reply.setPerformative(ACLMessage.DISCONFIRM);
				reply.setContent("not-available");
			}
			myAgent.send(reply);
			if (book.getBookType() == BookType.PAPERBACK) {
				book.setPrice(null);
			}
		} else {
			block();
		}

	}

}
