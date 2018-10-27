package maas.tutorials;

import java.util.ArrayList;
import java.util.List;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import maas.behaviour.RequestPerformer;

@SuppressWarnings("serial")
public class BookBuyerAgent extends Agent {

	private static int instance_counter = 0;

	// The title of the book to buy
	private String targetBookTitle;
	// The list of known seller agents
	private AID[] sellerAgents;

	public BookBuyerAgent() {
		instance_counter++;
	}

	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready.");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetBookTitle = (String) args[0];
			System.out.println("Trying to buy " + targetBookTitle);
		} else {
			// Make the agent terminate immediately
			System.out.println("No book title specified");
			doDelete();
		}

		// Add a TickerBehaviour that schedules a request to seller agents every minute
		addBehaviour(new TickerBehaviour(this, 5000) {
			List<Behaviour> behaviours = new ArrayList<>();

			protected void onTick() {
				if (behaviours.size() == 3) {
					for (Behaviour b : behaviours) {
						if (!b.done()) {
							return;
						}
					}
					System.out.println("Agent " + myAgent.getName() + " has bought his book 3 times.");
					instance_counter--;
					if (instance_counter == 0) {
						ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
						Codec codec = new SLCodec();
						myAgent.getContentManager().registerLanguage(codec);
						myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
						shutdownMessage.addReceiver(myAgent.getAMS());
						shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
						shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
						try {
							myAgent.getContentManager().fillContent(shutdownMessage,
									new Action(myAgent.getAID(), new ShutdownPlatform()));
							myAgent.send(shutdownMessage);
							System.out.println("All BookBuyerAgents are done!");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					myAgent.doDelete();
				}

				// Update the list of seller agents
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("book-selling");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					sellerAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						sellerAgents[i] = result[i].getName();
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				RequestPerformer behav = new RequestPerformer(targetBookTitle, sellerAgents);
				behaviours.add(behav);
				addBehaviour(behav);
			}
		});
	}

	protected void takeDown() {
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}

}
