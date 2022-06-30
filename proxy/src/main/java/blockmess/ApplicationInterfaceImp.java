package blockmess;


import applicationInterface.ApplicationInterface;

import data.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;


public class ApplicationInterfaceImp extends ApplicationInterface {
    private Ledger ledger;
    private static final byte[] LEDGER = new byte[]{0x0};
    public static final int REWARD = 30;

    public ApplicationInterfaceImp(@NotNull String[] blockmessProperties, Ledger ledger) {
        super(blockmessProperties);
        this.ledger = ledger;
    }

    private boolean sendTransaction(Request request) {
        if (request.getValue() < 0)
            throw new IllegalArgumentException("Value must be positive!");
        this.ledger.sendTransaction(new Transaction(request.getAccount(), request.getAccountDestiny(), request.getNonce(), request.getValue(), request.getSignature()));
        return true;
    }

    @Override
    public byte[] processOperation(byte[] bytes) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
            Request request = (Request) objIn.readObject();
            Reply reply;
            switch (request.getRequestType()) {
                case CREATE_ACCOUNT:
                    reply = new Reply(this.ledger.addAccount(request.getAccount()), request.getRequestType());
                    break;
                case SEND_TRANSACTION:
                    reply = new Reply(this.sendTransaction(request), LedgerRequestType.SEND_TRANSACTION);
                    break;
                case GET_BLOCK_TO_MINE:
                    reply = new Reply(this.ledger.getBlockToMine(), LedgerRequestType.GET_BLOCK_TO_MINE);
                    break;
                case MINE_BLOCK:
                    this.ledger.addMinedBlock(request.getBlock());
                    Request rewardRequest = new Request(LedgerRequestType.LOAD_MONEY, this.LEDGER, request.getAccount(), REWARD, -1);
                    rewardRequest.setPublicKey(request.getPublicKey().getEncoded());
                    rewardRequest.setSignature(request.getSignature());
                    this.sendTransaction(rewardRequest);
                    reply = new Reply(REWARD, request.getRequestType());
                    break;
                default:
                    reply = new Reply("Operation not supported");
            }

            objOut.writeObject(reply);
            objOut.flush();
            byteOut.flush();
            return byteOut.toByteArray();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
                Reply reply = new Reply("Operation not supported");
                objOut.writeObject(reply);
                objOut.flush();
                byteOut.flush();
                return byteOut.toByteArray();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return new byte[0];
    }
}
