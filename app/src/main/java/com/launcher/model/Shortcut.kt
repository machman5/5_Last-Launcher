package com.launcher.model;

public class Shortcut {

    private String name;
    private String uri;

    public Shortcut() {

    }

    public Shortcut(String name, String uris) {
        this.name = name;
        this.uri = uris;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
