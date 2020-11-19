package com.iptsco.interview.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "char"
})
public class Password {
    @JsonIgnore
    private State state;
    @JsonIgnore
    private String message;
    @JsonProperty("char")
    private List<String> passwords = null;

    public Password() {
        super();
    }

    public Password(State state, String message, List<String> passwords) {
        this.state = state;
        this.message = message;
        this.passwords = passwords;
    }

    public static Password success(Password password) {
        return new Password(State.SUCCESS, null, password.getPasswords());
    }

    public static Password error(Throwable throwable) {
        return error(throwable.getMessage());
    }

    public static Password error(String message) {
        return new Password(State.ERROR, message, null);
    }

    public static Password loading() {
        return new Password(State.LOADING, null, null);
    }

    @JsonProperty("char")
    public List<String> getPasswords() {
        return passwords;
    }

    @JsonProperty("char")
    public void setPasswords(List<String> passwords) {
        this.passwords = passwords;
    }

    public State getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public enum State {
        SUCCESS, ERROR, LOADING
    }
}