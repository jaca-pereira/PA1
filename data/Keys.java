package data;

import java.security.KeyPair;
import java.util.List;

public class Keys {

    private List<KeyPair> keys;


    public Keys(List<KeyPair> keys) {
        this.keys = keys;
    }

    public List<KeyPair> getKeys() {
        return keys;
    }

}

