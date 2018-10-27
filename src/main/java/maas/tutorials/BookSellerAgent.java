package maas.tutorials;

import java.util.Hashtable;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import maas.Book;
import maas.BookType;
import maas.behaviour.OfferRequestsServer;
import maas.behaviour.PurchaseOrdersServer;

public class BookSellerAgent extends Agent {
	private static final long serialVersionUID = -5310054528477305012L;
	// The catalogue of books for sale (maps the title of a book to its price)
	private Hashtable<String, Book> catalogue;

	// Put agent initializations here
	protected void setup() {
		// Create the catalogue
		catalogue = new Hashtable<>();

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			for (Object arg : args) {
				Book b = (Book) arg;
				catalogue.put(b.getTitle(), b);
				// Paperbacks could be less expensive than ebooks to make sure some are bought
				b.setPrice((Math.random() * 20 + 5) * ((b.getBookType() == BookType.PAPERBACK) ? 1 : 2));
			}
		}

		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-selling");
		sd.setName("JADE-book-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behaviour serving requests for offer from buyer agents
		addBehaviour(new OfferRequestsServer(catalogue));
		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new PurchaseOrdersServer(catalogue));
	}

	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Seller-agent " + getAID().getName() + " terminating.");
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

}