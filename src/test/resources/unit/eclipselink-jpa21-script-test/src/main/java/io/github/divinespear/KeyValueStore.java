package io.github.divinespear;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Access(AccessType.FIELD)
@Table(name = "KEY_VALUE_STORE")
public class KeyValueStore {

    @Id
    @Column(name = "STORED_KEY", length = 128)
    private String key;

    @Column(name = "STORED_VALUE", length = 32768)
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
