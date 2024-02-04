package com.example.filetransfertest

import android.util.Log

fun api_login(username: String, password: String): ByteArray {
    var command = username.toByteArray(Charsets.UTF_8)
    command = command.plus(0x03)
    command = command.plus(password.toByteArray(Charsets.UTF_8))
    return command

}

fun api_send_command(typedCmd: String): ByteArray{
    var command = typedCmd.toByteArray(Charsets.UTF_8)
    command = command.plus(0x03)
    return command
}

fun api_send_file(filename: String, content: ByteArray): ByteArray{
    Log.d("API", "Sending File")
    var command = "sfile".toByteArray(Charsets.UTF_8)
    command = command.plus(0x03)
    command = command.plus(filename.toByteArray(Charsets.UTF_8))
    command = command.plus(0x03)
    command = command.plus(content)
    return command
}

fun api_request_file(filename: String): String{
    return "rfile${0x03}${filename}"
}