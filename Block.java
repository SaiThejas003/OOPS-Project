import java.security.MessageDigest;
import java.util.HexFormat;

/** One block = one vote, linked to the previous block by its hash. */
public class Block {
    public final int index;
    public final String prevHash;
    public final String voterId;
    public final String signature;
    public final long timestamp;
    public final String hash;
    public String candidate; // not final only so Main can demonstrate tampering

    public Block(int index, String prevHash, String voterId, String candidate, String signature) {
        this.index = index;
        this.prevHash = prevHash;
        this.voterId = voterId;
        this.candidate = candidate;
        this.signature = signature;
        this.timestamp = System.currentTimeMillis();
        this.hash = computeHash();
    }

    public String computeHash() {
        try {
            String data = index + prevHash + timestamp + voterId + candidate + signature;
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
