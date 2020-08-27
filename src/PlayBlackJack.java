/*
    The below is a text-based version of blackjack
    and proof-of-concept client to my BlackJackGame class
    that can be played with any number of players.
    All players are informed exactly where
    a shuffle has occurred in the deck
    so it is just as compatible with card counting
    as any physical version.
 */
import java.util.List;
import java.util.Scanner;

public class PlayBlackJack {
    private static final int NUM_DECKS = 6;
    private static final int NUM_PLAYERS = 1;
    // these are minimum bet size and maximum bet size respectively
    private static final int TABLE_MIN = 10;
    private static final int TABLE_MAX = 10000;
    private static final int STARTING_WEALTH = 500;

    private static BlackJackGame bjg;
    private static Scanner sc;

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        // construct new blackjackgame according to static variables above
        int[] playerWalletSizes = new int[NUM_PLAYERS];
        for (int i = 0; i < playerWalletSizes.length; i++) {
            playerWalletSizes[i] = STARTING_WEALTH;
        }
        bjg = new BlackJackGame(NUM_PLAYERS, NUM_DECKS,
                TABLE_MIN, TABLE_MAX, playerWalletSizes);

        // enter main game loop
        boolean playMore = true;
        while(playMore) {
            // collect the initial bets from all users
            takeBets(false);

            // every player draws their starting cards
            System.out.println("NOW DEALING...");
            System.out.println();
            List<Card> cardsDrawn = bjg.DrawCards();

            // print all cards drawn from dealing in order
            // and if a shuffle occurred in the middle
            // of dealing, inform players precisely where
            int numCardsDrawn = NUM_PLAYERS * 2 + 2;
            System.out.println("CARD DEALING ORDER:");
            for (int i = 0; i < cardsDrawn.size(); i++) {
                if (numCardsDrawn - bjg.getDrawsSinceLastShuffle() == i + 1) {
                    System.out.println("A SHUFFLE OCCURRED HERE");
                }
                if (i != (cardsDrawn.size() / 2) - 1) {
                    System.out.println(cardsDrawn.get(i));
                } else {
                    System.out.println("dealer's face down card");
                }
            }
            System.out.println();

            // display partial information of dealer hand
            System.out.println("DEALER:");
            System.out.println(bjg.getString(BlackJackGame.DEALER_ID));
            if (bjg.getHand(-1, -1).get(1).getRankID() == Card.ACE) {
                takeBets(true);
            }

            // we need to check if dealer has a blackjack
            // because if they do, we skip much of what follows
            List<Card> dealerHand = bjg.getHand(-1,-1);
            Card first = dealerHand.get(0);
            Card second = dealerHand.get(1);
            boolean dealerBJ = ((first.isRoyal() || first.getRankID() == 10) &&
                    second.getRankID() == Card.ACE) ||
                    ((second.isRoyal() || second.getRankID() == 10)
                            && first.getRankID() == Card.ACE);

            if (dealerBJ) {
                System.out.println("THE DEALER HAS A BLACKJACK. SKIPPING TO RESULTS...");
                System.out.println("");
            } else {

                // prompt all players for their turns until
                // every player is either standing or busted on all hands
                System.out.println("ALL STARTING HANDS DEALT. COMMENCING PLAYER TURNS.");
                System.out.println();
                getAllPlayerTurns();

                System.out.println("EVERY PLAYER IS NOW EITHER STANDING OR BUSTED");
                System.out.println();
            }
            // dealer draws until satisfied (the rules of blackjack provide
            // no room for decision making here)
            List<Card> dealerDrew = bjg.dealersTurn();

            // if a shuffle occurred anywhere in the dealer's
            // turn, inform the users precisely where
            int numDealerDrew = dealerDrew.size();
            if (numDealerDrew > bjg.getDrawsSinceLastShuffle()) {
                System.out.println("A SHUFFLE HAS OCCURRED");
                for (int i = 0; i < dealerDrew.size(); i++) {
                    if (numDealerDrew - bjg.getDrawsSinceLastShuffle() == i + 1) {
                        System.out.println("THE SHUFFLE OCCURRED HERE");
                    }
                    System.out.println(dealerDrew.get(i));
                }
            }

            // display dealer hand
            System.out.println("DEALER:");
            System.out.println(bjg.getString(-1));

            // get list of who's won, who's lost, and who's pushed
            // and resolve all bets accordingly
            List<Integer> judgements = bjg.resolveAllBets();
            bjg.resolveAllInsuranceBets();

            // display each player's hand and whether they won, lost, pushed
            // or have been expelled from the game for being too poor
            for (int i = 0; i < NUM_PLAYERS; i++) {
                System.out.println("PLAYER " + (i + 1) + ":");
                // print victory status if player has enough money to play
                // or if they ran out of funds this turn
                System.out.print(bjg.getString(i));
                for (int j = 0; j < bjg.numHands(i); j++) {
                    if (bjg.hasEnoughToPlay(i, j) || !bjg.getHand(i, j).isEmpty()) {
                        System.out.print("HAND " + (j + 1) + " RESULT: ");
                        switch (judgements.remove(0)) {
                            case BlackJackGame.BLACKJACK_ID:
                                System.out.println("BLACKJACK");
                                break;
                            case BlackJackGame.WIN_ID:
                                System.out.println("WIN");
                                break;
                            case BlackJackGame.PUSH_ID:
                                System.out.println("PUSH");
                                break;
                            case BlackJackGame.LOSS_ID:
                                System.out.println("LOSS");
                        }
                    } else {
                        System.out.println("TOO POOR TO PLAY");
                    }
                }
                System.out.println();
            }

            // reset players stand-ing state, and discard all cards
            bjg.endTurn();

            // ask if the users want to play another round
            System.out.println("Play another hand?");
            String bString = sc.nextLine();
            bString = bString.toLowerCase();
            playMore = bString.startsWith("y");
            if (playMore) {
                System.out.println();
            }
        }
        sc.close();
    }

    // reads bets from all users with enough remaining money
    // to play this hand and updates bjg object to reflect new
    // bets, if bad inputs are supplied by user
    // continues to prompt them until good inputs
    // are supplied
    // params:
    // isInsurance is false if this is the initial bet round
    // and true if this is the insurance bet round
    // modifies:
    // bjg to reflect state of user bets
    private static void takeBets(boolean isInsurance) {
        // display appropriate message
        if(isInsurance) {
            System.out.println("COMMENCE INSURANCE BETTING ROUND...");
        } else {
            System.out.println("COMMENCE BETTING...");
        }
        System.out.println();
        // collect bet information from players
        for (int i = 0; i < NUM_PLAYERS; i++) {
            // ignore players that are too poor to play
            if (bjg.getWealth(i) >= TABLE_MIN) {
                // provide prompt
                if (!isInsurance) {
                    System.out.println("What is your wager player " + (i + 1) + "? ");
                } else {
                    System.out.println("How much insurance would you care to bet player " + (i + 1) + "? ");
                }
                System.out.println("You currently have $" + bjg.getWealth(i));
                // get numeric input from user
                int bet = getBetFromUser();
                System.out.println();
                boolean goodResponse = false;
                // try to place bet, catch if bet is outside
                // of appropriate range
                // repeat until good bet supplied
                while(!goodResponse) {
                    try {
                        if (!isInsurance) {
                            bjg.placeInitialBet(i, bet);
                        } else {
                            bjg.playerInsuranceBet(i, bet);
                        }
                        goodResponse = true;
                    } catch (IllegalArgumentException e) {
                        int minLegal = TABLE_MIN;
                        int maxLegal;
                        // display meaningful error message
                        // explaining error to user and
                        // what the appropriate bounds are for
                        // them to bet
                        if (isInsurance) {
                            maxLegal = Math.min((bjg.getBet(i,0) / 2 ), bjg.getWealth(i));
                        } else {
                            maxLegal = Math.min(TABLE_MAX, bjg.getWealth(i));
                        }
                        if (minLegal <= maxLegal) {
                            System.out.println("PLEASE ENTER A VALUE BETWEEN " +
                                    "$" + minLegal + " AND $" + maxLegal);
                            if (isInsurance) {
                                System.out.println("OR $0 IF YOUR DON'T CARE TO BUY INSURANCE");
                            }
                        } else { // this is only reachable in insurance phase
                            System.out.println("EITHER YOU'RE TOO POOR TO BUY INSURANCE\n" +
                                    "OR YOUR INITIAL BET WAS SO SMALL\n" +
                                    "THAT THE LARGEST INSURANCE SUM YOU'RE\n" +
                                    "ALLOWED TO PAY IS BELOW TABLE BET MINIMUM\n" +
                                    "PLEASE ENTER \"$0\"");
                        }
                        bet = getBetFromUser();
                        System.out.println();
                    }
                }
            }
        }
    }

    // reads next user's desired bet from scanner
    // if bad input is supplied, notifies user
    // and prompts again until a usable value
    // is provided
    // a usable bet is of form "$x", "x dollars"
    // or "x" where x is an integer
    // returns:
    // int representing size of next user's desired bet
    private static int getBetFromUser () {
        int bet = -1;
        String current = sc.nextLine();
        current = sanitizeBetString(current);
        boolean goodResponse = false;
        // loop until appropriately formatted response is supplied
        while (!goodResponse) {
            try {
                bet = Integer.parseInt(current);
                goodResponse = true;
            } catch (NumberFormatException e) {
                // poorly formatted string supplied,
                // prompt user again
                System.out.println();
                System.out.println("BAD BET FORMAT\n" +
                        "ACCEPTABLE BET EXAMPLES: \"$10\", " +
                        "\"10 dollars\", \"10\"");
                current = sc.nextLine();
                current = sanitizeBetString(current);
            }
        }
        return bet;
    }

    // param:
    // betString formatted as "$x", "x dollars" or "x"
    // where x is the desired bet size
    // returns:
    // x as string
    private static String sanitizeBetString(String betString) {
        return betString.replace("$", "")
                .replace("dollars", "")
                .replace(" ", "");
    }

    // prompt players for their turn choices and enact them
    // until every player is busted, standing, or too poor
    // to play
    // modifies:
    // bjg to reflect results of all turns taken
    private static void getAllPlayerTurns() {
        boolean seenAlive = true;
        // loop as long as at least one player has neither busted
        // nor elected to stand
        // allow any active player to take their turn
        for (int i = 0; i < NUM_PLAYERS; i++) {
            for (int j = 0; j < bjg.numHands(i); j++) {
                // checks if this player is currently alive
                boolean canAct = bjg.isNotStandingOrBusted(i, j) &&
                        bjg.hasEnoughToPlay(i, j);
                while(canAct) {
                    // if alive, prompt player for action, carry it out
                    // display player state
                    System.out.println("PLAYER " + (i + 1) + ":");
                    System.out.println(bjg.getString(i));
                    System.out.println("What will you do player " +
                            (i + 1) + " on hand " + (j + 1) + "? ");
                    // get user's turn decision
                    int choice = getTurnChoiceFromUser();
                    // carry out said decision
                    try {
                        // take action
                        Card[] drawn = bjg.playerAction(i, j, choice);
                        // show the user which cards were drawn and
                        // any shuffles that may have occurred
                        System.out.println();
                        if (drawn[0] != null) {
                            System.out.println("DRAWN:");
                            System.out.println(drawn[0]);
                            if((bjg.getDrawsSinceLastShuffle() == 0 &&
                                    choice != BlackJackPlayer.SPLIT_ID)
                                    || (bjg.getDrawsSinceLastShuffle() == 1 &&
                                    choice == BlackJackPlayer.SPLIT_ID)) {
                                System.out.println("A SHUFFLE OCCURRED HERE");
                            }
                            if(drawn[1] != null) {
                                System.out.println(drawn[1]);
                                if(bjg.getDrawsSinceLastShuffle() == 0) {
                                    System.out.println("A SHUFFLE OCCURRED HERE");
                                }
                            }
                            System.out.println();
                        }
                    } catch (Exception e) {
                        // user attempted illegal move
                        // check documentation of BlackJackGame's
                        // getPlayerAction method for more
                        // information about what this mean
                        System.out.println();
                        System.out.println("ILLEGAL ACTION");
                        System.out.println();
                    }
                    canAct = bjg.isNotStandingOrBusted(i, j) &&
                            bjg.hasEnoughToPlay(i, j);
                }
            }
        }
    }

    // prompt user to make a choice for their turn
    // until a valid choice is provided
    // returns
    // int representing user move choice
    private static int getTurnChoiceFromUser () {
        String current = sc.nextLine();
        current = current.toLowerCase();
        boolean goodResponse = false;
        while (!current.equals("hit") && !current.equals("stand") &&
                !current.equals("dd") && !current.equals("split")) {
            System.out.println();
            System.out.println("UNRECOGNIZED COMMAND:");
            System.out.println("PLEASE ENTER \"hit\", \"stand\", \"dd\", OR \"split\"");
            current = sc.nextLine();
            current = current.toLowerCase();
        }
        switch (current) {
            case "hit":
                return BlackJackPlayer.HIT_ID;
            case "stand":
                return BlackJackPlayer.STAND_ID;
            case "split":
                return BlackJackPlayer.SPLIT_ID;
            default:
                return BlackJackPlayer.DOUBLE_DOWN_ID;
        }
    }
}