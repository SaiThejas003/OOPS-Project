import java.security.PublicKey;
import java.security.Signature;
import java.util.*;

/** The ledger: accepts only valid votes, detects tampering, and counts results. */
public class Blockchain {
    private final List<Block> chain = new ArrayList<>();
    private final Map<String, PublicKey> registered = new HashMap<>();
    private final Set<String> voted = new HashSet<>();
    private final Set<String> candidates;
    private String lastError = "";

    public Blockchain(Set<String> candidates) {
        this.candidates = candidates;
        chain.add(new Block(0, "0", "GENESIS", "-", "-"));
    }

    public void register(String voterId, PublicKey key) {
        registered.put(voterId, key);
    }

    public boolean castVote(String voterId, String candidate, String signature) {
        if (!registered.containsKey(voterId)) return reject("voter not registered");
        if (voted.contains(voterId)) return reject("voter already voted");
        if (!candidates.contains(candidate)) return reject("invalid candidate");
        if (!verify(voterId, candidate, signature)) return reject("bad signature");

        Block last = chain.get(chain.size() - 1);
        chain.add(new Block(chain.size(), last.hash, voterId, candidate, signature));
        voted.add(voterId);
        return true;
    }

    private boolean reject(String reason) {
        lastError = reason;
        System.out.println("  rejected: " + reason);
        return false;
    }

    private boolean verify(String voterId, String candidate, String signature) {
        try {
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(registered.get(voterId));
            s.update((voterId + ":" + candidate).getBytes());
            return s.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            return false;
        }
    }

    /** Recomputes every hash and checks every link. Any edit to an old block breaks this. */
    public boolean isValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block cur = chain.get(i), prev = chain.get(i - 1);
            if (!cur.hash.equals(cur.computeHash())) return false;
            if (!cur.prevHash.equals(prev.hash)) return false;
        }
        return true;
    }

    public Map<String, Integer> tally() {
        Map<String, Integer> result = new TreeMap<>();
        for (String c : candidates) result.put(c, 0);
        for (int i = 1; i < chain.size(); i++) result.merge(chain.get(i).candidate, 1, Integer::sum);
        return result;
    }

    public String getLastError() {
        return lastError;
    }

    public List<Block> getChain() {
        return chain;
    }
}
