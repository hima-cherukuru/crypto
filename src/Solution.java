import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Solution {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPair pk_scrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_alice = KeyPairGenerator.getInstance("RS").generateKeyPair();

        Transaction rootTransaction = new Transaction();
        rootTransaction.addOutput(10, pk_scrooge.getPublic());

        byte[] rootHash = BigInteger.valueOf(0).toByteArray();
        rootTransaction.addInput(rootHash, 0);

        rootTransaction.addSignature(pk_scrooge.getPrivate().getEncoded(), 0);

        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(rootTransaction.getHash(), 0);
        utxoPool.addUTXO(utxo, rootTransaction.getOutput(0));

        // Set up a test transaction
        Transaction testTransaction = new Transaction();
        testTransaction.addInput(rootTransaction.getHash(), 0);

        testTransaction.addOutput(3.0, pk_alice.getPublic());
        testTransaction.addOutput(3.0, pk_alice.getPublic());
        testTransaction.addOutput(4.0, pk_alice.getPublic());

        testTransaction.addSignature(pk_scrooge.getPrivate().getEncoded(), 0);

        TxHandler txHandler = new TxHandler(utxoPool);
        System.out.println("txhandler isValid returns: "+txHandler.isValidTx(testTransaction));
        System.out.println("txhandler handleTxs returns: "+txHandler.handleTxs(
                new Transaction[] {testTransaction}).length + " transactions");
    }
}