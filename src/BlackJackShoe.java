/*
    represents blackjack shoe which is a large card dispenser
    usually holding between 6 and 8 decks of playing cards
    as well as a special card inserted late into the pile
    that, when drawn, tells the dealer to immediately shuffle
    the discard pile back into the shoe
 */

import java.util.List;
import java.util.Random;

public class BlackJackShoe extends PlayingCardDeck {

    // where used cards are sent until the next special card comes up
    private PlayingCardDeck discardPile;
    // how many spaces from the top the current special card is
    private int specialCardIndex;
    // number of times a card has been drawn since the last
    // time the deck was shuffled
    private int drawsSinceLastShuffle;

    // constructs a shuffled blackjack shoe with given number of decks
    // throws:
    // IllegalArgumentException if numDecks < 1
    public BlackJackShoe(int numDecks) throws IllegalArgumentException {
        super();
        // sanity check
        if (numDecks < 1) {
            throw new IllegalArgumentException();
        }
        this.discardPile = new PlayingCardDeck();
        // add numDecks decks to this
        for (int i = 0; i < numDecks; i++) {
            stackOnDeck(new PlayingCardDeck(false));
        }
        // shuffle
        shuffle();
        updateSpecialCard();
        drawsSinceLastShuffle = 0;
    }

    // returns:
    // next card from top of shoe
    // or null if shoe is empty
    // modifies:
    // deck to contain one less card
    // and if the called when specialCardIndex = 1,
    // the discard pile is added back into the deck
    // and then all cards are shuffled
    public Card draw() {
        specialCardIndex--;
        drawsSinceLastShuffle++;
        Card current = super.draw();
        if (specialCardIndex == 0) {
            stackOnDeck(discardPile);
            shuffle();
            updateSpecialCard();
            drawsSinceLastShuffle = 0;
        }
        return current;
    }

    // param:
    // list of cards representing hand
    // modifies:
    // hand to not contain any cards
    // discardPile to hold all cards hand once did
    public void discardHand(List<Card> hand) {
        while(!hand.isEmpty()) {
            discardPile.stackOn(hand.remove(0));
        }
    }

    // returns:
    // number of times deck has been drawn from since last shuffle
    public int getDrawsSinceLastShuffle() {
        return drawsSinceLastShuffle;
    }

    // adds a special card at a random point at least 75% of
    // the way into the deck to mark when the discard pile
    // should be shuffled back into the shoe
    // modifies:
    // location of special card in deck
    // requires:
    // deck contains at least 2 cards
    private void updateSpecialCard() {
        int jLower = (int) (size() * 0.75);
        int jUpper = size();
        Random randy = new Random();
        specialCardIndex = randy.nextInt(jUpper - jLower) + jLower + 1;
    }
}
