import java.security.*;
import java.util.Base64;

/** A registered voter with a chosen name, a chosen ID, and an RSA key pair. */
public class Voter {
    public final String id;
    public final String name;
    public final PublicKey publicKey;
    private final PrivateKey privateKey;

    public Voter(String id) throws GeneralSecurityException {
        this(id, id);
    }

    public Voter(String id, String name) throws GeneralSecurityException {
        this.id = id;
        this.name = name;
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair kp = gen.generateKeyPair();
        this.publicKey = kp.getPublic();
        this.privateKey = kp.getPrivate();
    }

    /** Signs "voterId:candidate" so nobody else can cast a vote on this voter's behalf. */
    public String sign(String candidate) throws GeneralSecurityException {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(privateKey);
        s.update((id + ":" + candidate).getBytes());
        return Base64.getEncoder().encodeToString(s.sign());
    }
}
