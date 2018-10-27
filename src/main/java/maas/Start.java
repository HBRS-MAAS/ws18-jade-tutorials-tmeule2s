package maas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import jade.wrapper.PlatformEvent;
import jade.wrapper.StaleProxyException;
import maas.tutorials.BookBuyerAgent;
import maas.tutorials.BookSellerAgent;

public class Start {
	public static void main(String[] args) throws ControllerException {
		// given values
		HashMap<String, String> buyer_name_arguments = new HashMap<>();
		for (int i = 1; i < 30; i++) {
			buyer_name_arguments.put("buyer" + i, "Title" + i);
		}
		for (int i = 30; i < 50; i++) {
			int e = (int) (Math.random() * buyer_name_arguments.size()) + 1;
			buyer_name_arguments.put("buyer" + i, "Title" + e);
		}

		List<Book> seller_catalog = new ArrayList<>();
		int paperbacks = 0;
		String[] titles = buyer_name_arguments.values().toArray(new String[buyer_name_arguments.size()]);
		while (seller_catalog.size() < buyer_name_arguments.size() / 2) {
			Book b = new Book();
			b.setBookType(BookType.EBOOK);
			b.setTitle(titles[seller_catalog.size()]);
			seller_catalog.add(b);
		}
		for (int i = 0; i < 20; i++) {
			Book b = new Book();
			b.setBookType(BookType.PAPERBACK);
			b.setTitle(titles[(int) (Math.random() * titles.length)]);
			seller_catalog.add(b);
			if (b.getBookType() == BookType.PAPERBACK) {
				paperbacks++;
			}
		}

//		// starting code
//		List<String> agents = new Vector<>();
//		for (String agent_name : buyer_name_arguments.keySet()) {
//			agents.add(String.format("%s:%s", agent_name, BookBuyerAgent.class.getName()));
//		}
//
//		List<String> cmd = new Vector<>();
//		cmd.add("-agents");
//		StringBuilder sb = new StringBuilder();
//		for (String a : agents) {
//			sb.append(a);
//			sb.append(";");
//		}
//		cmd.add(sb.toString());
//		jade.Boot.main(cmd.toArray(new String[cmd.size()]));

		// Get a hold on JADE runtime
		jade.core.Runtime rt = jade.core.Runtime.instance();

		// Exit the JVM when there are no more containers around
		rt.setCloseVM(true);
		rt.invokeOnTermination(() -> {
			System.out.println("End of Simulation!");
		});
		System.out.print("runtime created\n");

		// Create a default profile
		Profile profile = new ProfileImpl(null, 1200, null);
		System.out.print("profile created\n");

		// rt.startUp(profile);

		System.out.println("Launching a whole in-process platform..." + profile);
		jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(profile);

		// now set the default Profile to start a container
		// ProfileImpl pContainer = new ProfileImpl(null, 1200, null);
		// System.out.println("Launching the agent container ..." + pContainer);

		// jade.wrapper.AgentContainer cont = rt.createAgentContainer(pContainer);
		// System.out.println("Launching the agent container after ..." + pContainer);

		System.out.println("containers created");
		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
		rma.start();

		List<String> sellerNames = new ArrayList<>();

		for (int i = 1; paperbacks > 0 || i < 4; i++) {
			Hashtable<String, Book> agent_catalog = new Hashtable<>();
			for (int e = 0; e < 10; e++) {
				Book b = seller_catalog.get((int) (Math.random() * seller_catalog.size()));
				if (agent_catalog.get(b.getTitle()) == null) {
					agent_catalog.put(b.getTitle(), b);
					if (b.getBookType() == BookType.PAPERBACK) {
						seller_catalog.remove(b);
						paperbacks--;
					}
				}
			}
			String name = String.format("SellerAgent%s", i);
			AgentController seller = mainContainer.createNewAgent(name, BookSellerAgent.class.getName(),
					agent_catalog.values().toArray());
			sellerNames.add(name);
			seller.start();
		}

		for (String agent_name : buyer_name_arguments.keySet()) {
			AgentController buyer = mainContainer.createNewAgent(agent_name, BookBuyerAgent.class.getName(),
					new Object[] { buyer_name_arguments.get(agent_name) });
			buyer.start();
		}

	}
}
