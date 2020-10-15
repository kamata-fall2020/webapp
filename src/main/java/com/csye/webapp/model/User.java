package com.csye.webapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.type.descriptor.sql.TinyIntTypeDescriptor;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String user_id;

    @NotBlank(message="name cannot be blank") @NotNull(message="name cannot be null") @Size(min=2, message = "size should be atleast 2")
    private String first_name;

    @NotBlank @NotNull
    private String last_name;

    @NotNull @NotBlank @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

   @NotNull @NotBlank
    private String username;

    @ReadOnlyProperty
    private Timestamp account_created;

    @ReadOnlyProperty
    private Timestamp account_updated;



    @ReadOnlyProperty @Column(columnDefinition = "integer default 1") @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int enabled;

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public Timestamp getAccount_created() {
        return account_created;
    }

    public void setAccount_created(Timestamp account_created) {
        this.account_created = account_created;
    }

    public Timestamp getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(Timestamp account_updated) {
        this.account_updated = account_updated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        return "{" +
                "\"user_id\"=" +"\""+user_id + "\""+
                ", \"first_name\"=" +"\"" +first_name + '\"' +
                ", \"last_name\"=" +"\""+ last_name + '\"' +
                ", \"account_created\"=\"" + account_created +"\""+
                ", \"account_updated\"=\"" + account_updated +"\""+
                ", \"username\"=\"" + username + '\"' +
                "}";
    }
}
