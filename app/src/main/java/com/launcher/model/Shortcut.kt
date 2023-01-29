package com.launcher.model

class Shortcut {
    var name: String = ""
    var uri: String = ""

    constructor()
    constructor(
        name: String,
        uris: String
    ) {
        this.name = name
        uri = uris
    }
}
