import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {
    private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        UTXOPool uniqueUTXOs = new UTXOPool();
        List<Transaction.Input> inputList = tx.getInputs();
        List<Transaction.Output> outputList = tx.getOutputs();
        double previousTxOutSum = 0;
        UTXO utxo;
        for(int i = 0;i<tx.numInputs();i++) {
            utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
            if(!utxoPool.contains(utxo)) {
                return false;
            }
            Transaction.Output output = utxoPool.getTxOutput(utxo);
            if(!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), tx.getInput(i).signature)) {
                return false;
            }
            if(uniqueUTXOs.contains(utxo)) {
                return false;
            }
            uniqueUTXOs.addUTXO(utxo, output);
            previousTxOutSum += output.value;
        }
        double currentTxOutSum = 0;
        for(Transaction.Output out: tx.getOutputs()) {
            if(out.value < 0) {
                return false;
            }
            currentTxOutSum += out.value;
        }

        if(currentTxOutSum > previousTxOutSum) {
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Set<Transaction> validTxs = new HashSet<>();

        for(int i=0;i<possibleTxs.length;i++) {
            if(isValidTx(possibleTxs[i])) {
                validTxs.add(possibleTxs[i]);
                for(Transaction.Input input: possibleTxs[i].getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }

                for(int k=0;k<possibleTxs[i].numOutputs();k++) {
                    UTXO utxo = new UTXO(possibleTxs[i].getHash(), k);
                    utxoPool.addUTXO(utxo, possibleTxs[i].getOutput(k));
                }
            }
        }

        Transaction[] validTxArray = new Transaction[validTxs.size()];
        return validTxs.toArray(validTxArray);
    }
}
