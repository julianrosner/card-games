/*
    represents a blackjack dealer
    that behaves autonomously according
    to the rules of the game,
    which completely specify how the
    dealer should behave in all circumstance
 */

import java.util.ArrayList;
import java.util.List;

public class BlackJackDealer extends BlackJackPlayer {

    // param:
    // the deck the dealer will be drawing from
    // and discarding to
    // throws:
    // illegal argument exception if shoe is null
    public BlackJackDealer(BlackJackShoe shoe)
            throws IllegalArgumentException {
        super(shoe,0);
    }

    // draws until bust or until hand is worth at least 17 points
    // returns:
    // list of cards drawn in order
    // modifies:
    // shoe, hand, self to be standing on only hand
    public List<Card> drawUntilSatisfied() {
        List<Card> drawnCards = new ArrayList<Card>();
        int[] pValues = getThisHandsValues(0);
        // draw while hand is worth less than 17 points
        // while counting any ace high when possible
        // without busting
        while((pValues[0] < 17 && pValues[1] < 17) ||
                (pValues[0] < 17 && pValues[1] > 21)) {
            drawnCards.add(draw(0));
            pValues = getThisHandsValues(0);
        }
        // when done drawing, stand
        standings[0] = true;
        return drawnCards;
    }

    // returns:
    // string representation of dealer's hand
    // when not standing, disguises the dealer's first card
    // because it is not yet legal for the player's to see this
    // requires:
    // if dealer is not standing, must have exactly two cards
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HAND: ");
        if (standings[0]) {  // if dealer has taken his turn
            List<Card> hand = super.getHand(0);
            if (hand.size() != 0) {  // non-empty case
                // build string of cards in hand in comma separated list
                for (int i = 0; i < hand.size() - 1; i++) {
                    sb.append(hand.get(i).toString());
                    sb.append(", ");
                }
                sb.append(hand.get(hand.size() - 1).toString());
            } else {  // empty case
                sb.append("EMPTY");
            }
        } else {  // if dealer hasn't taken his turn yet
            // don't display first card in hand
            sb.append("hidden card, ");
            // get second card in hand as string
            sb.append(super.getHand(0).get(1).toString());
        }
        sb.append("\n");
        return sb.toString();
    }
}