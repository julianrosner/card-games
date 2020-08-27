/*
    represents standard 52 card playing card deck
    with or without jokers
 */

import java.util.Collections;
import java.util.LinkedList;

public class PlayingCardDeck {
    private LinkedList<Card> deck;
    private static final int NUMBER_OF_SUITS = 4;
    private static final int NUMBER_OF_RANKS = 13;

    // creates deck containing no cards
    public PlayingCardDeck() {
        deck = new LinkedList<Card>();
    }

    // params:
    // includeJokers if true adds two jokers to deck
    // builds standard 52 card deck in sorted order
    public PlayingCardDeck(boolean includeJokers) {
        deck = new LinkedList<Card>();
        // build deck
        String[] suits = {"diamonds", "clubs", "hearts", "spades"};
        for (int suit = 0; suit < NUMBER_OF_SUITS; suit++) {
            for (int rank = 1; rank <= NUMBER_OF_RANKS; rank++) {
                deck.add(new Card(rank, suits[suit]));
            }
        }
        // add jokers if desired
        if(includeJokers) {
            deck.add(new Card("joker", "joker"));
            deck.add(new Card("joker", "joker"));
        }
    }

    // returns:
    // number of cards in this deck
    public int size() {
        return deck.size();
    }

    // modifies:
    // shuffles deck
    public void shuffle() {
        Collections.shuffle(deck);
    }

    // modifies:
    // sorts deck by suit and then rank
    // in standard order
    public void sort() {
        Collections.sort(deck);
    }

    // returns:
    // card at top of deck
    // unless deck is empty in which case null is returned
    // modifies:
    // removes top card from this
    public Card draw() {
        if (size() == 0) {
            return null;
        }
        return deck.removeFirst();
    }

    // param:
    // card to add to this
    // modifies:
    // adds given card to top of deck
    public void stackOn(Card c) {
        deck.addFirst(c);
    }

    // param:
    // other deck
    // modifies:
    // empties other deck, adds its cards to top
    // of this in reverse order
    public void stackOnDeck(PlayingCardDeck other) {
        while (other.size() > 0) {
            stackOn(other.draw());
        }
    }

    // param:
    // card to add to this
    // modifies:
    // adds given card to bottom of deck
    public void addToBottom(Card c) {
        deck.addLast(c);
    }

    // returns:
    // if deck is empty "empty deck"
    // else a new line separated list of the cards
    // in the deck in the order that they would be drawn
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (size() != 0) {  // empty check
            // pop each card, read it, then add to bottom
            for (int i = 0; i < size(); i++) {
                Card current = deck.removeFirst();
                sb.append(current.toString());
                // this condition checks if i = size()
                // and not size() - 1 because we just removed
                // a card from the deck
                if (i != size()) {
                    sb.append("\n");
                }
                deck.addLast(current);
            }
        } else {
            sb.append("empty deck");
        }
        return sb.toString();
    }
}