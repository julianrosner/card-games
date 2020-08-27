/*
    immutable object representing either a standard playing card
    with a rank and a suit or a joker.
 */

public final class Card implements Comparable<Card> {
    // note: rank IDs are not arbitrary,
    // altering their values relative to each other or to
    // the numbered 2 - 10 cards will break the compareTo
    // method
    public static final int ACE = 1;
    public static final int JACK = 11;
    public static final int QUEEN = 12;
    public static final int KING = 13;
    public static final int JOKER = 14;

    // suit IDs are 1-indexed for the sake of consistency
    // with rankIDs
    // the relative values here also affect compareTo
    public static final int DIAMONDS = 1;
    public static final int CLUBS = 2;
    public static final int HEARTS = 3;
    public static final int SPADES = 4;
    public static final int JOKER_SUIT = 5;

    // suit |  suitID
    // diamonds = 1
    // clubs = 2
    // hearts = 3
    // spades = 4
    // joker = 5
    // other values invalid
    private final int suitID;

    // rank | rankID
    // ace  = 1
    // 2 = 2
    // ... = ...
    // 10 = 10
    // jack = 11
    // queen = 12
    // king = 13
    // joker = 14
    // other values invalid
    private final int rankID;

    // the first two constructors use Strings to represent both
    // suits and non-integer ranks because that felt more user
    // friendly to me at the time
    // in retrospect I question if these really add anything meaningful
    // to the class, but at this point they may as well stay

    // the purpose of this constructor is to handle the
    // royal cards and the joker with String arguments
    // throws:
    // IllegalArgumentException if rank is not ace, joker, jack, king, or queen
    // or if suit is not diamond, spade, clubs, or hearts
    // or if a joker's suit is not "joker"
    // or if a non-joker's suit is "joker"
    public Card (String rank, String suit) throws IllegalArgumentException {
        this(rankStringToInt(rank), suit);
    }

    // constructor for the ranks 2-10, with String suits
    // throws:
    // IllegalArgumentException if rankID is out of bounds
    // or if suit is unrecognized
    // or if a joker's suit is not "joker"
    // or if a non-joker's suit is "joker"
    public Card (int rankID, String suit) throws IllegalArgumentException {
        this(rankID, suitStringToInt(suit));
    }

    // this constructor uses the provided static int IDs to
    // identify all suits and ranks
    // throws:
    // IllegalArgumentException if rankID or suitID is out of bounds
    // or if a joker's suit is not "joker"
    // or if a non-joker's suit is "joker"
    public Card (int rankID, int suitID) {
        if (suitID == -1 || rankID < 1 || rankID > JOKER ||
                (rankID == JOKER && suitID != JOKER_SUIT) ||
                (rankID != JOKER && suitID == JOKER_SUIT)) {
            throw new IllegalArgumentException();
        }
        this.rankID = rankID;
        this.suitID = suitID;
    }

    // returns:
    // rankID of this
    public int getRankID() { return rankID; }

    // returns:
    // suitId of this
    public int getSuitID() { return suitID; }

    // returns:
    // rank of this as string
    public String getRankString() {
        switch(rankID) {
            case JOKER:
                return "joker";
            case ACE:
                return "ace";
            case JACK:
                return "jack";
            case QUEEN:
                return "queen";
            case KING:
                return "king";
            default:
                return rankID + "";
        }
    }

    // returns:
    // suit of this as string
    public String getSuitString() {
        switch(suitID) {
            case DIAMONDS:
                return "diamonds";
            case CLUBS:
                return "clubs";
            case HEARTS:
                return "hearts";
            case SPADES:
                return "spades";
            case JOKER_SUIT:
                return "joker";
            default:
                // this is unreachable
                return "INVALID SUIT";
        }
    }

    // returns:
    // true if card is jack, queen, or king, else false
    public boolean isRoyal() {
        return getRankID() == JACK || getRankID() == QUEEN
                || getRankID() == KING;
    }

    // param:
    // rank of non-number card as string
    // return:
    // rankID or -1 if invalid
    private static int rankStringToInt(String rankString) {
        rankString = rankString.toLowerCase();
        switch (rankString) {
            case "joker":
                return JOKER;
            case "ace":
                return ACE;
            case "jack":
                return JACK;
            case "queen":
                return QUEEN;
            case "king":
                return KING;
            default:
                return -1;
        }
    }

    // param:
    // suit of card as string
    // returns:
    // suitID or -1 if invalid
    private static int suitStringToInt(String suitString) {
        suitString = suitString.toLowerCase();
        switch(suitString) {
            case "diamonds":
                return DIAMONDS;
            case "clubs":
                return CLUBS;
            case "hearts":
                return HEARTS;
            case "spades":
                return SPADES;
            case "joker":
                return JOKER_SUIT;
            default:
                return -1;
        }
    }

    // returns:
    // "joker" if joker else <rank> of <suit>
    @Override
    public String toString() {
        if (rankID == JOKER) {
            return "joker";
        }
        return this.getRankString() + " of " + this.getSuitString();
    }

    // two cards are equal if they have the same rank and suit
    // param:
    // other object for comparison
    // returns:
    // true if equal, false if not
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Card)) {
            return false;
        }
        Card otherCard = (Card)other;
        return compareTo(otherCard) == 0;
    }

    // sort by suit and then by rank according to standard deck ordering
    // all jokers are identical and are ordered last
    // param:
    // other card to be compared to
    // returns:
    // result of comparison
    @Override
    public int compareTo(Card other) {
        if (other.getSuitID() != suitID) {
            return -1 * Integer.compare(other.getSuitID(), suitID);
        } else {
            return -1 * Integer.compare(other.getRankID(), rankID);
        }
    }
}