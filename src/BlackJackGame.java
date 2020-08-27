/*
 The below class represent a game of blackjack
 designed to be compatible with different
 manners of representation, be they text-based or graphical.
 If you're unfamiliar, the rules are explained
 here https://bicyclecards.com/how-to-play/blackjack/
 The dealer is hard-coded to be
 computer controlled because a dealer's
 behavior requires no decision making.
 All players can take input through any
 means however.
 As for the rules of this implementation:
 only one split is permitted per player per round,
 splits are evaluated on card rank rather than value,
 surrendering is not permitted,
 insurance is offered only if the dealer is showing an ace
 and if the current player bet at least twice the
 table minimum in the initial round of betting.
*/

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
// NOTE ABOUT DOCUMENTATION: the term "player i" or "ith player" is used
// frequently below.
// this "i" refers to the index of the BlackJackPlayer in the "players" field
// j is often used to refer to the ith player's jth hand (a single player can
// have multiple hands due to splitting)
// if ever a method in this class is given an "i" outside of the players list's
// bounds or a "j" outside of a player's list of hands, an
// IndexOutOfBoundsException WILL BE THROWN unless otherwise specified
// use numPlayers, and numHands(i) to prevent this
public class BlackJackGame {
    // this is used internally to distinguish
    // a black jack from an ordinary 21
    private static final int BJ_VALUE = 600;

    private BlackJackDealer dealer;
    private BlackJackShoe shoe;  // the play deck
    private List<BlackJackPlayer> players;  // all players listed left to right
    private int tableMin;  // lowest legal bet
    private int tableMax;  // highest legal bet

    // used to identify when a method is to act
    // on the dealer rather than a player
    // (accepted where specified)
    public static final int DEALER_ID = -1;

    // used to indicate outcome of player hand
    public static final int LOSS_ID = -1;
    public static final int PUSH_ID = 0;
    public static final int WIN_ID = 1;
    public static final int BLACKJACK_ID = 2;

    // params:
    // numPlayers is the desired number of players,
    // numDecks is number of decks in the shoe (the large deck),
    // tableMin is the smallest legal bet,
    // tableMax is the largest legal bet,
    // playerWalletSizes is an int array representing how much money
    //  each players starts with ie. if the array holds [10, 60, 300],
    //  player one starts with $10
    //  player two with $60, and player three with $300
    // throws:
    // IllegalArgumentException if any of the
    // following conditions is violated
    // numbPlayers > 0, numDecks > 0,
    // tableMin non-negative, tableMin <= tableMax,
    // playerWalletSizes.length = numPlayers,
    // lastly ((12 * (numPlayers + 1)) / 52) < numDecks
    // this unusual last condition ensures that the deck never runs out
    // of cards because 11 is the most cards that can appear in
    // a non-busted hand
    public BlackJackGame(int numPlayers, int numDecks, int tableMin,
                         int tableMax, int[] playerWalletSizes)
            throws IllegalArgumentException {
        // error check described in documentation
        if(numPlayers <= 0 || numDecks <= 0 || tableMin < 0 ||
                tableMin > tableMax || playerWalletSizes.length != numPlayers ||
                ((12 * (numPlayers + 1)) / 52) >= numDecks ) {
            throw new IllegalArgumentException();
        }
        // set betting restrictions
        this.tableMin = tableMin;
        this.tableMax = tableMax;

        // create deck
        this.shoe = new BlackJackShoe(numDecks);

        // create dealer with existing deck
        dealer = new BlackJackDealer(shoe);

        // create numPlayers many players and add them to list
        players = new ArrayList<BlackJackPlayer>();
        for (int i = 0; i < numPlayers; i++) {
            players.add(new BlackJackPlayer(shoe, playerWalletSizes[i]));
        }
    }

    // simple constructor for basic one-player game
    // creates instance with one $500 player,
    // a six deck shoe, min bet $10, and max bet $10,000
    public BlackJackGame() throws IllegalArgumentException {
        this(1, 6, 10, 10000, new int[] {500});
    }

    // FIRST ALL METHODS WHICH ADVANCE THE GAME'S STATE IN THE ORDER
    // THAT THEY SHOULD BE CALLED (playerAction calls
    // should be skipped when the dealer has a blackjack and insurance methods
    // should be skipped when the dealer isn't showing an ace)

    // every player with enough money to play
    // and the dealer draws until they have two cards, in
    // the order stipulated by the rules of blackjack
    // returns:
    // list of cards drawn in the order that they were drawn
    // modifies:
    // players and dealer to all draw two additional card
    public List<Card> DrawCards() {
        List<Card> cards = new ArrayList<Card>();
        // this unusual draw pattern is
        // necessary according to the game's rules
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < players.size(); i++) {
                // don't draw cards if you can't afford to play
                if (hasEnoughToPlay(i, 0)) {
                    // this should be called at start of turn
                    // so there's only one hand to draw into
                    cards.add(players.get(i).draw(0));
                }
            }
            cards.add(dealer.draw(0));
        }
        return cards;
    }

    // params:
    // i index of player for whom the bet will be placed
    // j index of whichever of the player's hands she is placing the bet on
    // betSize the desired size for the player to bet
    // modifies:
    // specified player to reflect newly placed bet
    // throws:
    // IllegalArgumentException
    // if bet is outside of the table's min and max bet sizes,
    // if betSize exceeds the players current wealth, or if bet is less than 0
    public void placeInitialBet(int i, int betSize) throws IllegalArgumentException {
        if (betSize < tableMin || betSize > tableMax) {
            throw new IllegalArgumentException();
        }
        players.get(i).bet(0, betSize);
    }

    // params:
    // i index of player for whom the bet will be placed
    // betSize the desired size for the player to bet insurance
    // modifies:
    // specified player to reflect newly placed bet
    // throws:
    // IllegalArgumentException
    // if bet is outside of the table's min and max bet sizes
    // (unless the bet is 0 which is always ok),
    // if betSize exceeds the players current wealth, if bet is less than 0
    // of if bet is greater than half the this player's original bet this round
    public void playerInsuranceBet(int i, int betSize)
            throws IllegalArgumentException {
        if (betSize != 0 && (betSize < tableMin || betSize > tableMax)) {
            throw new IllegalArgumentException();
        }
        players.get(i).insuranceBet(betSize);
    }

    // params:
    // i index of player who will be acting
    // j index of whichever of the player's hands she is acting on
    // actionID indicates what sort of action will be performed
    // this is specified using static global variables in
    // the BlackJackPlayerClass
    // HIT_ID for hit, STAND_ID for stand,
    // DOUBLE_DOWN_ID for double down, and SPLIT_ID for split
    // modifies:
    // specified player to reflect specified action
    // returns:
    // size 2 array containing any cards that were drawn
    // if less than two were drawn, those slots will be null
    // throws:
    // UnsupportedOperationException
    // if a player with 2 hands tries to split
    // IllegalArgumentException
    // when the given player is not in a legal state to
    // take the given move, this happens when:
    // desired double down would exceed the table's max bet,
    // if the player hasn't bet this round,
    // or has already finished acting this round,
    // if the player lacks the funds
    // to pay for a split or double down,
    // if the player tries to split or double down
    // with more than two cards,
    // or if the player
    // tries to split with cards which are different ranks
    public Card[] playerAction(int i, int j, int actionID)
            throws IllegalArgumentException {
        if ((actionID == BlackJackPlayer.DOUBLE_DOWN_ID &&
                players.get(i).getBet(j) * 2 > tableMax) ||
                players.get(i).getBet(j) < tableMin ||
                !isNotStandingOrBusted(i, j)) {
            throw new IllegalArgumentException();
        }
        return players.get(i).takeAction(j, actionID);
    }

    // causes dealer to take their turn
    // this behavior is specified entirely by
    // the games rules, so there's no complicated
    // decision making here
    // returns:
    // list of cards dealer drew this turn in order
    // modifies:
    // dealers hand to reflect cards drawn
    public List<Card> dealersTurn () {
        return dealer.drawUntilSatisfied();
    }

    // returns:
    // a list judgements where index i holds an int representing
    // the result of the ith hand played against the dealer
    // NOT merely the ith player's hand
    // e.g. in a two player game, if player one split to have two
    // hands and player two did not judgements(0) would represent
    // the result of player one's left hand, judgements(1) her right, and
    // judgements(2) player two's hand
    // the results map to their meaning as such
    // BLACKJACK_ID for player blackjack
    // WIN_ID for player win
    // PUSH_ID for push (aka a tie)
    // LOSS_ID for player loss
    // note that the distinction between blackjack case and regular win case
    // is needed because blackjacks have a higher payout
    // modifies:
    // all players to have the appropriate wealth
    // given the sizes and results of their bets
    public List<Integer> resolveAllBets() {
        // list holding victory status of players
        List<Integer> judgements = new ArrayList<Integer>();
        int dealerScore = evaluateScore(DEALER_ID, -1);
        // evaluate all hands of all players
        for (int i = 0; i < players.size(); i++) {
            for (int j = 0; j < players.get(i).numHands(); j++) {
                int curHandScore = evaluateScore(i, j);
                int result;
                // player blackjack
                if (curHandScore == BJ_VALUE && dealerScore != BJ_VALUE
                        && !hasSplitAces(i)) {
                    // a blackjack on a split ace counts instead as 21
                    result = BLACKJACK_ID;
                } else if (curHandScore > dealerScore) {  // player win
                    result = WIN_ID;
                } else if (curHandScore == -1 ||
                        curHandScore < dealerScore) { // player loss
                    // note that according to the rules of blackjack,
                    // a player who busts loses even if the dealer also busts
                    result = LOSS_ID;
                } else {  // push
                    // push (this is the blackjack lingo for "tie")
                    result = PUSH_ID;
                }
                // award player winnings according to results calculated above
                players.get(i).resolveBet(j, result);
                // add result to list
                judgements.add(result);
            }
        }
        return judgements;
    }

    // awards winnings to any players who correctly bet the
    // dealer would blackjack
    // modifies:
    // players to have appropriate funds given their
    // insurance bets
    public void resolveAllInsuranceBets() {
        boolean dealerBJ = evaluateScore(DEALER_ID, -1) == BJ_VALUE;
        for (var p : players) {
            p.resolveInsuranceBet(dealerBJ);
        }
    }

    // discard all cards and mark all players
    // as being able to draw again
    // modifies:
    // dealer and players to have empty hands
    // and be ready to draw again
    public void endTurn() {
        for(var p : players) {
            p.sit();
            p.discard();
        }
        dealer.sit();
        dealer.discard();
    }

    // THIS CONCLUDES STATE_ADVANCING METHODS

    // params:
    // i index of desired player in players list
    // or DEALER_ID if the dealer's hand is desired
    // j index representing which of player i's hands is desired
    // returns:
    // the ith player's jth hand or dealer's only hand if i = DEALER_ID
    public List<Card> getHand(int i, int j) {
        if (i == DEALER_ID) {
            // the dealer only ever has one hand
            return dealer.getHand(0);
        } else {
            return players.get(i).getHand(j);
        }
    }

    // returns:
    // the number of times a card has
    // been drawn since the last time the shoe
    // was shuffled
    public int getDrawsSinceLastShuffle() {
        return shoe.getDrawsSinceLastShuffle();
    }

    // returns:
    // ith players current wealth
    // any bets the player currently
    // has placed is not included in
    // this sum
    public int getWealth(int i) {
        return players.get(i).getWealth();
    }

    // params:
    // i index of player for which bet info is desired
    // j index representing which of player i's current
    // bets user wants the size of (each active hand has a bet)
    // returns:
    // the size of the ith player's jth bet
    public int getBet(int i, int j) {
        return players.get(i).getBet(j);
    }

    // params:
    // i index of player for which info is desired
    // returns:
    // true if player i split on aces this turn
    private boolean hasSplitAces(int i) {
        BlackJackPlayer bjp = players.get(i);
        return bjp.numHands() > 1 &&
                bjp.getHand(0).get(0).getRankID() == Card.ACE;
    }

    // params:
    // i index of player for which info is desired
    // returns:
    // number of hands player i currently has
    public int numHands(int i) {
      return players.get(i).numHands();
    }

    // returns:
    // number of players in game
    public int numPlayers() {
        return players.size();
    }

    // params:
    // i index of player for which information is desired
    // j index of whichever of the player's hands info is desired
    // returns:
    // true if ith player's jth hand is not standing,
    // nor busted
    // else returns false
    public boolean isNotStandingOrBusted(int i, int j) {
        return !players.get(i).isStanding(j) && evaluateScore(i, j) > -1;
    }

    // params:
    // i index of player for which information is desired
    // j index of whichever of the player's hands info is desired
    // returns:
    // true if this player has more than tableMin dollars
    // or if they bet more than tableMin dollars on specified hand
    // else returns false
    public boolean hasEnoughToPlay(int i, int j) {
        return players.get(i).getWealth() >= tableMin || players.get(i).getBet(j) >= tableMin;
    }

    // param:
    // index of desired player, or
    // DEALER_ID for dealer's exposed hand, or
    // -2 for dealer with card hidden
    // returns:
    // a string representing actor in
    // specified state
    // requires:
    // if i = -2 the dealer must
    // have at least two cards in its hand
    public String getString(int i) {
         if (i == DEALER_ID) {
            return dealer.toString();
        } else {
            return players.get(i).toString();
        }
    }

    // param:
    // i index of desired player, DEALER_ID for the dealer
    // j index for which of player i's hands should be evaluated
    // j is (ignored when i = DEALER_ID)
    // returns:
    // the score specified hand is worth
    // -1 if busted, BJ_VALUE if blackjack, or highest sum
    // of card points if <= 21
    private int evaluateScore(int i, int j) {
        List<Card> handInQuestion;
        if (i != DEALER_ID) {  // player case
            handInQuestion = players.get(i).getHand(j);
        } else {  // dealer case
            handInQuestion = dealer.getHand(0);
        }

        // get possible scores for hand
        int[] scores = getHandValues(handInQuestion);

        // bust score 1
        if(scores[0] > 21 && scores[0] != BJ_VALUE) {
            scores[0] = -1;
        }

        // bust score 2
        if(scores[1] > 21 && scores[1] != BJ_VALUE) {
            scores[1] = -1;
        }

        // calculate best possible interpretation
        return Math.max(scores[0], scores[1]);
    }

    // param:
    // hand, list of cards to be evaluated as a bj hand
    // returns:
    // potential point values for hand in size 2 array
    // note the, size two part is necessary because aces are worth
    // either 1 or 11 points so a single hand could be worth different
    // amounts of points
    // however you would never want to count two aces as 11s at the same time
    // because that will always result in at least 22
    // so a hand only has 2 values worth tracking at most
    // this pair of values is what's returned
    // if no aces are present in the hand, a -1 is returned in
    // second slot
    public static int[] getHandValues(List<Card> hand) {
        int[] values = {0, -1};
        // checks if the given hand
        // is a blackjack and, if so return a result indicating such
        if (hand.size() == 2) {
            // get hands
            Card first = hand.get(0);
            Card second = hand.get(1);
            // check if they're a blackjack
            if (((first.isRoyal() || first.getRankID() == 10) &&
                    second.getRankID() == Card.ACE) ||
                    ((second.isRoyal() || second.getRankID() == 10)
                            && first.getRankID() == Card.ACE)) {
                // if so return said result
                values[0] = BlackJackGame.BJ_VALUE;
                return values;
            }
        }
        // not a blackjack so we'll have to evaluate the hand in full
        // copy hand so we don't alter original
        List<Card> handCopy = new LinkedList<>(hand);
        // start helper method
        return valuesHelper(handCopy, values);
    }

    // params:
    // workingHand is a list of those cards not yet counted
    // workingValues is a size 2 array of working counts
    // for the observed cards' points values
    // returns:
    // potential point values for hand in size 2 array, with no awareness of
    // blackjacks
    // modifies:
    // workingHand, workingValues
    private static int[] valuesHelper(List<Card> workingHand,
                                      int[] workingValues) {
        // empty hand, computation is complete
        if (workingHand.size() == 0) {
            return workingValues;
        } else {  // more work to do
            // get front card
            Card current = workingHand.remove(0);
            // check if current card is the first ace observed
            if (current.getRankID() == Card.ACE && workingValues[1] == -1) {
                // update values for ace worth 11 point
                workingValues[1] = workingValues[0] + 11;
                // update values for ace worth 1 point
                workingValues[0]++;
            } else {  // current card is not an ace, or we've
                // already found an ace
                // determine number of points card is worth
                int value;
                if (current.isRoyal()) { // royal
                    value = 10;
                } else if (current.getRankID() == Card.ACE) { // low ace
                    value = 1;
                } else { // number card
                    value = current.getRankID();
                }
                // increase both possible sums by value
                workingValues[0]+= value;
                if (workingValues[1] != -1) {
                    workingValues[1]+= value;
                }
            }
            return valuesHelper(workingHand, workingValues);
        }
    }
}