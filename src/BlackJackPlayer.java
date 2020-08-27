/*
    represents a player in a blackjack game
    capable of splitting to have multiple hands
    at the same time, supports only one or two hands
    currently but this my increase to four in the future
 */

import java.util.*;

public class BlackJackPlayer {
    // each List of cards represents one hand
    // the ith element of the below list of lists will frequently
    // be referred to as "the ith hand" in this documentation
    private List<List<Card>> hands;

    // the deck this player is drawing from
    // and discarding to
    private BlackJackShoe shoe;

    // the status of each hand
    // if standings[i] is true
    // hand i is inactive
    protected boolean[] standings;

    private int wealth;

    // the amount the player is betting on each hand
    // bets[i] is the active bet on hand i
    private int[] bets;

    private int insuranceBet;

    // used to identify which move player is taking
    public static final int HIT_ID = 1;
    public static final int STAND_ID = 2;
    public static final int DOUBLE_DOWN_ID = 3;
    public static final int SPLIT_ID = 4;

    // constructs a blackjack player who draws from
    // given card shoe and has wealth many dollars
    // throws:
    // IllegalArgumentException if wealth < 0
    // or shoe is null
    public BlackJackPlayer(BlackJackShoe shoe, int wealth)
            throws IllegalArgumentException {
        // sanity check
        if (shoe == null || wealth < 0) {
            throw new IllegalArgumentException();
        }
        // create first empty hand
        hands = new ArrayList<List<Card>>();
        hands.add(new ArrayList<Card>());
        this.shoe = shoe;
        // this player is not standing or betting on any hands yet
        standings = new boolean[]{false, false, false, false};
        bets = new int[]{0, 0, 0, 0};
        this.wealth = wealth;
        insuranceBet = 0;
    }

    // returns:
    // the number of hands this player currently has
    public int numHands() {
        return hands.size();
    }

    // params:
    // i is the index of desired hand
    // returns:
    // copy of current hand
    // note: that cards are immutable so deep copying is not necessary
    // throws:
    // index out of bounds exception if called with invalid index
    public List<Card> getHand(int i) {
        return new ArrayList<Card>(hands.get(i));
    }

    // params:
    // i is the index of the hand that will be drawing the card
    // returns:
    // the card which was drawn
    // modifies:
    // hand i to have the additional card
    public Card draw(int i) {
        Card drawn = shoe.draw();
        hands.get(i).add(drawn);
        return drawn;
    }

    // empties all hands into discard pile
    // then cuts hands field down to size one
    // modifies:
    // hands as described above
    // shoe to have all discarded cards in its discard pile
    public void discard() {
        for(int i = 0; i < hands.size(); i++) {
            shoe.discardHand(hands.get(i));
        }
        // return to having just one empty hand
        hands = new ArrayList<List<Card>>();
        hands.add(new ArrayList<Card>());
    }

    // force player not to be standing on any hands
    // modifies:
    // this to be not be standing on any hand
    public void sit() {
        for (int i = 0; i < standings.length; i++) {
            standings[i] = false;
        }
    }

    // param:
    // index of hand for which information is desired
    // returns:
    // true if player is standing on that hand, otherwise false
    public boolean isStanding(int i){
        return standings[i];
    }

    // params:
    // index of hand for which bet information is desired
    // returns:
    // current size of player's bet on hand i
    public int getBet(int i) {
        return bets[i];
    }

    // param:
    // i is index of hand that bet is to be placed on
    // betSize is the size of bet the player wants to make on said hand
    // throws:
    // IllegalArgumentException if betSize exceeds the players
    // current wealth or is less than 0
    // modifies:
    // this, decreases player's wealth and increases current bet
    // on hand i both by betSize
    public void bet(int i, int betSize) throws IllegalArgumentException {
        if (betSize < 0 || betSize > wealth) {
            throw new IllegalArgumentException();
        }
        wealth-= betSize;
        bets[i]+= betSize;
    }

    // param:
    // betSize is the size of the insurance bet the player wants to place
    // throws:
    // IllegalArgumentException if betSize exceeds the player's
    // current wealth, is less than 0, or is greater than half
    // the player's initial bet
    // modifies:
    // this, decreases player's wealth and increases current insuranceBet
    // both by betSize
    public void insuranceBet(int betSize) throws IllegalArgumentException {
        if (betSize < 0 || betSize > wealth || betSize > (bets[0] / 2)) {
            throw new IllegalArgumentException();
        }
        wealth-= betSize;
        insuranceBet+= betSize;
    }

    // returns:
    // player wealth
    // any bets currently placed are not included in this sum
    public int getWealth() {
        return wealth;
    }

    // param:
    // i is the index of bet for which result is the result
    // result represents outcome of player hand
    // using static constants from BlackJackGame
    // BLACKJACK_ID indicates the player had a blackjack
    // WIN_ID indicates the player beat the dealer
    // PUSH_ID indicates the player tied with the dealer (or pushed)
    // LOSS_ID indicates the player lost
    // modifies:
    // this to have a bet of 0 on this hand and
    // whatever wealth is appropriate given outcome of hand
    public void resolveBet(int i, int result) {
            switch (result) {
                case BlackJackGame.BLACKJACK_ID:
                    wealth += (int) (bets[i] * 2.5);
                    break;
                case BlackJackGame.WIN_ID:
                    wealth += bets[i] * 2;
                    break;
                case BlackJackGame.PUSH_ID:
                    wealth += bets[i];
            }
            bets[i] = 0;
    }

    // param:
    // dealerBJ is a boolean which indicates whether or not
    // the dealer has a blackjack
    // if he does, dealerBJ is to be true
    // and false otherwise
    // modifies:
    // this to have an insurance bet of 0
    // and whatever wealth is appropriate given outcome of dealer hand
    public void resolveInsuranceBet(boolean dealerBJ) {
        if(dealerBJ) {
            wealth+= 3 * insuranceBet;
        }
        insuranceBet = 0;
    }

    // params:
    // i is index of hand taking action
    // actID == HIT_ID if hit, STAND_ID if stand,
    // DOUBLE_DOWN_ID for double down, SPLIT_ID for split
    // behavior not guaranteed for other values
    // returns:
    // size 2 array of cards drawn from this action in order
    // if less than 2 cards were drawn, one or both slots
    // in the array will be null
    // modifies:
    // this to have taken the specified action
    // throws:
    // UnsupportedOperationException
    // if a player with 2 hands tries to split
    // IllegalArgumentException
    // if a player attempts a double down or split that they do not
    // have the wealth to pay for,
    // attempts a split with a hand that isn't size 2
    // with matching cards,
    // or tries to double down after having drawn this turn
    public Card[] takeAction(int i, int actID) throws IllegalArgumentException,
            UnsupportedOperationException {
        Card[] drawn = new Card[2];
        if (actID == HIT_ID) { // hit
            drawn[0] = draw(i);
        } else if (actID == DOUBLE_DOWN_ID) { // double down
            // check player hasn't drawn yet
            if (hands.get(i).size() != 2) {
                throw new IllegalArgumentException();
            }
            // try bet, throws exception if player is too poor
            bet(i, getBet(i));
            // marks hand as standing
            standings[i] = true;
            drawn[0] = draw(i);
        } else if (actID == SPLIT_ID) { // split
            // having more than 2 hands is occasionally accepted
            // in the rules of some casinos, but it is not
            // supported here
            if (hands.size() == 2) {
                throw new UnsupportedOperationException();
                // below we check that hand holds exactly two matching cards
                // if not throws exception
            } else if (hands.get(i).size() != 2 ||
                    hands.get(i).get(0).getRankID() !=
                            hands.get(i).get(1).getRankID()) {
                throw new IllegalArgumentException();
            }
            // bet on new hand equal to size of bet on parent hand
            hands.add(new ArrayList<Card>());
            bet(hands.size() - 1, getBet(i));
            // draw both hands up to two cards
            splitHelper(i, hands.size() - 1, drawn);
            // if a player splits aces, those hands can no longer
            // be touched this round after they are completed
            if (hands.get(i).get(0).getRankID() == Card.ACE) {
                standings[i] = true;
                standings[hands.size() - 1] = true;
            }
        } else {  // stand
            standings[i] = true;
        }
        return drawn;
    }

    // params:
    // i is the hand index that is being split from
    // j is the hand index that is receiving the split
    // drawn is a return parameter for cards drawn to hands
    // in order
    // modifies:
    // this to have a second hand containing
    // the second card of the first one and both hands
    // to draw an additional card each
    // requires:
    // hand index i has at least 2 cards
    // drawn is at least size 2
    private void splitHelper(int i, int j, Card[] drawn) {
        Card c = hands.get(i).remove(1);
        hands.get(j).add(c);
        drawn[0] = draw(i);
        drawn[1] = draw(j);
    }

    // param:
    // index of hand for which values are desired
    // returns:
    // potential point values for hand in size 2 array
    // (a hand can have multiple values because
    // aces can be worth 1 or 11 points
    // there is no use taking 11's on
    // multiple aces however because that will always result in at least 22
    // so a hand only has 2 values worth tracking at most)
    // with the smaller evaluation first followed by the greater one
    // or -1 if the hand contains no aces
    public int[] getThisHandsValues(int j) {
        return BlackJackGame.getHandValues(hands.get(j));
    }

    // returns:
    // string representing player's state
    // e.g.
    // HAND 1: jack of hearts, 2 of clubs
    // BET 1: $1000
    // HAND 2: jack of diamonds, 8 of hearts, 2 of spades
    // BET 2: $2000
    // INSURANCE BET: $0
    // WEALTH: $7590
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < hands.size(); j++) {
            sb.append(handToString(j));
        }
        sb.append("INSURANCE BET: ");
        sb.append("$");
        sb.append(insuranceBet);
        sb.append("\n");
        sb.append("WEALTH: ");
        sb.append("$");
        sb.append(wealth);
        sb.append("\n");
        return sb.toString();
    }

    // helper method for toString
    // param:
    // i, index of hand for which string representation is desired
    // returns:
    // string representing hand's state e.g.
    // HAND 2: jack of diamonds, 8 of hearts, 2 of spades
    // BET 2: $2000
    private String handToString(int i) {
        StringBuilder sb = new StringBuilder();
        // print all but last card in hand
        if (hands.get(i).size() > 0) {
            sb.append("HAND ").append(i + 1).append(": ");
            for (int j = 0; j < hands.get(i).size() - 1; j++) {
                sb.append(hands.get(i).get(j).toString());
                sb.append(", ");
            }
            // print last card in hand
            sb.append(hands.get(i).get(hands.get(i).size() - 1).toString());
        } else {
            sb.append("EMPTY");
        }
        sb.append("\n");
        sb.append("BET ");
        sb.append(i + 1);
        sb.append(": ");
        sb.append("$");
        sb.append(bets[i]);
        sb.append("\n");
        return sb.toString();
    }
}
