import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        Blockchain bc = new Blockchain(Set.of("Alice", "Bob"));

        Voter v1 = new Voter("V1"), v2 = new Voter("V2"), v3 = new Voter("V3");
        for (Voter v : List.of(v1, v2, v3)) bc.register(v.id, v.publicKey);

        System.out.println("V1 votes Alice: " + bc.castVote(v1.id, "Alice", v1.sign("Alice")));
        System.out.println("V2 votes Bob:   " + bc.castVote(v2.id, "Bob", v2.sign("Bob")));

        System.out.println("V1 votes again (double vote):");
        bc.castVote(v1.id, "Bob", v1.sign("Bob"));

        System.out.println("Someone forges a vote for V3 using V2's key:");
        bc.castVote("V3", "Bob", v2.sign("Bob"));

        System.out.println("Unregistered voter tries to vote:");
        Voter outsider = new Voter("V9");
        bc.castVote(outsider.id, "Alice", outsider.sign("Alice"));

        System.out.println("V3 votes Alice: " + bc.castVote(v3.id, "Alice", v3.sign("Alice")));

        System.out.println("\nChain valid? " + bc.isValid());
        System.out.println("Tally: " + bc.tally());

        System.out.println("\nTampering: changing block 1's candidate to Bob...");
        bc.getChain().get(1).candidate = "Bob";
        System.out.println("Chain valid? " + bc.isValid());
    }
}
